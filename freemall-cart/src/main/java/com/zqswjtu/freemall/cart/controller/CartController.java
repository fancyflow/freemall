package com.zqswjtu.freemall.cart.controller;

import com.zqswjtu.freemall.cart.interceptor.CartInterceptor;
import com.zqswjtu.freemall.cart.service.CartService;
import com.zqswjtu.freemall.cart.vo.CartItemVo;
import com.zqswjtu.freemall.cart.vo.CartVo;
import com.zqswjtu.freemall.cart.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart.html")
    public String cartListPage(Model model) {
//        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
//        System.out.println(userInfoVo);
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }


    @GetMapping("/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) {
        CartItemVo cartItemVo = cartService.addCartItem(skuId, num);
//        model.addAttribute("cartItem", cartItemVo);
//        model.addAttribute("skuId", skuId);
        // 该方法会将参数自动拼接到重定向的url中
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.freemall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        // 重定向到成功页面，而不是重复提交数据
        CartItemVo cartItemVo = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItemVo);
        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("checked") Integer checked) {
        cartService.checkItem(skuId, checked);
        return "redirect:http://cart.freemall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.freemall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.freemall.com/cart.html";
    }

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItemVo> getCurrentUserCartItems(@RequestParam("userId") Long userId) {
        return cartService.getUserCartItems(userId);
    }

    @GetMapping("/deletePayedCartItems")
    @ResponseBody
    public Integer deletePayedCartItems(@RequestParam("userId") Long userId) {
        /**
         * 远程调用异常，忘记加@ResponseBody注解
         * [500] during [GET] to [http://freemall-cart/deletePayedCartItems?userId=1] [CartFeignService#deletePayedCartItems(Long)]:
         * [{"timestamp":"2024-05-31T13:31:49.653+00:00","status":500,"error":"Internal Server Error",
         * "trace":"java.lang.IllegalArgumentException: Unknown return value type: java.lang.Integer\r\n\tat org.springfr... (5165 bytes)]
         */
        return cartService.deleteCheckedCartItems(userId);
    }
}
