package xyz.zionjo.littlemall.cart.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.zionjo.littlemall.cart.service.CartService;
import xyz.zionjo.littlemall.cart.vo.CartItemVo;
import xyz.zionjo.littlemall.cart.vo.CartVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Controller
public class CartController {


    @Autowired
    CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItemVo> getCurrentUserCartItems(){

        List<CartItemVo> userCartItems = cartService.getUserCartItems();
        //log.info("进入getCurrentUserCartItems {}" , userCartItems.size());
        return userCartItems;
    }

    /**
     * 没登陆： 按照cookie里的user-key
     * 登录： session
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        //UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart",cartVo);
        return "cartList";
    }
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);
        redirectAttributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.littlemall.com/addToCartSuccess.html";
    }


    @GetMapping("/addToCartSuccess.html")
    public String addToSuccessPage(@RequestParam("skuId") Long skuId,Model model) {

        CartItemVo cartItemVo = cartService.getCartItem(skuId);
        model.addAttribute("item",cartItemVo);
        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId")Long skuId,@RequestParam("check") Integer check){

        cartService.checkItem(skuId,check);

        return "redirect:http://cart.littlemall.com/cart.html";

    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId")Long skuId,@RequestParam("num") Integer num){

        cartService.countItem(skuId,num);

        return "redirect:http://cart.littlemall.com/cart.html";

    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId")Long skuId){

        cartService.deleteItem(skuId);

        return "redirect:http://cart.littlemall.com/cart.html";

    }

}
