package com.rabbiter.em.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.Good;
import com.rabbiter.em.entity.GoodStandard;
import com.rabbiter.em.entity.dto.GoodDTO;
import com.rabbiter.em.entity.dto.GoodDocument;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.GoodMapper;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.rabbiter.em.constants.RedisConstants.GOOD_TOKEN_KEY;
import static com.rabbiter.em.constants.RedisConstants.GOOD_TOKEN_TTL;

@Service
public class GoodService extends ServiceImpl<GoodMapper, Good> {

    @Resource
    private GoodMapper goodMapper;
    @Resource
    private RedisTemplate<String, Good> redisTemplate;
    @Resource
    private com.rabbiter.em.common.CacheClient cacheClient;
    @Resource
    private ElasticsearchService elasticsearchService;

    //查询一个商品的信息
    public Good getGoodById(Long id) {
        String redisKey = GOOD_TOKEN_KEY + id;
        Good data = cacheClient.get(
                redisKey,
                Good.class,
                GOOD_TOKEN_TTL,
                key -> lambdaQuery().eq(Good::getId, id).eq(Good::getIsDelete, false).one()
        );
        if (data == null) {
            throw new ServiceException(Constants.NO_RESULT, "无结果");
        }
        return data;
    }

    //查询商品的规格
    public String getStandard(int id) {
        List<GoodStandard> standards = goodMapper.getStandardById(id);
        if (standards.size() == 0) {
            throw new ServiceException(Constants.NO_RESULT, "无结果");
        }
        return JSON.toJSONString(standards);
    }


    public BigDecimal  getStandardPrice(String name,Long id) {
        BigDecimal price = goodMapper.getStandardPriceByNameAndId(name, id);
        // 2. 处理null场景（根据业务需求定义默认值或抛异常）

        // 3. 安全拆箱返回
        return price;
    }

    //查询某商品的最低规格价
    public BigDecimal getMinPrice(Long id) {
        return goodMapper.getMinPrice(id);
    }

    //查询全部（首页推荐商品）
    public List<GoodDTO> findFrontGoods() {
        return goodMapper.findFrontGoods();
    }


