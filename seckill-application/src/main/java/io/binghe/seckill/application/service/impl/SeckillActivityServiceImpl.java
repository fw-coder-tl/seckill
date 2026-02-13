package io.binghe.seckill.application.service.impl;

import io.binghe.seckill.application.builder.SeckillActivityBuilder;
import io.binghe.seckill.application.command.SeckillActivityCommand;
import io.binghe.seckill.application.service.SeckillActivityService;
import io.binghe.seckill.domain.code.HttpCode;
import io.binghe.seckill.domain.model.enums.SeckillActivityStatus;
import io.binghe.seckill.domain.exception.SeckillException;
import io.binghe.seckill.domain.model.entity.SeckillActivity;
import io.binghe.seckill.domain.repository.SeckillActivityRepository;
import io.binghe.seckill.infrastructure.utils.id.SnowFlakeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SeckillActivityServiceImpl implements SeckillActivityService {
    @Autowired
    private SeckillActivityRepository seckillActivityRepository;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSeckillActivityDTO(SeckillActivityCommand seckillActivityCommand) {
        if (seckillActivityCommand == null){
            throw new SeckillException(HttpCode.PARAMS_INVALID);
        }
        SeckillActivity seckillActivity = SeckillActivityBuilder.toSeckillActivity(seckillActivityCommand);
        seckillActivity.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
        seckillActivity.setStatus(SeckillActivityStatus.PUBLISHED.getCode());
        seckillActivityRepository.saveSeckillActivity(seckillActivity);
    }

    @Override
    public List<SeckillActivity> getSeckillActivityList(Integer status) {
        return seckillActivityRepository.getSeckillActivityList(status);
    }

    @Override
    public List<SeckillActivity> getSeckillActivityListBetweenStartTimeAndEndTime(Date currentTime, Integer status) {
        return seckillActivityRepository.getSeckillActivityListBetweenStartTimeAndEndTime(currentTime, status);
    }

    @Override
    public SeckillActivity getSeckillActivityById(Long id) {
        return seckillActivityRepository.getSeckillActivityById(id);
    }

    @Override
    public int updateStatus(Integer status, Long id) {
        return seckillActivityRepository.updateStatus(status, id);
    }
}
