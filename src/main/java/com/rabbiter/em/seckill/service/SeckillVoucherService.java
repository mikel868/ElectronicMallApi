package com.rabbiter.em.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.shared.cache.RedisIdWorker;
import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.shared.constants.Constants;
import com.rabbiter.em.seckill.entity.SeckillVoucher;
import com.rabbiter.em.seckill.entity.SeckillOrder;
import com.rabbiter.em.seckill.dto.SeckillOrderDTO;
import com.rabbiter.em.shared.exception.ServiceException;
import com.rabbiter.em.seckill.mapper.SeckillVoucherMapper;
import com.rabbiter.em.seckill.mapper.SeckillOrderMapper;
import com.rabbiter.em.seckill.mapper.UserSeckillRecordMapper;
import com.rabbiter.em.shared.util.TokenUtils;
import com.rabbiter.em.shared.util.UserHolder;
import com.rabbiter.em.shared.cache.RedisDistributedLock;
import jakarta.annotation.PostConstruct;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 秒杀券服务实现类
 * 包含完整的订单创建和管理功能
 */
@Service
public class SeckillVoucherService extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> {

    @Resource
    private SeckillVoucherMapper seckillVoucherMapper;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private UserSeckillRecordMapper userSeckillRecordMapper;

    @Resource
    private RedisDistributedLock redisDistributedLock;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

//
//
//
//    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
//    static {
//        SECKILL_SCRIPT = new DefaultRedisScript<>();
//        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
//        SECKILL_SCRIPT.setResultType(Long.class);
//    }
//    private BlockingQueue<SeckillOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
//    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();
//
//    @PostConstruct
//    private void init() {
//        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
//    }
//
//    private class VoucherOrderHandler implements Runnable {
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    // 1.获取队列中的订单信息
//                    SeckillOrder voucherOrder = orderTasks.take();
//                    // 2.创建订单
//                    // TODO: 实现创建订单的业务逻辑
//                    handleVoucherOrder(voucherOrder);
//                }  catch (Exception e) {
//                    // 处理异常，记录日志等
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private Object handleVoucherOrder(SeckillOrder voucherorder) {
//
//
//        Long userId = Long.valueOf(UserHolder.getUser().getId());
//        String lockKey = "seckill:user:" + userId + ":voucherOrder:" + voucherorder;
//
//        // 使用Redisson分布式锁，等待5秒，自动续期
//        return redisDistributedLock.executeWithLock(lockKey, 5, () -> {
//            // 保证事务完成后再释放锁
//            return proxy.CreateOrder(voucherorder);
//        });
//
//    }


    // ======================== 管理员功能 ========================

    /**
     * 创建秒杀券（管理员）
     */
    public boolean createSeckillVoucher(SeckillVoucher seckillVoucher) {
        LocalDateTime now = LocalDateTime.now();
        seckillVoucher.setCreateTime(now);
        seckillVoucher.setUpdateTime(now);

        // 根据时间自动判断状态
        if (now.isBefore(seckillVoucher.getBeginTime())) {
            seckillVoucher.setStatus(0); // 未开始
        } else if (!now.isAfter(seckillVoucher.getEndTime())) {
            seckillVoucher.setStatus(1); // 进行中
        } else {
            seckillVoucher.setStatus(2); // 已结束
        }

        boolean saved = save(seckillVoucher);

        if (saved) {
            // 库存预热到Redis
            warmUpStock(seckillVoucher.getId());
        }

        return saved;
    }

    /**
     * 更新秒杀券（管理员）
     */
    public boolean updateSeckillVoucher(SeckillVoucher seckillVoucher) {
        SeckillVoucher existing = getById(seckillVoucher.getId());
        if (existing == null) {
            throw new ServiceException(Constants.CODE_500, "秒杀券不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        seckillVoucher.setUpdateTime(now);

        // 根据时间自动判断状态
        if (now.isBefore(seckillVoucher.getBeginTime())) {
            seckillVoucher.setStatus(0);
        } else if (!now.isAfter(seckillVoucher.getEndTime())) {
            seckillVoucher.setStatus(1);
        } else {
            seckillVoucher.setStatus(2);
        }

        boolean updated = updateById(seckillVoucher);

        if (updated) {
            // 更新Redis中的库存
            warmUpStock(seckillVoucher.getId());
        }

        return updated;
    }

    /**
     * 删除秒杀券（管理员）
     */
    public boolean deleteSeckillVoucher(Long id) {
        return removeById(id);
    }

    /**
     * 获取所有秒杀券（管理员）
     */
    public Page<SeckillVoucher> getAllSeckillVouchers(int pageNum, int pageSize) {
        Page<SeckillVoucher> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SeckillVoucher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SeckillVoucher::getCreateTime);
        return page(page, queryWrapper);
    }

    // ======================== 用户功能 ========================

    /**
     * 用户查看秒杀券列表（分页，只显示未开始和进行中的）
     */
    public Page<SeckillVoucher> getUserSeckillVoucherList(int pageNum, int pageSize) {
        Page<SeckillVoucher> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SeckillVoucher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SeckillVoucher::getStatus, 0, 1)
                   .orderByAsc(SeckillVoucher::getStatus)   // 进行中的排前面
                   .orderByAsc(SeckillVoucher::getBeginTime);
        return page(page, queryWrapper);
    }

    /**
     * 获取进行中的秒杀券（用户）
     */
    public List<SeckillVoucher> getActiveSeckillVouchers() {
        LambdaQueryWrapper<SeckillVoucher> queryWrapper = new LambdaQueryWrapper<>();
        LocalDateTime now = LocalDateTime.now();
        queryWrapper.eq(SeckillVoucher::getStatus, 1)
                   .le(SeckillVoucher::getBeginTime, now)
                   .ge(SeckillVoucher::getEndTime, now)
                   .apply("stock > 0");
        
        return list(queryWrapper);
    }

    /**
     * 用户抢购秒杀券
     */
//    private SeckillOrderService proxy;
//    public Result seckillPurchase(Long  voucherId) {
//        Long userId = Long.valueOf(UserHolder.getUser().getId());
//
//        System.out.println("=== 秒杀请求调试信息 ===");
//        System.out.println("用户ID: " + userId);
//        System.out.println("秒杀券ID: " + voucherId);
//
//        // 1.执行lua脚本
//        Long result = stringRedisTemplate.execute(
//                SECKILL_SCRIPT,
//                Collections.emptyList(),
//                voucherId.toString(),
//                userId.toString()
//        );
//
//        System.out.println("Lua脚本执行结果: " + result);
//
//        // 2.判断结果是否为0
//        int r = result.intValue();
//        if (r != 0) {
//            // 2.1.不为0,代表没有购买资格
//            String errorMsg = r == 1 ? "库存不足" : "不能重复下单";
//            System.out.println("秒杀失败: " + errorMsg);
//            return Result.fail(errorMsg);
//        }
//
//        // 2.2.为0,有购买资格,把下单信息保存到阻塞队列
//        long orderId = redisIdWorker.nextId("order");
//        System.out.println("生成订单ID: " + orderId);
//        // TODO 保存阻塞队列
//        SeckillOrder order = new SeckillOrder();
//        order.setUserId(userId);
//        order.setVoucherId(voucherId);
//        orderTasks.add( order);
//
//        proxy = (SeckillOrderService) AopContext.currentProxy();
//
//        // 3.返回订单id
//        return Result.ok(orderId);
//    }


//    public  SeckillOrder seckillPurchase(Long  voucherId) {
//
//        // 检查秒杀券信息
//        SeckillVoucher voucher = getById(voucherId);
//        if (voucher == null) {
//            throw new ServiceException(Constants.CODE_500, "秒杀券不存在");
//        }
//
//        // 检查秒杀时间
//        LocalDateTime now = LocalDateTime.now();
//        if (now.isBefore(voucher.getBeginTime()) || now.isAfter(voucher.getEndTime())) {
//            throw new ServiceException(Constants.CODE_500, "秒杀活动已结束");
//        }
//
//        // 检查库存
//        if (voucher.getStock() <= 0) {
//            throw new ServiceException(Constants.CODE_500, "秒杀券已售完");
//        }
//
//
//        // 检查并原子性更新已发放数量（防止超卖）
//        boolean success = lambdaUpdate()
//                .setSql("stock = stock - 1")
//                .eq(SeckillVoucher::getId,seckillOrderService.lambdaQuery().eq(SeckillOrder::getStatus,-1))
//                .apply("stock > 0")
//                .update();
//        if (!success) {
//            throw new ServiceException(Constants.CODE_500, "秒杀券已售完");
//        }
//
//        Long userId = Long.valueOf(UserHolder.getUser().getId());
//        String lockKey = "seckill:user:" + userId + ":voucher:" + voucherId;
//
//        // 使用Redisson分布式锁，等待5秒，自动续期
//        return redisDistributedLock.executeWithLock(lockKey, 5, () -> {
//            // 保证事务完成后再释放锁
//            SeckillVoucherService proxy = (SeckillVoucherService) AopContext.currentProxy();
//            return proxy.CreateOrder(voucherId);
//        });
//    }


//
//    @Transactional
//    public synchronized SeckillOrder CreateOrder(SeckillOrder voucherorder){
//        Long userId= Long.valueOf(UserHolder.getUser().getId());
//
//        // 检查用户是否已经购买过该秒杀券
//        Long count = seckillOrderService.lambdaQuery()
//                .eq(SeckillOrder::getVoucherId, voucherId)
//                .eq(SeckillOrder::getUserId, userId)
//                .count();
//        if(count > 0){
//            throw new ServiceException(Constants.CODE_500, "用户已抢购过该秒杀券");
//        }
//
//        // 检查并原子性更新库存（防止超卖）
//        boolean success = lambdaUpdate()
//                .setSql("stock = stock - 1")
//                .eq(SeckillVoucher::getId, voucherId)
//                .apply("stock > 0")
//                .update();
//        if (!success) {
//            throw new ServiceException(Constants.CODE_500, "秒杀券已售完");
//        }
//
//        // 创建订单
//        SeckillOrder order = new SeckillOrder();
//        order.setUserId(userId);
//        order.setVoucherId(voucherId);
//        order.setStatus(1); // 未支付状态
//        order.setCreateTime(LocalDateTime.now());
//        order.setUpdateTime(LocalDateTime.now());
//
//        // 保存订单
//        seckillOrderService.save(order);
//
//        return order;
//    }

    /**
     * 将指定秒杀券的库存从MySQL同步到Redis（库存预热）
     * 在秒杀券创建时或秒杀开始前调用
     */
    public boolean warmUpStock(Long voucherId) {
        SeckillVoucher voucher = getById(voucherId);
        if (voucher == null) {
            return false;
        }
        String stockKey = "seckill:voucher:" + voucherId;
        stringRedisTemplate.opsForValue().set(stockKey, voucher.getStock().toString());
        return true;
    }

    /**
     * 预热所有活跃秒杀券的库存到Redis
     */
    public void warmUpAllActiveStock() {
        List<SeckillVoucher> activeVouchers = getActiveSeckillVouchers();
        for (SeckillVoucher voucher : activeVouchers) {
            String stockKey = "seckill:voucher:" + voucher.getId();
            stringRedisTemplate.opsForValue().set(stockKey, voucher.getStock().toString());
        }
    }

    /**
     * 获取用户的秒杀优惠券列表（包含优惠券详情）
     */
    public List<SeckillOrderDTO> getUserSeckillVouchers() {
        Long userId = Long.valueOf(UserHolder.getUser().getId());
        return seckillOrderMapper.selectVouchersWithDetailByUserId(userId);
    }





}