    //假删除
    public void deleteGood(Long id) {
        cacheClient.invalidate(GOOD_TOKEN_KEY + id);
        goodMapper.goodDelete(id);
        // 同步删除 Elasticsearch 中的商品
        elasticsearchService.deleteGoodDocument(id);
        new Thread(() -> {
            try {
                Thread.sleep(80);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            cacheClient.invalidate(GOOD_TOKEN_KEY + id);
        }).start();
    }

    //保存商品信息
    public Long saveOrUpdateGood(Good good) {
        System.out.println(good);
        if (good.getId() == null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            good.setCreateTime(df.format(LocalDateTime.now()));
            goodMapper.insertGood(good);
        } else {
            saveOrUpdate(good);
            cacheClient.invalidate(GOOD_TOKEN_KEY + good.getId());
            new Thread(() -> {
                try {
                    Thread.sleep(80);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                cacheClient.invalidate(GOOD_TOKEN_KEY + good.getId());
            }).start();
        }
        
        // 同步到 Elasticsearch
        GoodDocument document = elasticsearchService.convertToDocument(good);
        elasticsearchService.saveGoodDocument(document);
        
        return good.getId();
    }

    public boolean setRecommend(Long id, Boolean isRecommend) {
//        LambdaUpdateWrapper<Good> goodsLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
//        goodsLambdaUpdateWrapper.eq(Good::getId,id)
//                .set(Good::getRecommend,isRecommend);

        return lambdaUpdate()
                .eq(Good::getId, id)
                .set(Good::getRecommend, isRecommend)
                .update();
    }

    public List<Good> getSaleRank(int num) {
        return goodMapper.getSaleRank(num);
    }


    public void update(Good good) {
        updateById(good);
        cacheClient.invalidate(GOOD_TOKEN_KEY + good.getId());
        // 同步更新到 Elasticsearch
        GoodDocument document = elasticsearchService.convertToDocument(good);
        elasticsearchService.saveGoodDocument(document);
        new Thread(() -> {
            try {
                Thread.sleep(80);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            cacheClient.invalidate(GOOD_TOKEN_KEY + good.getId());
        }).start();
    }

    //分页查询
    public IPage<GoodDTO> findPage(Integer pageNum, Integer pageSize, String searchText, Integer categoryId) {
//        // 如果有搜索文本，优先使用 Elasticsearch 搜索
//        if (StrUtil.isNotBlank(searchText)) {
//            return searchGoodsWithElasticsearch(pageNum, pageSize, searchText, categoryId);
//        }
        
        LambdaQueryWrapper<Good> query = Wrappers.<Good>lambdaQuery().orderByDesc(Good::getId);
        //对名称和描述进行模糊查询
        if (StrUtil.isNotBlank(searchText)) {
            query.like(Good::getName, searchText).or().like(Good::getDescription, searchText).or().eq(Good::getId, searchText);
        }
        if (categoryId != null) {
            query.eq(Good::getCategoryId, categoryId);
        }
        //筛除掉已被删除的商品
        query.eq(Good::getIsDelete, false);
        IPage<Good> page = this.page(new Page<>(pageNum, pageSize), query);
        //把good转为dto
        IPage<GoodDTO> goodDTOPage = page.convert(good -> {
            GoodDTO goodDTO = new GoodDTO();
            BeanUtil.copyProperties(good, goodDTO);
            return goodDTO;
        });
        for (GoodDTO good : goodDTOPage.getRecords()) {
            //附上最低价格
            good.setPrice(getMinPrice(good.getId()));
        }
        return goodDTOPage;
    }

    public IPage<Good> findFullPage(Integer pageNum, Integer pageSize, String searchText, Integer categoryId) {
        LambdaQueryWrapper<Good> query = Wrappers.<Good>lambdaQuery().orderByDesc(Good::getId);
        //对名称和描述进行模糊查询
        if (StrUtil.isNotBlank(searchText)) {
            query.like(Good::getName, searchText).or().like(Good::getDescription, searchText).or().eq(Good::getId, searchText);
        }
        if (categoryId != null) {
            query.eq(Good::getCategoryId, categoryId);
        }
        //筛除掉已被删除的商品
        query.eq(Good::getIsDelete, false);
        IPage<Good> page = this.page(new Page<>(pageNum, pageSize), query);
        for (Good good : page.getRecords()) {
            //附上最低价格
            good.setPrice(getMinPrice(good.getId()));
        }
        return page;
    }


    /**
     * 使用 Elasticsearch 搜索商品
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param searchText 搜索文本
     * @param categoryId 分类ID
     * @return 商品分页结果
     */
    public IPage<GoodDTO> searchGoodsWithElasticsearch(Integer pageNum, Integer pageSize, String searchText, Integer categoryId) {
        try {

            // 使用 Elasticsearch 进行搜索
            List<GoodDocument> documents;
            if (categoryId != null) {
                documents = elasticsearchService.searchGoodsByCategory(categoryId.longValue(), (pageNum - 1) * pageSize, pageSize);
            } else {
                documents = elasticsearchService.searchGoods(searchText, (pageNum - 1) * pageSize, pageSize);
            }
            
            // 转换为 Good 实体
            List<Good> goods = documents.stream()
                    .map(document -> {
                        Good good = new Good();
                        good.setId(document.getId());
                        good.setName(document.getName());
                        good.setDescription(document.getDescription());
                        good.setDiscount(document.getDiscount());
                        good.setSales(document.getSales());
                        good.setSaleMoney(document.getSaleMoney());
                        good.setCategoryId(document.getCategoryId());
                        good.setImgs(document.getImgs());
                        good.setRecommend(document.getRecommend());
                        good.setCreateTime(document.getCreateTime());
                        return good;
                    })
                    .collect(Collectors.toList());
            
            // 转换为 DTO
            List<GoodDTO> goodDTOs = goods.stream()
                    .map(good -> {
                        GoodDTO goodDTO = new GoodDTO();
                        BeanUtil.copyProperties(good, goodDTO);
                        // 注意：这里需要确保 good.getId() 不为 null
                        if (good.getId() != null) {
                            goodDTO.setPrice(getMinPrice(good.getId()));
                        }
                        return goodDTO;
                    })
                    .collect(Collectors.toList());
            
            // 构造分页对象
            IPage<GoodDTO> page = new Page<>(pageNum, pageSize);
            page.setRecords(goodDTOs);
            page.setTotal(goods.size());
            page.setPages((goods.size() + pageSize - 1) / pageSize);
            
            return page;
        } catch (Exception e) {
            // 如果 Elasticsearch 搜索失败，回退到传统数据库搜索
            return findPageFallback(pageNum, pageSize, searchText, categoryId);
        }
    }
    
    /**
     * 当 Elasticsearch 搜索失败时的回退方法
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param searchText 搜索文本
     * @param categoryId 分类ID
     * @return 商品分页结果
     */
    private IPage<GoodDTO> findPageFallback(Integer pageNum, Integer pageSize, String searchText, Integer categoryId) {
        LambdaQueryWrapper<Good> query = Wrappers.<Good>lambdaQuery().orderByDesc(Good::getId);
        //对名称和描述进行模糊查询
        if (StrUtil.isNotBlank(searchText)) {
            query.like(Good::getName, searchText).or().like(Good::getDescription, searchText);
        }
        if (categoryId != null) {
            query.eq(Good::getCategoryId, categoryId);
        }
        //筛除掉已被删除的商品
        query.eq(Good::getIsDelete, false);
        IPage<Good> page = this.page(new Page<>(pageNum, pageSize), query);
        //把good转为dto
        IPage<GoodDTO> goodDTOPage = page.convert(good -> {
            GoodDTO goodDTO = new GoodDTO();
            BeanUtil.copyProperties(good, goodDTO);
            return goodDTO;
        });
        for (GoodDTO good : goodDTOPage.getRecords()) {
            //附上最低价格
            good.setPrice(getMinPrice(good.getId()));
        }
        return goodDTOPage;
    }

    public String selectByName(String name) {
        // 查询商品
        Good good = lambdaQuery()
                .eq(Good::getName, name)
                .eq(Good::getIsDelete, false)
                .one();

        if (good == null) {
            return "商品不存在";
        }

        try {
            // 查询规格
            int id = Math.toIntExact(good.getId());
            List<GoodStandard> standards = goodMapper.getStandardById(id);

            // 构建规格信息
            StringBuilder totalValue = new StringBuilder();
            int index = 1;
            for (GoodStandard standard : standards) {
                String value = String.format("第%d规格：%s，价格：%.2f；",
                        index++, standard.getValue(), standard.getPrice());
                totalValue.append(value);
            }

            return String.format("商品名称：%s，描述：%s，价格：%.2f，规格：%s",
                    good.getName(), good.getDescription(), good.getPrice(), totalValue.toString());

        } catch (ArithmeticException e) {
            return "商品ID格式错误";
        }
    }
}
