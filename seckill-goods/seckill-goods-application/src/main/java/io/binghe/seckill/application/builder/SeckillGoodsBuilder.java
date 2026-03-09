package io.binghe.seckill.application.builder;

import io.binghe.seckill.application.command.SeckillGoodsCommond;
import io.binghe.seckill.common.builder.SeckillCommonBuilder;
import io.binghe.seckill.common.model.dto.SeckillGoodsDTO;
import io.binghe.seckill.common.utils.beans.BeanUtil;
import io.binghe.seckill.goods.domain.model.entity.SeckillGoods;
public class SeckillGoodsBuilder extends SeckillCommonBuilder {

    public static SeckillGoods toSeckillGoods(SeckillGoodsCommond seckillGoodsCommond){
        if (seckillGoodsCommond == null){
            return null;
        }
        SeckillGoods seckillGoods = new SeckillGoods();
        BeanUtil.copyProperties(seckillGoodsCommond, seckillGoods);
        return seckillGoods;
    }

    public static SeckillGoodsDTO toSeckillGoodsDTO(SeckillGoods seckillGoods){
        if (seckillGoods == null){
            return null;
        }
        SeckillGoodsDTO seckillGoodsDTO = new SeckillGoodsDTO();
        BeanUtil.copyProperties(seckillGoods, seckillGoodsDTO);
        return seckillGoodsDTO;
    }
}
