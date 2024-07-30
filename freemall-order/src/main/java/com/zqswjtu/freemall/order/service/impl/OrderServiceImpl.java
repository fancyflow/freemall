package com.zqswjtu.freemall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zqswjtu.common.constant.OrderConstant;
import com.zqswjtu.common.exception.NoStockException;
import com.zqswjtu.common.to.es.SkuHasStockTo;
import com.zqswjtu.common.to.mq.OrderTo;
import com.zqswjtu.common.to.mq.SeckillOrderTo;
import com.zqswjtu.common.utils.R;
import com.zqswjtu.common.vo.member.MemberResponseVo;
import com.zqswjtu.freemall.order.entity.OrderItemEntity;
import com.zqswjtu.common.enums.OrderStatusEnum;
import com.zqswjtu.freemall.order.exception.OrderStatusUpdateFailureException;
import com.zqswjtu.freemall.order.exception.OrderTimeOutException;
import com.zqswjtu.freemall.order.feign.CartFeignService;
import com.zqswjtu.freemall.order.feign.MemberFeignService;
import com.zqswjtu.freemall.order.feign.ProductFeignService;
import com.zqswjtu.freemall.order.feign.WareFeignService;
import com.zqswjtu.freemall.order.interceptor.LoginInterceptor;
import com.zqswjtu.freemall.order.service.OrderItemService;
import com.zqswjtu.freemall.order.to.OrderCreateTo;
import com.zqswjtu.freemall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.order.dao.OrderDao;
import com.zqswjtu.freemall.order.entity.OrderEntity;
import com.zqswjtu.freemall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认页返回所需要的数据
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder(MemberResponseVo memberResponseVo) {
        assert memberResponseVo != null;
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        // 远程查询所有的收货地址列表
        List<MemberAddressVo> addresses = memberFeignService.getAddress(memberResponseVo.getId());
        orderConfirmVo.setMemberAddressVos(addresses);
        // 远程查询购物车中选中的购物项
        List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems(memberResponseVo.getId());
        // feign在请求调用之前会构造请求，调用很多的拦截器，feign远程调用会丢失请求头，必须加上feign远程调用的请求拦截器
        orderConfirmVo.setItems(items);
        // 查询商品库存信息
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        R hasStock = wareFeignService.getSkusHasStock(skuIds);
        if (hasStock.getCode() != 0) {
            // 查询失败
        }
        List<SkuHasStockTo> hasStockData = hasStock.getData(new TypeReference<List<SkuHasStockTo>>() {});
        Map<Long, Boolean> stockMap = hasStockData.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
        orderConfirmVo.setStocks(stockMap);
        // 查询用户积分
        orderConfirmVo.setIntegration(memberResponseVo.getIntegration());

        // 生成该订单对应的唯一令牌保证幂等性
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setUniqueToken(token);
        return orderConfirmVo;
    }

    /**
     * 下单操作实现
     * 去服务器创建订单，验令牌、验价格、锁库存
     * @param orderSubmitVo
     * @return
     */
    // 发生运行时异常事务会自动回滚
    // @GlobalTransactional // 该注解不适用于高并发场景，两阶段提交事务的变体
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) throws NoStockException {
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        MemberResponseVo user = LoginInterceptor.loginUser.get();
        assert user != null;
        // 验证令牌, 必须保证获取令牌，比较令牌，删除令牌是一个原子操作
        // 单机可以加本地锁，如果是分布式应用则需要使用分布式锁，redis的setnx命令可以作为分布式锁，redis执行lua脚本也可以保证原子性
        // 脚本操作返回0或1，key不存在或者值不相等返回0，key存在删除成功返回1，否则返回0
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // 获取订单对应的token
        String token = orderSubmitVo.getUniqueToken();
        // 原子验证和删除令牌
        Long execute = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + user.getId()), token);
        /*
         * 下列操作无法保证原子性
        String token = orderSubmitVo.getUniqueToken();
        String s = stringRedisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + user.getId());
        if (token != null && token.equals(s)) {
            // 令牌验证通过
        }
         */
        if (execute != null && execute == 1L) {
            // 令牌验证成功
            // 创建订单
            OrderCreateTo order = createOrder(orderSubmitVo.getAddrId(), user.getId());
            // 验证价格
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 验价成功后保存到数据库
                saveOrder(order);
                // 库存锁定, 只要有异常就回滚
                // 锁定库存，skuId，count，orderSn
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());

                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLockItems(orderItemVos);
                // 远程锁库存
                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                // 为了保证高并发，库存服务可以自己回滚，可以发送消息给库存服务
                // 库存服务本身也可以使用自动解锁模式，消息队列完成
                if (r.getCode() == 0) {
                    // 库存锁定成功
                    submitOrderResponseVo.setCode(0);
                    submitOrderResponseVo.setOrder(order.getOrder());
                    // TODO 1、库存锁定成功，远程调用回复超时导致订单回滚而库存没有回滚
                    // TODO 2、库存锁定成功，远程调用结束后的后续业务抛出异常导致订单会滚为库存没有回滚
                    // 可以使用分布式事务解决上述问题，也可以使用消息队列回滚相应操作保持最终一致性
                    // 这里使用消息队列的方式
                    // throw new RuntimeException("业务失败异常"); // 模拟后续业务可能会出现异常
                    // 锁定库存成功后将订单数据加入消息队列，如果用户取消订单或者超时未付款则自动取消订单
                    // 方便后续锁库存操作回滚
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                } else {
                    // 锁定库存失败
                    submitOrderResponseVo.setCode(3);
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }
            } else {
                // 金额验证失败
                submitOrderResponseVo.setCode(2);
            }
        } else {
            // 令牌验证失败
            submitOrderResponseVo.setCode(1);
        }
        return submitOrderResponseVo;
    }

    /**
     * 根据订单号查询订单
     * @param orderSn
     * @return
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity order) {
        // 查询当前订单的最新状态
        OrderEntity orderEntity = this.getById(order.getId());
        // 如果订单还是处于未支付状态则取消该订单
        if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
            orderEntity.setStatus(OrderStatusEnum.CANCELED.getCode());
            this.updateById(orderEntity);
            // 可能由于网络波动等原因，导致订单关闭出现在库存解锁操作之后
            // 为了保证锁定的库存能够正常释放，还需要再在关闭订单操作后给库存解锁的交换机发送消息，再次检查一遍库存是否
            // 成功解锁了，如果成功解锁了，则没有必要再解锁；否则，需要重新解锁库存
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    @Override
    public void payOrder(String orderSn) {
        // TODO 支付订单，应该有实际的支付金额操作，但是没有实际实现，仅仅是将订单状态改为已支付状态
        // 更新数据库将订单修改为已支付状态，先查询是不是待支付状态，这个需要确认一下。
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
            int count = orderDao.updateOrderStatusByOrderSn(orderSn, OrderStatusEnum.PAYED.getCode());
            if (count == 1) {
                // 更新订单状态成功后需要从购物车中删除对应商品
                MemberResponseVo user = LoginInterceptor.loginUser.get();
                assert user != null && user.getId() != null;
                // 删除购物车已付款商品
                Integer num = cartFeignService.deletePayedCartItems(user.getId());
                System.out.println("成功从购物出车中删除" + num + "件商品");
                // 远程调用扣减库存，同样可以使用消息队列控制
            } else {
                // 更新订单状态失败
                throw new OrderStatusUpdateFailureException("订单状态更新失败");
            }
        } else {
            throw new OrderTimeOutException("订单信息已过期，请重新下单");
        }
    }

    /**
     * 创建秒杀订单
     * @param order
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo order) {
        // TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        String orderSn = order.getOrderSn();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(order.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = order.getSeckillPrice().multiply(new BigDecimal(order.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);

        // TODO 保存订单项数据
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(orderSn);
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(order.getNum());
        // TODO 设置sku信息的详细信息
        orderItemService.save(orderItemEntity);
    }

    /**
     * 保存订单数据
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        // 保存订单数据
        this.save(orderEntity);
        // 保存订单里的商品项数据
        orderItemService.saveBatch(order.getOrderItems());
    }

    private OrderCreateTo createOrder(Long addrId, Long userId) {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 生成订单号, 创建订单
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(addrId, orderSn);

        // 生成订单项信息
        List<OrderItemEntity> orderItemEntities = buildOrderItems(userId, orderSn);

        // 计算总价格、积分等相关信息
        if (orderItemEntities != null) {
            computePrice(orderEntity, orderItemEntities);
        }

        // 设置属性
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);

        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal sum = new BigDecimal("0");
        BigDecimal coupon = new BigDecimal("0");
        BigDecimal integration = new BigDecimal("0");
        BigDecimal promotion = new BigDecimal("0");
        Integer gift = 0, growth = 0;
        // 订单的总额，即叠加每一项订单物品的金额
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            sum = sum.add(orderItemEntity.getRealAmount());
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            integration = integration.add(orderItemEntity.getIntegrationAmount());
            promotion = promotion.add(orderItemEntity.getPromotionAmount());
            gift += orderItemEntity.getGiftIntegration();
            growth += orderItemEntity.getGiftGrowth();
        }
        // 订单价格相关
        orderEntity.setTotalAmount(sum);
        // 应付总额
        orderEntity.setPayAmount(sum.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        // 积分信息
        orderEntity.setIntegration(gift);
        orderEntity.setGrowth(growth);

        // 其他相关信息
        orderEntity.setDeleteStatus(0); // 未删除状态
    }

    private OrderEntity buildOrder(Long addrId, String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        // 获取收货地址信息
        R fare = wareFeignService.getFare(addrId);
        MemberFareVo data = fare.getData(new TypeReference<MemberFareVo>() {});
        // 设置运费金额以及地址信息
        orderEntity.setMemberId(data.getAddress().getMemberId());
        orderEntity.setFreightAmount(data.getFare());
        orderEntity.setReceiverCity(data.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        orderEntity.setReceiverName(data.getAddress().getName());
        orderEntity.setReceiverPhone(data.getAddress().getPhone());
        orderEntity.setReceiverPostCode(data.getAddress().getPostCode());
        orderEntity.setReceiverProvince(data.getAddress().getProvince());
        orderEntity.setReceiverRegion(data.getAddress().getRegion());

        // 设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        // 订单的自动确认时间
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    /**
     * 构建所有订单项数据
     * @param userId
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(Long userId, String orderSn) {
        // 最后确定每个购物项的价格操作
        List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems(userId);
        if (items != null && !items.isEmpty()) {
            return items.stream()
                    .map(item -> {
                        OrderItemEntity orderItemEntity = buildOrderItem(item);
                        orderItemEntity.setOrderSn(orderSn);
                        return orderItemEntity;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 构建某个订单项数据
     * @param orderItemVo
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo orderItemVo) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 订单信息、订单号
        // 商品spu信息
        Long skuId = orderItemVo.getSkuId();
        R spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = spuInfo.getData(new TypeReference<SpuInfoVo>(){});
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setCategoryId(spuInfoVo.getCatelogId());

        // 商品sku信息
        orderItemEntity.setSkuId(orderItemVo.getSkuId());
        orderItemEntity.setSkuName(orderItemVo.getTitle());
        orderItemEntity.setSkuPic(orderItemVo.getImage());
        orderItemEntity.setSkuPrice(orderItemVo.getPrice());
        orderItemEntity.setSkuQuantity(orderItemVo.getCount());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttrValues(), ";"));
        // 积分信息
        // TODO 也是需要查库，这里同样简单处理
        Integer value = orderItemVo.getPrice().multiply(BigDecimal.valueOf(orderItemVo.getCount())).intValue() / 100;
        orderItemEntity.setGiftGrowth(value);
        orderItemEntity.setGiftIntegration(value);

        // 订单项的价格信息
        // TODO 优惠券信息应该查库，这里简单起见设置为0
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal money = orderItemEntity.getSkuPrice().multiply(BigDecimal.valueOf(orderItemEntity.getSkuQuantity()));
        BigDecimal finalMoney = money.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(finalMoney);
        return orderItemEntity;
    }
}