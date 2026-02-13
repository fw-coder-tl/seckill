package io.binghe.seckill.application.service.impl;

import io.binghe.seckill.application.service.SeckillGoodsService;
import io.binghe.seckill.domain.code.HttpCode;
import io.binghe.seckill.domain.model.dto.SeckillGoodsDTO;
import io.binghe.seckill.domain.model.enums.SeckillGoodsStatus;
import io.binghe.seckill.domain.exception.SeckillException;
import io.binghe.seckill.domain.model.entity.SeckillActivity;
import io.binghe.seckill.domain.model.entity.SeckillGoods;
import io.binghe.seckill.domain.repository.SeckillActivityRepository;
import io.binghe.seckill.domain.repository.SeckillGoodsRepository;
import io.binghe.seckill.infrastructure.utils.beans.BeanUtil;
import io.binghe.seckill.infrastructure.utils.id.SnowFlakeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    @Autowired
    private SeckillGoodsRepository seckillGoodsRepository;
    @Autowired
    private SeckillActivityRepository seckillActivityRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveSeckillGoods(SeckillGoodsDTO seckillGoodsDTO) {
        if (seckillGoodsDTO == null){
            throw new SeckillException(HttpCode.PARAMS_INVALID);
        }
        SeckillActivity seckillActivity = seckillActivityRepository.getSeckillActivityById(seckillGoodsDTO.getActivityId());
        if (seckillActivity == null){
            throw new SeckillException(HttpCode.ACTIVITY_NOT_EXISTS);
        }
        SeckillGoods seckillGoods = new SeckillGoods();
        BeanUtil.copyProperties(seckillGoodsDTO, seckillGoods);
        seckillGoods.setStartTime(seckillActivity.getStartTime());
        seckillGoods.setEndTime(seckillActivity.getEndTime());
        seckillGoods.setAvailableStock(seckillGoodsDTO.getInitialStock());
        seckillGoods.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
        seckillGoods.setStatus(SeckillGoodsStatus.PUBLISHED.getCode());
        return seckillGoodsRepository.saveSeckillGoods(seckillGoods);
    }

    @Override
    public SeckillGoods getSeckillGoodsId(Long id) {
        return seckillGoodsRepository.getSeckillGoodsId(id);
    }


    @Override
    public List<SeckillGoods> getSeckillGoodsByActivityId(Long activityId) {
        return seckillGoodsRepository.getSeckillGoodsByActivityId(activityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(Integer status, Long id) {
        return seckillGoodsRepository.updateStatus(status, id);
    }

    @Override
    public int updateAvailableStock(Integer count, Long id) {
        return seckillGoodsRepository.updateAvailableStock(count, id);
    }

    @Override
    public Integer getAvailableStockById(Long id) {
        return seckillGoodsRepository.getAvailableStockById(id);
    }
}
