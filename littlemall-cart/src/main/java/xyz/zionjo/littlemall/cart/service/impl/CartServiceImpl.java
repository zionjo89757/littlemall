package xyz.zionjo.littlemall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.cart.feign.ProductFeignService;
import xyz.zionjo.littlemall.cart.interceptor.CartInterceptor;
import xyz.zionjo.littlemall.cart.service.CartService;
import xyz.zionjo.littlemall.cart.vo.CartItemVo;
import xyz.zionjo.littlemall.cart.vo.CartVo;
import xyz.zionjo.littlemall.cart.vo.SkuInfoVo;
import xyz.zionjo.littlemall.cart.vo.UserInfoTo;

import javax.swing.plaf.basic.BasicIconFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    private static final String CART_PREFIX = "littlemall:cart:";

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();



        String res = (String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(res)){
            CartItemVo cartItemVo = new CartItemVo();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                // TODO 1 远程查询商品信息

                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                //  设置购物项信息
                cartItemVo.setCheck(true);
                cartItemVo.setCount(num);
                cartItemVo.setImage(skuInfo.getSkuDefaultImg());
                cartItemVo.setTitle(skuInfo.getSkuTitle());
                cartItemVo.setSkuId(skuInfo.getSkuId());
                cartItemVo.setPrice(skuInfo.getPrice());
            },executor);

            // TODO 2 远程查询商品sku销售属性信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> saleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttr(saleAttrValues);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask,getSkuSaleAttrValues).get();


            String s = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(),s);
            return cartItemVo;
        }else {
            CartItemVo cartItemVo = JSON.parseObject(res, CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount()+num);

            String s = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(),s);
            return cartItemVo;
        }




    }

    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(res, CartItemVo.class);
        return cartItemVo;
    }

    @Override
    public CartVo getCart() throws ExecutionException, InterruptedException {
        CartVo cart = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null){
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> tempCartItems = getCartItems(tempCartKey);
            if(tempCartItems != null){
                // 合并临时购物车数据 -->可优化
                for (CartItemVo tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(),tempCartItem.getCount());
                }
                // 清空购物车
                clearCart(tempCartKey);
            }
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);


        }else{
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        }
        return cart;
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if(userInfoTo.getUserId() != null){
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }else{
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItemVo> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values!=null && values.size()>0) {
            List<CartItemVo> collect = values.stream().map(item -> {
                String str = (String) item;
                return JSON.parseObject(str,CartItemVo.class);
            }).collect(Collectors.toList());
            return collect;
        }else{
            return null;
        }
    }

    @Override
    public void clearCart(String cartKey){
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCheck(check.equals(1));
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);

    }

    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() == null){
            return null;
        }else{
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItemVo> cartItems = getCartItems(cartKey);
            List<CartItemVo> collect = cartItems.stream().filter(CartItemVo::getCheck).map(item->{
                // TODO 远程调用商品服务查价格
                R price = productFeignService.getPrice(item.getSkuId());
                String data = (String) price.get("data");
                item.setPrice(new BigDecimal(data));
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
    }
}
