package xyz.zionjo.littlemall.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.zionjo.common.exception.NoStockException;
import xyz.zionjo.littlemall.order.service.OrderService;
import xyz.zionjo.littlemall.order.vo.OrderConfirmVo;
import xyz.zionjo.littlemall.order.vo.OrderSubmitVo;
import xyz.zionjo.littlemall.order.vo.SubmitOrderResponseVo;

import java.util.concurrent.ExecutionException;
import java.util.jar.Attributes;

@Controller
public class OrderWebController {

    @Autowired
    OrderService OrderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = OrderService.confirmOrder();
        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        try {
            //下单
            SubmitOrderResponseVo responseVo = OrderService.submitOrder(vo);
            if(responseVo.getCode()==0){
                //成功
                model.addAttribute("submitOrderResp",responseVo);
                return "pay";
            }else{
                String msg = "下单失败:";
                //失败
                switch (responseVo.getCode()){
                    case 1: msg += "订单信息过期，请刷新提交"; break;
                    case 2: msg += "订单商品价格发生变动,请确认后再次提交";break;
                    case 3: msg += "库存锁定失败，商品库存不足";break;

                }
                redirectAttributes.addFlashAttribute("msg",msg);
                return "redirect:http://order.littlemall.com/ToTrade";
            }
        }catch (Exception e){
            if(e instanceof NoStockException){
                String msg = ((NoStockException) e).getMessage();
                redirectAttributes.addFlashAttribute("msg",msg);

            }
            return "redirect:http://order.littlemall.com/ToTrade";
        }

    }
}
