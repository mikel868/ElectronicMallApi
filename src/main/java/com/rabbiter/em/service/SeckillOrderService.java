package com.rabbiter.em.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.common.RedisDistributedLock;
import com.rabbiter.em.common.RedisIdWorker;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.SeckillOrder;
import com.rabbiter.em.entity.SeckillVoucher;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.SeckillOrderMapper;
import com.rabbiter.em.utils.TokenUtils;
import com.rabbiter.em.utils.UserHolder;
import com.rabbiter.em.entity.UserSeckillRecord;
import com.rabbiter.em.mapper.UserSeckillRecordMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SeckillOrderService extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ApplicationContextAware {


    @Resource
    private RedisDistributedLock redisDistributedLock;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private SeckillOrderMapper  seckillOrderMapper;

    
    // 添加ApplicationContext字段
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

     @PostConstruct
    private void init() {
        // 确保Redis Stream和消费者组存在
        try {
            stringRedisTemplate.opsForStream().createGroup("stream.orders", "g1");
        } catch (Exception e) {
            // Stream或消费者组已存在，忽略
        }
        SECKILL_ORDER_EXECUTOR.submit(new SeckillOrderService.VoucherOrderHandler());
    }

    // 添加手动启动秒杀订单处理的方法


    private class VoucherOrderHandler implements Runnable {
        String queueName = "stream.orders";
        @Override
        public void run() {
            while (true) {
                try {
                    // 1. 从消息队列中获取订单信息
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),  // 消费者组g1，消费者c1
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),  // 每次1条，阻塞2秒
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())  // 从上次消费的位置开始
                    );

                    if (list == null || list.isEmpty()) {
                        // 如果获取失败，说明没有消息，继续下一次循环
                        continue;
                    }

                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    SeckillOrder voucherOrder = BeanUtil.mapToBean(values, SeckillOrder.class, false, null);
                    System.out.println("反序列化订单: voucherId=" + voucherOrder.getVoucherId() + ", userId=" + voucherOrder.getUserId());

                    handleVoucherOrder(voucherOrder);
                    stringRedisTemplate.opsForStream().acknowledge("g1", record);
                }  catch (Exception e) {
                    // 处理异常，记录日志等
                    e.printStackTrace();
                    handlePendingListOrder();
                }
            }
        }

        private void handlePendingListOrder() {
            while (true) {
                try {
                    // 1. 从pending list中获取未确认的消息
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),  // 消费者组g1，消费者c1
                            StreamReadOptions.empty().count(1), // 每次1条
                            StreamOffset.create(queueName, ReadOffset.from("0"))  // 从头开始读取pending list
                    );

                    if (list == null || list.isEmpty()) {
                        // 如果没有pending消息，跳出循环
                        break;
                    }

                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    SeckillOrder voucherOrder = BeanUtil.mapToBean(values, SeckillOrder.class, false, null);

                    handleVoucherOrder(voucherOrder);
                    stringRedisTemplate.opsForStream().acknowledge("g1", record);
                }  catch (Exception e) {
                    // 处理异常，记录日志等
                    e.printStackTrace();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    private SeckillOrder handleVoucherOrder(SeckillOrder voucherorder) {
        Long userId = voucherorder.getUserId();
        String lockKey = "seckill:user:" + userId + ":voucherOrder:" + voucherorder.getVoucherId();

        // 使用Redisson分布式锁，等待5秒，自动续期
        return redisDistributedLock.executeWithLock(lockKey, 5, () -> {
            // 保证事务完成后再释放锁
            // 直接通过ApplicationContext获取代理对象
            try {
                SeckillOrderService appContextProxy = applicationContext.getBean(SeckillOrderService.class);
                System.out.println("通过ApplicationContext成功获取到代理对象: " + appContextProxy.getClass().getName());
                return appContextProxy.CreateOrder(voucherorder);
            } catch (ServiceException e) {
                // 业务异常直接抛出（如库存不足等）
                System.out.println("业务异常: " + e.getMessage());
                throw e;
            }
        });

    }
    

    private SeckillOrderService proxy;
    public Result seckillPurchase(Long  voucherId) {
        Long userId = Long.valueOf(UserHolder.getUser().getId());
        Long orderId = redisIdWorker.nextId("order");
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString(),
                String.valueOf(orderId)
        );

        System.out.println("Lua脚本执行结果: " + result);

        // 2.判断结果是否为0
        int r = result.intValue();
        if (r != 0) {
            // 2.1.不为0,代表没有购买资格
            String errorMsg = r == 1 ? "库存不足" : "不能重复下单";
            System.out.println("秒杀失败: " + errorMsg);
            return Result.fail(errorMsg);
        }

        // 2.2.为0,有购买资格,把下单信息保存到阻塞队列
        // 不在主线程中获取代理对象，异步处理时会通过applicationContext.getBean()获取

        // 3.返回订单id
        return Result.ok(orderId);
    }
    @Transactional
    public SeckillOrder CreateOrder(SeckillOrder voucherorder){
        System.out.println("CreateOrder收到订单: voucherId=" + voucherorder.getVoucherId() + ", userId=" + voucherorder.getUserId() + ", id=" + voucherorder.getId());
        Long userId= voucherorder.getUserId(); ;

        // 检查用户是否已经购买过该秒杀券
        Long count = lambdaQuery()
                .eq(SeckillOrder::getVoucherId, voucherorder.getVoucherId())
                .eq(SeckillOrder::getUserId, userId)
                .count();

        if(count > 0){
            System.out.println("用户已抢购过该秒杀券");
            throw new ServiceException(Constants.CODE_500, "用户已抢购过该秒杀券");
        }


        SeckillVoucher voucher = seckillVoucherService.getById(voucherorder.getVoucherId());
        if (voucher != null) {
            System.out.println("数据库中秒杀券信息: ID=" + voucher.getId() + ", 库存=" + voucher.getStock());
            
            // 检查Redis中的库存
            String redisStockKey = "seckill:voucher:" + voucher.getId();
            String redisStock = stringRedisTemplate.opsForValue().get(redisStockKey);
            System.out.println("Redis中秒杀券库存: " + redisStockKey + " = " + redisStock);
        } else {
            System.out.println("未找到秒杀券信息");
            throw new ServiceException(Constants.CODE_500, "秒杀券不存在");
        }
        
        // 直接扣减库存（Lua脚本已保证原子性，这里只需要执行一次）
        System.out.println("开始库存扣减操作");
        boolean success = seckillVoucherService.lambdaUpdate()
                .setSql("stock = stock - 1")
                .eq(SeckillVoucher::getId, voucherorder.getVoucherId())
                .apply("stock > 0")
                .update();
        System.out.println("库存扣减结果: " + success);
        
        if (!success) {
            throw new ServiceException(Constants.CODE_500, "秒杀券已售完");
        } else {
            System.out.println("库存扣减成功");
        }
        // 保存订单
        System.out.println("保存订单");
        voucherorder.setStatus(1); // 1-未支付
        save(voucherorder);

        // 返回创建的订单
        System.out.println("订单处理完成");
        return voucherorder;
    }



    /**
     * 获取用户秒杀订单列表
     */
    public List<SeckillOrder> getUserSeckillOrders() {
        Long userId = Long.valueOf(TokenUtils.getCurrentUser().getId());
        return seckillOrderMapper.selectByUserId(userId);
    }

    /**
     * 根据订单号查询订单
     */
    public SeckillOrder getSeckillOrderByOrderNo(String orderNo) {
        return seckillOrderMapper.selectByOrderNo(orderNo);
    }

    /**
     * 更新订单状态
     */
    public boolean updateOrderStatus(Long orderId, Integer status) {
        return seckillOrderMapper.updateOrderStatus(orderId, status) > 0;
    }

    /**
     * 支付订单
     */
    @Transactional
    public boolean payOrder(Long orderId, Integer paymentMethod) {
        return seckillOrderMapper.updatePaymentInfo(orderId, paymentMethod) > 0;
    }

    /**
     * 发货
     */
    @Transactional
    public boolean shipOrder(Long orderId) {
        return seckillOrderMapper.updateShipInfo(orderId) > 0;
    }

    /**
     * 完成订单
     */
    @Transactional
    public boolean completeOrder(Long orderId) {
        return seckillOrderMapper.completeOrder(orderId) > 0;
    }

}
