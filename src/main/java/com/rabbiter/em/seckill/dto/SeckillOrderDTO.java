package com.rabbiter.em.seckill.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单DTO（包含优惠券信息）
 */
public class SeckillOrderDTO {

    // 订单信息
    private Long id;
    private Long userId;
    private Long voucherId;
    private Integer payType;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;

    // 优惠券信息
    private BigDecimal voucherAmount;
    private BigDecimal minAmount;
    private String voucherStatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }

    public Integer getPayType() { return payType; }
    public void setPayType(Integer payType) { this.payType = payType; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }

    public BigDecimal getVoucherAmount() { return voucherAmount; }
    public void setVoucherAmount(BigDecimal voucherAmount) { this.voucherAmount = voucherAmount; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public String getVoucherStatus() { return voucherStatus; }
    public void setVoucherStatus(String voucherStatus) { this.voucherStatus = voucherStatus; }
}
