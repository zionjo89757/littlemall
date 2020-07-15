package xyz.zionjo.littlemall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import xyz.zionjo.common.exception.NoStockException;
import xyz.zionjo.common.to.SkuHasStockVo;
import xyz.zionjo.common.to.mq.OrderTo;
import xyz.zionjo.common.to.mq.QuickOrderTo;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.common.utils.R;
import xyz.zionjo.common.vo.MemberVo;
import xyz.zionjo.littlemall.order.constant.OrderConstant;
import xyz.zionjo.littlemall.order.dao.OrderDao;
import xyz.zionjo.littlemall.order.entity.OrderEntity;
import xyz.zionjo.littlemall.order.entity.OrderItemEntity;
import xyz.zionjo.littlemall.order.enume.OrderStatusEnum;
import xyz.zionjo.littlemall.order.feign.CartFeignService;
import xyz.zionjo.littlemall.order.feign.MemberFeignService;
import xyz.zionjo.littlemall.order.feign.ProductFeignService;
import xyz.zionjo.littlemall.order.feign.WareFeignService;
import xyz.zionjo.littlemall.order.interceptor.LoginUserInterceptor;
import xyz.zionjo.littlemall.order.service.OrderItemService;
import xyz.zionjo.littlemall.order.service.OrderService;
import xyz.zionjo.littlemall.order.to.OrderCreateTo;
import xyz.zionjo.littlemall.order.vo.*;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> orderSubmitThreadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // TODO 远程查询所有收获地址列表
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberVo.getId());
            confirmVo.setAddress(address);
        }, executor);


        CompletableFuture<Void> getItemsFuture = CompletableFuture.runAsync(() -> {
            // TODO 远程查询购物车所有选中的购物项
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            //cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(currentUserCartItems);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            // TODO 远程查询商品库存
            R skusHasStock = wareFeignService.getSkusHasStock(collect);
            Map<Long, Boolean> mapStock = skusHasStock.getData(new TypeReference<List<SkuHasStockVo>>() {
            }).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            confirmVo.setStocks(mapStock);
        }, executor);


        // 查询用户积分信息
        Integer integration = memberVo.getIntegration();
        confirmVo.setIntegration(integration);

        // TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, getItemsFuture).get();

        return confirmVo;
    }

    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        orderSubmitThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();
        // 1 验证令牌[保证令牌的对比和删除为原子性操作]
        String orderToken = vo.getOrderToken();
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberVo.getId()), orderToken);
        if (result == 0L) {
            responseVo.setCode(1);
            return responseVo;
        } else {
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getPayPrice();
            BigDecimal payPrice = vo.getPayPrice();
            // 验价
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                saveOrder(order);
                // TODO 远程调用锁库存
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(collect);
                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    // 锁定成功
                    responseVo.setOrder(order.getOrder());
                    responseVo.setCode(0);
                    // TODO 订单创建成功，发送消息至MQ
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
                    return responseVo;
                } else {
                    // 失败
                    throw new NoStockException();
                }
            } else {
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity orderEntity = getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        // 查询当前订单的最新状态
        OrderEntity byId = this.getById(entity.getId());
        if(byId.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId(byId.getId());
                orderEntity.setStatus(OrderStatusEnum.CANCLED.getCode());
                this.updateById(orderEntity);
                OrderTo orderTo = new OrderTo();
                BeanUtils.copyProperties(byId,orderTo);
                try {
                    rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);
                }catch (Exception e){

                }
            }

    }

    @Override
    public void createSeckillOrder(QuickOrderTo orderTo) {
        // TODO
        log.info("创建订单。。。");
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        save(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);



    }

    private OrderCreateTo createOrder(){
        OrderCreateTo createTo = new OrderCreateTo();
        // 生成订单号
        String orderSn = IdWorker.getTimeId();

        // 创建订单
        OrderEntity orderEntity = buildOrder(orderSn);

        //获取所有订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        // 验价
        computePrice(orderEntity,orderItemEntities);

        createTo.setFare(orderEntity.getFreightAmount());
        createTo.setOrder(orderEntity);
        createTo.setOrderItems(orderItemEntities);
        createTo.setPayPrice(orderEntity.getPayAmount());


        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal("0");
        BigDecimal coupon = new BigDecimal("0");
        BigDecimal integration = new BigDecimal("0");
        BigDecimal promotion = new BigDecimal("0");
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;
        if(orderItemEntities != null && orderItemEntities.size()>0){

            for (OrderItemEntity entity : orderItemEntities) {
                total = total.add(entity.getRealAmount());
                coupon= coupon.add(entity.getCouponAmount());
                integration = integration.add(entity.getIntegrationAmount());
                promotion = promotion.add(entity.getPromotionAmount());
                giftIntegration += entity.getGiftIntegration();
                giftGrowth += entity.getGiftGrowth();
            }
        }
        orderEntity.setTotalAmount(total);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setGrowth(giftGrowth);
        orderEntity.setIntegration(giftIntegration);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
    }

    private OrderEntity buildOrder(String orderSn) {
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();
        // 创建订单
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(memberVo.getId());
        // TODO 远程查询获取收货地址信息
        OrderSubmitVo orderSubmitVo = orderSubmitThreadLocal.get();
        R r = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = r.getData(new TypeReference<FareVo>() {
        });

        // 设置运费信息
        entity.setFreightAmount(fareResp.getFare());
        // 设置收货人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        entity.setDeleteStatus(0);

        return entity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // TODO 远程获取所有订单项信息
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if(currentUserCartItems != null && currentUserCartItems.size()>0){
            List<OrderItemEntity> collect = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1 TODO 商品的SPU信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setCategoryId(data.getCatalogId());

        // 2 商品的SKU信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        itemEntity.setSkuQuantity(cartItem.getCount());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(),";");
        itemEntity.setSkuAttrsVals(skuAttr);
        // 3 积分信息

        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString())).intValue());

        // 4 价格
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        BigDecimal origin = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = origin.subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }

}