package com.rabbiter.em.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbiter.em.product.entity.Icon;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.product.entity.IconCategory;
import com.rabbiter.em.product.mapper.IconCategoryMapper;
import com.rabbiter.em.product.mapper.IconMapper;
import com.rabbiter.em.shared.util.BaseApi;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class IconService extends ServiceImpl<IconMapper, Icon> {

    @Resource
    private IconMapper iconMapper;

    @Resource
    private IconCategoryMapper iconCategoryMapper;

    public List<Icon> getIconCategoryMapList() {
        return iconMapper.getIconCategoryMapList();
    }

    /**
     * 删除上级分类
     *
     * @param id id
     */
    public Map<String, Object> deleteById(Long id) {
        // 检查是否包含下级分类
        Long count = iconCategoryMapper.selectCount(
                new QueryWrapper<IconCategory>().eq("icon_id", id)
        );
        if (count > 0) {
            return BaseApi.error("该上级分类存在下级分类，请删除所有下级分类再尝试删除");
        }
        super.removeById(id);
        return BaseApi.success();
    }
}
