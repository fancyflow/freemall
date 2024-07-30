package com.zqswjtu.freemall.order.web;

import com.zqswjtu.common.exception.NoStockException;
import com.zqswjtu.common.vo.member.MemberResponseVo;
import com.zqswjtu.freemall.order.exception.OrderStatusUpdateFailureException;
import com.zqswjtu.freemall.order.exception.OrderTimeOutException;
import com.zqswjtu.freemall.order.interceptor.LoginInterceptor;
import com.zqswjtu.freemall.order.service.OrderService;
import com.zqswjtu.freemall.order.vo.OrderConfirmVo;
import com.zqswjtu.freemall.order.vo.OrderSubmitVo;
import com.zqswjtu.freemall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        MemberResponseVo memberResponseVo = LoginInterceptor.loginUser.get();
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder(memberResponseVo);
        model.addAttribute("confirmOrderData", orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        // 去服务器创建订单，验令牌、验价格、锁库存
        // 下单成功来到支付选择页面
        // 下单失败回到订单确认页重新确认订单信息
        SubmitOrderResponseVo submitOrderResponseVo = null;
        try {
            submitOrderResponseVo = orderService.submitOrder(orderSubmitVo);
        } catch (NoStockException e) {
            String msg = e.getMessage();
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.freemall.com/toTrade";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
            return "redirect:http://order.freemall.com/toTrade";
        }
        if (submitOrderResponseVo != null) {
            if (submitOrderResponseVo.getCode() == 0) {
                // 下单成功来到支付选择页面
                model.addAttribute("submitOrderResp", submitOrderResponseVo);
                return "pay";
            }
            String msg = "下单失败：";
            switch (submitOrderResponseVo.getCode()) {
                case 1:
                    msg += "订单信息过期，请重新下单"; break;
                case 2:
                    msg += "订单商品价格商品发生变化，请确认后再次提交"; break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
        }
        return "redirect:http://order.freemall.com/toTrade";
    }

    @GetMapping("/payOrder")
    public String payOrder(@RequestParam("orderSn") String orderSn, RedirectAttributes redirectAttributes) {
        System.out.println("订单号为：" + orderSn);
        try {
            orderService.payOrder(orderSn);
        } catch (OrderStatusUpdateFailureException e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
            return "redirect:http://order.freemall.com/toTrade";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
            return "redirect:http://cart.freemall.com/cart.html";
        }
        return "redirect:http://cart.freemall.com/cart.html";
    }
}
