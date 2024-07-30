package com.zqswjtu.freemall.cart.service;

import com.zqswjtu.freemall.cart.vo.CartItemVo;
import com.zqswjtu.freemall.cart.vo.CartVo;

import java.util.List;

public interface CartService {
    CartItemVo addCartItem(Long skuId, Integer num);

    CartItemVo getCartItem(Long skuId);

    CartVo getCart();

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer checked);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItemVo> getUserCartItems(Long userId);

    Integer deleteCheckedCartItems(Long userId);
}
