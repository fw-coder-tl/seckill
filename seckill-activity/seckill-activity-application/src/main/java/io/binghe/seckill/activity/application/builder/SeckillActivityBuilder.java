package io.binghe.seckill.activity.application.builder;

import io.binghe.seckill.activity.application.command.SeckillActivityCommand;
import io.binghe.seckill.activity.domain.model.entity.SeckillActivity;
import io.binghe.seckill.common.builder.SeckillCommonBuilder;
import io.binghe.seckill.common.model.dto.SeckillActivityDTO;
import io.binghe.seckill.common.utils.beans.BeanUtil;
public class SeckillActivityBuilder extends SeckillCommonBuilder {

    public static SeckillActivity toSeckillActivity(SeckillActivityCommand seckillActivityCommand){
        if (seckillActivityCommand == null){
            return null;
        }
        SeckillActivity seckillActivity = new SeckillActivity();
        BeanUtil.copyProperties(seckillActivityCommand, seckillActivity);
        return seckillActivity;
    }

    public static SeckillActivityDTO toSeckillActivityDTO(SeckillActivity seckillActivity){
        if (seckillActivity == null){
            return null;
        }
        SeckillActivityDTO seckillActivityDTO = new SeckillActivityDTO();
        BeanUtil.copyProperties(seckillActivity, seckillActivityDTO);
        return seckillActivityDTO;
    }
}
