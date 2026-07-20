package com.rabbiter.em.order.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.cart.service.CartService;
import com.rabbiter.em.shared.constants.Constants;
import com.rabbiter.em.product.entity.Good;
import com.rabbiter.em.order.entity.Order;
import com.rabbiter.em.order.entity.OrderGoods;
import com.rabbiter.em.order.entity.OrderItem;
import com.rabbiter.em.shared.exception.ServiceException;
import com.rabbiter.em.product.mapper.GoodMapper;
import com.rabbiter.em.order.mapper.OrderGoodsMapper;
import com.rabbiter.em.order.mapper.OrderMapper;
import com.rabbiter.em.product.mapper.StandardMapper;
import com.rabbiter.em.shared.util.TokenUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.rabbiter.em.shared.constants.RedisConstants.GOOD_TOKEN_KEY;

@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderGoodsMapper orderGoodsMapper;
    @Resource
    private StandardMapper standardMapper;
    @Resource
    private GoodMapper goodMapper;
    @Resource
    private CartService cartService;
    @Resource
    private RedisTemplate<String, Good> redisTemplate;

    @Transactional
    public String saveOrder(Order order) {
        order.setUserId(TokenUtils.getCurrentUser().getId());
        String orderNo = DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6);
        order.setOrderNo(orderNo);
        order.setCreateTime(DateUtil.now());
        orderMapper.insert(order);

        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setOrderId(order.getId());
        //遍历order里携带的goods数组，并用orderItem对象来接收
        String goods = order.getGoods();
        List<OrderItem> orderItems = JSON.parseArray(goods, OrderItem.class);
        for (OrderItem orderItem : orderItems) {
            long good_id = orderItem.getId();
            String standard = orderItem.getStandard();
            int num = orderItem.getNum();
            orderGoods.setGoodId(good_id);
            orderGoods.setCount(num);
            orderGoods.setStandard(standard);
            //插入到order_good表
            orderGoodsMapper.insert(orderGoods);
        }
        // 清除购物车
        cartService.removeById(order.getCartId());
        return orderNo;
    }

    //给订单付款
    @Transactional
    public void payOrder(String orderNo) {
        //更改状态为代付款
        orderMapper.payOrder(orderNo);
        //给对应规格减库存
        Map<String, Object> orderMap = orderMapper.selectByOrderNo(orderNo);
        int count = (int) orderMap.get("count");
        Object goodIdObj = orderMap.get("goodId");
        Long goodId = null;
        if(goodIdObj instanceof Long) {
            goodId = (Long) goodIdObj;
        } else if(goodIdObj != null) {
            try {
                goodId = Long.parseLong(goodIdObj.toString());
            } catch (NumberFormatException e) {
                throw new ServiceException(Constants.CODE_500, "商品ID不正确");
            }
        }

        if(goodId == null) {
            throw new ServiceException(Constants.CODE_500, "商品ID不存在");
        }
        String standard = (String) orderMap.get("standard");
        int store = standardMapper.getStore(goodId, standard);
        if (store < count) {
            throw new ServiceException(Constants.CODE_500, "库存不足");
        }
        standardMapper.deductStore(goodId, standard, store - count);

        //给对应商品加销量和销售额
//        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        orderLambdaQueryWrapper.eq(Order::getOrderNo, orderNo);
//        Order one = getOne(orderLambdaQueryWrapper);
//        BigDecimal totalPrice = one.getTotalPrice();
//        goodMapper.saleGood(goodId, count, totalPrice);
        Order one = lambdaQuery().eq(Order::getOrderNo, orderNo).one();
        BigDecimal totalPrice = one.getTotalPrice();
        goodMapper.saleGood(goodId, count, totalPrice);

        // redis 增销量
        String redisKey = GOOD_TOKEN_KEY + goodId;
        ValueOperations<String, Good> valueOperations = redisTemplate.opsForValue();
        Good good = valueOperations.get(redisKey);
        if(!ObjectUtils.isEmpty(good)) {
            good.setSales(good.getSales() + count);
            valueOperations.set(redisKey, good);
        }
    }

    public List<Map<String, Object>> selectByUserId(int userId) {

        return orderMapper.selectByUserId(userId);
    }

    public boolean receiveOrder(String orderNo) {
        return orderMapper.receiveOrder(orderNo);
    }

    public Map<String, Object> selectByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    public void delivery(String orderNo) {
//        LambdaUpdateWrapper<Order> orderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
//        orderLambdaUpdateWrapper.eq(Order::getOrderNo, orderNo)
//                .set(Order::getState, "已发货");
//        update(orderLambdaUpdateWrapper);

        lambdaUpdate().eq(Order::getOrderNo, orderNo).set(Order::getState, "已发货").update();

    }
}
