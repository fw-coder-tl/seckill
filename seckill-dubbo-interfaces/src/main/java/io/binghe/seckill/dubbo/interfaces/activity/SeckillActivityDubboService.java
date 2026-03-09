package io.binghe.seckill.dubbo.interfaces.activity;

import io.binghe.seckill.common.model.dto.SeckillActivityDTO;
public interface SeckillActivityDubboService {

    /**
     * 获取活动信息
     */
    SeckillActivityDTO getSeckillActivity(Long id, Long version);
}
