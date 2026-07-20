package com.rabbiter.em.product.dto;

import java.math.BigDecimal;

/**
 * 商品 Elasticsearch 文档实体类
 * 用于 Elasticsearch 索引存储和搜索
 * 使用原生Elasticsearch客户端，不依赖Spring Data Elasticsearch注解
 */
public class GoodDocument {

    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 折扣
     */
    private Double discount;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 销售额
     */
    private BigDecimal saleMoney;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 商品图片
     */
    private String imgs;

    /**
     * 是否推荐：0不推荐，1推荐
     */
    private Boolean recommend;

    /**
     * 创建时间
     */
    private String createTime;

    public GoodDocument() {}

    public GoodDocument(Long id, String name, String description, Double discount,
                        Integer sales, BigDecimal saleMoney, Long categoryId, 
                        String imgs, Boolean recommend, String createTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.discount = discount;
        this.sales = sales;
        this.saleMoney = saleMoney;
        this.categoryId = categoryId;
        this.imgs = imgs;
        this.recommend = recommend;
        this.createTime = createTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public BigDecimal getSaleMoney() {
        return saleMoney;
    }

    public void setSaleMoney(BigDecimal saleMoney) {
        this.saleMoney = saleMoney;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getImgs() {
        return imgs;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public Boolean getRecommend() {
        return recommend;
    }

    public void setRecommend(Boolean recommend) {
        this.recommend = recommend;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "GoodDocument{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", discount=" + discount +
                ", sales=" + sales +
                ", saleMoney=" + saleMoney +
                ", categoryId=" + categoryId +
                ", imgs='" + imgs + '\'' +
                ", recommend=" + recommend +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}