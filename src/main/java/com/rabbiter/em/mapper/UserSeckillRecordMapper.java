package com.rabbiter.em.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.em.entity.UserSeckillRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户秒杀券记录Mapper接口
 */
@Mapper
public interface UserSeckillRecordMapper extends BaseMapper<UserSeckillRecord> {

    /**
     * 查询用户未使用的秒杀券记录
     */
    @Select("SELECT * FROM user_seckill_record WHERE user_id = #{userId} AND is_used = 0 ORDER BY create_time DESC")
    List<UserSeckillRecord> selectUnusedRecordsByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已经获取过某个秒杀券
     */
    @Select("SELECT COUNT(*) FROM user_seckill_record WHERE user_id = #{userId} AND seckill_id = #{seckillId}")
    int countByUserIdAndSeckillId(@Param("userId") Long userId, @Param("seckillId") Long seckillId);

    /**
     * 更新秒杀券使用状态
     */
    @Update("UPDATE user_seckill_record SET is_used = 1, used_time = NOW(), update_time = NOW() " +
            "WHERE user_id = #{userId} AND seckill_id = #{seckillId} AND is_used = 0")
    int updateUsedStatus(@Param("userId") Long userId, @Param("seckillId") Long seckillId);
}