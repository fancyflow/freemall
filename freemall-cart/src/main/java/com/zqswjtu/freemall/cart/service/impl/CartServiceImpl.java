package com.zqswjtu.freemall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zqswjtu.common.constant.CartConstant;
import com.zqswjtu.common.to.SkuInfoTo;
import com.zqswjtu.common.utils.R;
import com.zqswjtu.freemall.cart.feign.ProductFeignService;
import com.zqswjtu.freemall.cart.interceptor.CartInterceptor;
import com.zqswjtu.freemall.cart.service.CartService;
import com.zqswjtu.freemall.cart.vo.CartItemVo;
import com.zqswjtu.freemall.cart.vo.CartVo;
import com.zqswjtu.freemall.cart.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;


    @Override
    public CartItemVo addCartItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();
        String object = (String) cartOperations.get(skuId.toString());

        CartItemVo cartItemVo = new CartItemVo();
        if (StringUtils.isEmpty(object)) {
            // 购物车没有这个商品
            R info = productFeignService.info(skuId);
            // 远程查询商品信息
            SkuInfoTo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoTo>() {});
            // 封装数据
            cartItemVo.setCheck(false);
            cartItemVo.setCount(num);
            cartItemVo.setImage(skuInfo.getSkuDefaultImg());
            cartItemVo.setTitle(skuInfo.getSkuTitle());
            cartItemVo.setSkuId(skuId);
            cartItemVo.setPrice(skuInfo.getPrice());
            // 远程调用查询sku组合信息
            cartItemVo.setSkuAttrValues(productFeignService.getSkuSaleAttrValues(skuId));

            String jsonString = JSON.toJSONString(cartItemVo);
            cartOperations.put(skuId.toString(), jsonString);
        } else {
            // 购物车有此商品，数量相加即可
            cartItemVo = JSON.parseObject(object, CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount() + num);
            cartOperations.put(skuId.toString(), JSON.toJSONString(cartItemVo));
        }

        return cartItemVo;
    }

    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();
        String str = (String) cartOperations.get(skuId.toString());
        return JSON.parseObject(str, CartItemVo.class);
    }

    @Override
    public CartVo getCart() {
        CartVo cartVo = new CartVo();
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        if (userInfoVo != null && userInfoVo.getUserId() != null) {
            // 登录状态
            String cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserId();
            BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(cartKey);
            // 判断该用户是否有临时购物车
            String tempUserCartKey = CartConstant.CART_PREFIX + userInfoVo.getUserKey();
            List<CartItemVo> tempCartItems = getCartItems(tempUserCartKey);
            // 如果有将其添加到登录后的用户购物车中
            if (tempCartItems != null) {
                for (CartItemVo cartItemVo : tempCartItems) {
                    addCartItem(cartItemVo.getSkuId(), cartItemVo.getCount());
                }
            }
            // 清除临时购物车的数据
            clearCart(tempUserCartKey);

            // 获取登录后的购物车数据，如果有临时购物车，那么在上一步已经被添加到了用户的购物车里
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        } else {
            // 未登录状态
            String cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserKey();
            // 临时购物车的购物项
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        }
        return cartVo;
    }

    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    /**
     * 勾选购物项
     * @param skuId
     * @param checked
     */
    @Override
    public void checkItem(Long skuId, Integer checked) {
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCheck(checked == 1);
        BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();
        cartOperations.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    /**
     * 修改购物车中某项商品的数量
     * @param skuId
     * @param num
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();
        cartOperations.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    /**
     * 删除购物车中的某项商品
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();
        cartOperations.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getUserCartItems(Long userId) {
//        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
//        if (userInfoVo.getUserId() == null) {
//            return null;
//        }
        String cartKey = CartConstant.CART_PREFIX + userId;
        List<CartItemVo> cartItems = getCartItems(cartKey);
        assert cartItems != null;
        // 获取所有被选中的购物项
        return cartItems.stream()
                .filter(CartItemVo::getCheck)
                .peek(cartItemVo -> cartItemVo.setPrice(productFeignService.getPrice(cartItemVo.getSkuId())))
                .collect(Collectors.toList());
    }

    /**
     * 删除购物车中用户已经购买的商品
     * @param userId
     * @return
     */
    @Override
    public Integer deleteCheckedCartItems(Long userId) {
        int count = 0;
        if (userId != null) {
            String cartKey = CartConstant.CART_PREFIX + userId;
            List<CartItemVo> cartItems = getCartItems(cartKey);
            BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(cartKey);
            if (cartItems != null) {
                for (CartItemVo cartItemVo : cartItems) {
                    if (cartItemVo.getCheck()) {
                        ops.delete(cartItemVo.getSkuId().toString());
                        ++count;
                    }
                }
            }
        }
        return count;
    }

    private List<CartItemVo> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(cartKey);
        // JSON格式的对象数据
        List<Object> values = ops.values();
        if (values != null && !values.isEmpty()) {
            return values.stream().map(obj -> {
                String s = (String) obj;
                CartItemVo cartItemVo = JSON.parseObject(s, CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private BoundHashOperations<String, Object, Object> getCartOperations() {
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        String cartKey = null;
        // 判断用户是否登录
        if (userInfoVo.getUserId() != null) {
            cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserKey();
        }
        return stringRedisTemplate.boundHashOps(cartKey);
    }
}
