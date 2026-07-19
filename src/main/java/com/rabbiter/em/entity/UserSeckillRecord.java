package com.rabbiter.em.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;

/**
 * 用户秒杀券记录实体类
 * 记录成功获取秒杀券的用户信息
 */
@TableName("user_seckill_record")
public class UserSeckillRecord extends Model<UserSeckillRecord> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（外键关联sys_user表）
     */
    private Long userId;

    /**
     * 秒杀券ID（外键关联voucher表）
     */
    private Long seckillId;

    /**
     * 获取时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否已使用：0-未使用，1-已使用
     */
    private Integer isUsed;

    /**
     * 使用时间
     */
    private LocalDateTime usedTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(Long seckillId) {
        this.seckillId = seckillId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
    }

    public LocalDateTime getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(LocalDateTime usedTime) {
        this.usedTime = usedTime;
    }

    @Override
    public String toString() {
        return "UserSeckillRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", seckillId=" + seckillId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isUsed=" + isUsed +
                ", usedTime=" + usedTime +
                '}';
    }
}