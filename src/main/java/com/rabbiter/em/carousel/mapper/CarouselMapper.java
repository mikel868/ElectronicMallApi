package com.rabbiter.em.carousel.mapper;

import com.rabbiter.em.carousel.entity.Carousel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CarouselMapper extends BaseMapper<Carousel> {

    List<Carousel> getAllCarousel();
}
