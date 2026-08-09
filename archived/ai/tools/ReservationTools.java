package com.rabbiter.em.ai.tools;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.rabbiter.em.shared.constants.Constants;
import com.rabbiter.em.shared.constants.RedisConstants;
import com.rabbiter.em.user.entity.User;
import com.rabbiter.em.product.entity.Good;
import com.rabbiter.em.product.entity.GoodStandard;
import com.rabbiter.em.ai.entity.AiOrder;
import com.rabbiter.em.order.entity.Order;
import com.rabbiter.em.order.entity.OrderGoods;
import com.rabbiter.em.shared.exception.ServiceException;
import com.rabbiter.em.product.mapper.GoodMapper;
import com.rabbiter.em.order.mapper.OrderGoodsMapper;
import com.rabbiter.em.order.mapper.OrderMapper;
import com.rabbiter.em.product.mapper.StandardMapper;
import com.rabbiter.em.ai.service.AiOrderService;
import com.rabbiter.em.product.service.GoodService;
import com.rabbiter.em.order.service.OrderService;
import com.rabbiter.em.shared.util.TokenUtils;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.rabbiter.em.shared.constants.RedisConstants.GOOD_TOKEN_KEY;

@Component
public class ReservationTools {

    @Resource
    private AiOrderService aiOrderService;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderGoodsMapper orderGoodsMapper;
    @Resource
    private GoodService goodService;
    @Resource
    private RedisTemplate<String, User> redisTemplate;
    @Resource
    private StandardMapper standardMapper;
    @Resource
    private GoodMapper goodMapper;
    @Resource
    private OrderService orderService;
    private static final String USER_TOKEN_KEY = "user:token:";
    
    // 添加一个成员变量来存储用户ID
    private Long currentUserId;

    // 新增一个设置用户ID的方法，供外部调用
    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
    }

    // 获取当前用户ID的辅助方法
    private Long getCurrentUserId() {
        // 首先尝试从成员变量获取
        if (currentUserId != null) {
            return currentUserId;
        }
        
        // 如果成员变量为空，尝试从ThreadLocal获取
        try {
            User user = TokenUtils.getCurrentUser();
            if (user != null) {
                return Long.valueOf(user.getId());
            }
        } catch (Exception e) {
            // 忽略异常
        }
        
        // 如果都获取不到，抛出异常
        throw new ServiceException(Constants.CODE_401, "用户未登录");
    }

    @Tool("了解商品详情")
    public String getGoodsDetails(@P("商品名") String name) {
        //根据商品名查询商品详情
        return goodService.selectByName(name);
    }

    @Tool("查询商城在售手机库存")
    public String searchGoodsInStock(@P("商品名关键字，为空则查询全部") String keyword) {
        List<Good> goods;
        if (keyword != null && !keyword.trim().isEmpty()) {
            goods = goodService.lambdaQuery()
                    .like(Good::getName, keyword)
                    .eq(Good::getIsDelete, false)
                    .list();
        } else {
            goods = goodService.lambdaQuery()
                    .eq(Good::getIsDelete, false)
                    .list();
        }

        if (goods.isEmpty()) {
            return "商城中暂无" + (keyword != null && !keyword.isEmpty() ? "包含「" + keyword + "」的" : "") + "在售商品";
        }

        StringBuilder sb = new StringBuilder();
        for (Good good : goods) {
            List<GoodStandard> standards = goodMapper.getStandardById(Math.toIntExact(good.getId()));
            // 筛选有库存的规格
            StringBuilder inStock = new StringBuilder();
            StringBuilder outOfStock = new StringBuilder();
            for (GoodStandard s : standards) {
                if (s.getStore() > 0) {
                    inStock.append(String.format("  %s，价格：%.2f，库存：%d件\n", s.getValue(), s.getPrice(), s.getStore()));
                } else {
                    outOfStock.append(String.format("  %s，价格：%.2f，已售罄\n", s.getValue(), s.getPrice()));
                }
            }
            sb.append(String.format("【%s】折扣：%.1f折\n", good.getName(), good.getDiscount() * 10));
            if (inStock.length() > 0) {
                sb.append("有库存规格：\n").append(inStock);
            }
            if (outOfStock.length() > 0) {
                sb.append("已售罄规格：\n").append(outOfStock);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    //1.工具方法:添加预约信息
    @Tool("预约下单服务")
    public void addReservation(
            @P("商品名") String name,
            @P("数量") int num,
            @P("规格") String standard,
            @P("联系人") String linkUser,
            @P("联系电话") String linkPhone,
            @P("送货地址") String linkAddress

    ) {
        goodService.selectByName(name);
        // 从Redis中查找当前用户的token和信息
        long userId = getCurrentUserId();
        System.out.println("当前用户ID: " + userId);
        System.out.println("当前用户名称"+name);
        System.out.println("当前用户规格"+standard);
        // 使用从Redis中获取的userId
        AiOrder aiOrder = new AiOrder();
        aiOrder.setName(name);
        aiOrder.setNum(num);
        aiOrder.setStandard(standard);
        aiOrder.setLinkUser(linkUser);
        aiOrder.setLinkPhone(linkPhone);
        aiOrder.setLinkAddress(linkAddress);
        aiOrder.setUserId(userId);
        aiOrderService.save(aiOrder);

        //插入到order表
        Order order = new Order();
        String orderNo = DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6);
        order.setOrderNo(orderNo);
        order.setUserId(Math.toIntExact(userId));
        order.setCreateTime(DateUtil.now());
        order.setState("待付款");

        Good good =goodService.lambdaQuery()
                .eq(Good::getName, name)
                .eq(Good::getIsDelete, false)
                .one();
        Long id = good.getId();
        BigDecimal price = goodMapper.getStandardPriceByNameAndId(standard,id);

        order.setTotalPrice(price.multiply(new BigDecimal(num)));
        order.setLinkAddress(linkAddress);
        order.setLinkPhone(linkPhone);
        order.setLinkUser(linkUser);
        orderMapper.insert(order);
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setOrderId(order.getId());

        orderGoods.setGoodId(id);
        orderGoods.setCount(num);
        orderGoods.setStandard(standard);



            //插入到order_good表
        orderGoodsMapper.insert(orderGoods);



    }

    //2.工具方法:查询预约信息
    @Tool("查询预约订单")
    public List<AiOrder> findReservation() {
        // 从Redis中查找当前用户的token和信息
        long userId = getCurrentUserId();
        
        // 根据用户ID查询预约订单
        return aiOrderService.findByUserId(userId);
    }
    

}