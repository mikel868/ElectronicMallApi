package com.rabbiter.em.seckill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.em.shared.annotation.Authority;
import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.system.entity.AuthorityType;
import com.rabbiter.em.seckill.entity.SeckillVoucher;
import com.rabbiter.em.seckill.entity.SeckillOrder;
import com.rabbiter.em.seckill.dto.SeckillOrderDTO;
import com.rabbiter.em.seckill.service.SeckillOrderService;
import com.rabbiter.em.seckill.service.SeckillVoucherService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 秒杀券控制器
 * 包含完整的订单创建和管理功能
 */
@RestController
@RequestMapping("/api/seckill")
public class SeckillVoucherController {

    @Resource
    private SeckillVoucherService seckillVoucherService;
    @Autowired
    private SeckillOrderService seckillOrderService;


    // ======================== 管理员接口 ========================

    /**
     * 创建秒杀券
     */
    @Authority(AuthorityType.requireAuthority)
    @PostMapping("/admin")
    public Result createSeckillVoucher(@RequestBody SeckillVoucher seckillVoucher) {
        boolean result = seckillVoucherService.createSeckillVoucher(seckillVoucher);
        if (result) {
            return Result.success("创建秒杀券成功");
        } else {
            return Result.error("500", "创建秒杀券失败");
        }
    }

    /**
     * 更新秒杀券
     */
    @Authority(AuthorityType.requireAuthority)
    @PutMapping("/admin")
    public Result updateSeckillVoucher(@RequestBody SeckillVoucher seckillVoucher) {
        boolean result = seckillVoucherService.updateSeckillVoucher(seckillVoucher);
        if (result) {
            return Result.success("更新秒杀券成功");
        } else {
            return Result.error("500", "更新秒杀券失败");
        }
    }

    /**
     * 删除秒杀券
     */
    @Authority(AuthorityType.requireAuthority)
    @DeleteMapping("/admin/{id}")
    public Result deleteSeckillVoucher(@PathVariable Long id) {
        boolean result = seckillVoucherService.deleteSeckillVoucher(id);
        if (result) {
            return Result.success("删除秒杀券成功");
        } else {
            return Result.error("500", "删除秒杀券失败");
        }
    }

    /**
     * 查看所有秒杀券状态（分页）
     */

    @GetMapping("/admin/page")
    public Result getAllSeckillVouchers(@RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        Page<SeckillVoucher> page = seckillVoucherService.getAllSeckillVouchers(pageNum, pageSize);
        return Result.success(page);
    }

    // ======================== 用户接口 ========================

    /**
     * 用户查看秒杀券列表（分页，包含未开始和进行中的）
     */
    @Authority(AuthorityType.noRequire)
    @GetMapping("/list")
    public Result getUserSeckillVoucherList(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        Page<SeckillVoucher> page = seckillVoucherService.getUserSeckillVoucherList(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取进行中的秒杀券列表
     */
    @GetMapping("/active")
    public Result getActiveSeckillVouchers() {
        List<SeckillVoucher> vouchers = seckillVoucherService.getActiveSeckillVouchers();
        return Result.success(vouchers);
    }

    /**
     * 用户秒杀抢购（获得优惠券）
     */

    @Authority(AuthorityType.requireLogin)
    @PostMapping("/purchase/{voucherId}")
    public Result seckillPurchase(@PathVariable Long voucherId) {
        try {
            Result result = seckillOrderService.seckillPurchase(voucherId);
            return result;
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    /**
     * 获取用户的秒杀优惠券列表（已获得但未使用）
     */
    @Authority(AuthorityType.requireLogin)
    @GetMapping("/my-vouchers")
    public Result getUserSeckillVouchers() {
        try {
            List<SeckillOrderDTO> vouchers = seckillVoucherService.getUserSeckillVouchers();
            return Result.success(vouchers);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }


}