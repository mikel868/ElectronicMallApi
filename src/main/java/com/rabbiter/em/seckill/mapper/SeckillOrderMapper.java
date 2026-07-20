package com.rabbiter.em.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.em.seckill.dto.SeckillOrderDTO;
import com.rabbiter.em.seckill.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 秒杀订单Mapper接口
 */
@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {

    /**
     * 根据用户ID和秒杀券ID查询订单（防重复购买）
     */
    @Select("SELECT * FROM seckill_order WHERE user_id = #{userId} AND voucher_id = #{voucherId}")
    SeckillOrder selectByUserAndSeckill(@Param("userId") Long userId, @Param("voucherId") Long voucherId);

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM seckill_order WHERE order_no = #{orderNo}")
    SeckillOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据用户ID查询订单列表
     */
    List<SeckillOrder> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据秒杀券ID查询订单列表（统计用）
     */
    List<SeckillOrder> selectByVoucherId(@Param("voucherId") Long voucherId);

    /**
     * 更新订单状态
     */
    @Update("UPDATE seckill_order SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateOrderStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新支付信息
     */
    @Update("UPDATE seckill_order SET status = 1, payment_method = #{paymentMethod}, pay_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int updatePaymentInfo(@Param("id") Long id, @Param("paymentMethod") Integer paymentMethod);

    /**
     * 更新发货信息
     */
    @Update("UPDATE seckill_order SET status = 2, ship_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int updateShipInfo(@Param("id") Long id);

    /**
     * 完成订单
     */
    @Update("UPDATE seckill_order SET status = 3, complete_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int completeOrder(@Param("id") Long id);

    /**
     * 查询用户的秒杀优惠券列表（包含优惠券详情）
     */
    @Select("SELECT o.id, o.user_id, o.voucher_id, o.pay_type, o.status, o.create_time, o.pay_time, " +
            "v.voucher_amount, v.min_amount, v.status as voucher_status " +
            "FROM seckill_order o LEFT JOIN voucher v ON o.voucher_id = v.id " +
            "WHERE o.user_id = #{userId} AND o.status = 1 ORDER BY o.create_time DESC")
    List<SeckillOrderDTO> selectVouchersWithDetailByUserId(@Param("userId") Long userId);

}