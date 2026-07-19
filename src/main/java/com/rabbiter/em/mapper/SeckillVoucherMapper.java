package com.rabbiter.em.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.em.entity.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 秒杀商品Mapper接口
 */
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {

    /**
     * 减少库存（用户购买时调用）
     *
     * @param seckillId 秒杀商品ID
     * @param count     减少数量
     * @return 影响行数
     */
    @Update("UPDATE voucher SET stock = stock - #{count} " +
            "WHERE id = #{seckillId} AND stock >= #{count}")
    int decreaseStock(@Param("seckillId") Long seckillId, @Param("count") Integer count);

    /**
     * 查询进行中的秒杀商品（用户界面显示）
     *
     * @return 秒杀商品列表
     */
    List<SeckillVoucher> selectActiveSeckills();

    /**
     * 根据用户ID查询用户可用的秒杀券（通过订单表关联）
     *
     * @param userId 用户ID
     * @return 用户的秒杀券列表
     */
    @Select("SELECT sv.* FROM voucher sv " +
            "INNER JOIN user_seckill_record usr ON sv.id = usr.seckill_id " +
            "WHERE usr.user_id = #{userId} AND usr.is_used = 0 " +
            "ORDER BY usr.create_time DESC")
    List<SeckillVoucher> selectVouchersByUserId(@Param("userId") Long userId);

}
