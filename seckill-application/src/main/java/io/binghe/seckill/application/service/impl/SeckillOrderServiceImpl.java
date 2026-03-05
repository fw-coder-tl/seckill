package io.binghe.seckill.application.service.impl;

import io.binghe.seckill.application.command.SeckillOrderCommand;
import io.binghe.seckill.application.order.place.SeckillPlaceOrderService;
import io.binghe.seckill.application.service.SeckillGoodsService;
import io.binghe.seckill.application.service.SeckillOrderService;
import io.binghe.seckill.domain.code.HttpCode;
import io.binghe.seckill.domain.model.dto.SeckillOrderDTO;
import io.binghe.seckill.domain.model.enums.SeckillGoodsStatus;
import io.binghe.seckill.domain.model.enums.SeckillOrderStatus;
import io.binghe.seckill.domain.exception.SeckillException;
import io.binghe.seckill.domain.model.entity.SeckillGoods;
import io.binghe.seckill.domain.model.entity.SeckillOrder;
import io.binghe.seckill.domain.repository.SeckillOrderRepository;
import io.binghe.seckill.domain.service.SeckillOrderDomainService;
import io.binghe.seckill.infrastructure.utils.beans.BeanUtil;
import io.binghe.seckill.infrastructure.utils.id.SnowFlakeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {
    @Autowired
    private SeckillOrderDomainService seckillOrderDomainService;
    @Autowired
    private SeckillPlaceOrderService seckillPlaceOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveSeckillOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        if (seckillOrderCommand == null){
            throw new SeckillException(HttpCode.PARAMS_INVALID);
        }
        return seckillPlaceOrderService.placeOrder(userId, seckillOrderCommand);
    }

    @Override
    public List<SeckillOrder> getSeckillOrderByUserId(Long userId) {
        return seckillOrderDomainService.getSeckillOrderByUserId(userId);
    }

    @Override
    public List<SeckillOrder> getSeckillOrderByActivityId(Long activityId) {
        return seckillOrderDomainService.getSeckillOrderByActivityId(activityId);
    }
}
