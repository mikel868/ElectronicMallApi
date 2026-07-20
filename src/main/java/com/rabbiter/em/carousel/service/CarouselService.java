package com.rabbiter.em.carousel.service;

import com.rabbiter.em.carousel.entity.Carousel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.carousel.mapper.CarouselMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class CarouselService extends ServiceImpl<CarouselMapper, Carousel> {

    @Resource
    private CarouselMapper carouselMapper;

    public List<Carousel> getAllCarousel() {
        return carouselMapper.getAllCarousel();
    }
}
