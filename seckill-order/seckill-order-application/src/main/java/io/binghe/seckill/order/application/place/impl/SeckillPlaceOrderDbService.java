package io.binghe.seckill.order.application.place.impl;

import io.binghe.seckill.common.constants.SeckillConstants;
import io.binghe.seckill.common.exception.ErrorCode;
import io.binghe.seckill.common.exception.SeckillException;
import io.binghe.seckill.common.model.dto.SeckillGoodsDTO;
import io.binghe.seckill.order.application.command.SeckillOrderCommand;
import io.binghe.seckill.order.application.place.SeckillPlaceOrderService;
import io.binghe.seckill.order.domain.model.entity.SeckillOrder;
import org.dromara.hmily.annotation.HmilyTCC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "place.order.type", havingValue = "db")
public class SeckillPlaceOrderDbService extends SeckillPlaceOrderBaseService implements SeckillPlaceOrderService {

    private final Logger logger = LoggerFactory.getLogger(SeckillPlaceOrderDbService.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HmilyTCC(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    public Long placeOrder(Long userId, SeckillOrderCommand seckillOrderCommand, Long txNo) {
        //幂等处理
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)) {
            logger.warn("placeOrder|基于数据库防止超卖的方法已经执行过Try方法,txNo:{}", txNo);
            return txNo;
        }
        //悬挂处理
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)
                || distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)) {
            logger.warn("placeOrder|基于数据库防止超卖的方法已经执行过Condirm方法或者Cancel方法,txNo:{}", txNo);
            return txNo;
        }
        boolean isSaveTryLog = false;
        try {
            //获取商品
            SeckillGoodsDTO seckillGoods = seckillGoodsDubboService.getSeckillGoods(seckillOrderCommand.getGoodsId(), seckillOrderCommand.getVersion());
            //检测商品信息
            this.checkSeckillGoods(seckillOrderCommand, seckillGoods);
            //扣减库存不成功，则库存不足
            if (!seckillGoodsDubboService.updateAvailableStock(seckillOrderCommand.getQuantity(), seckillOrderCommand.getGoodsId(), txNo)) {
                throw new SeckillException(ErrorCode.STOCK_LT_ZERO);
            }
            //构建订单
            SeckillOrder seckillOrder = this.buildSeckillOrder(userId, seckillOrderCommand, seckillGoods);
            //巧妙的使用事务编号作为订单id，避免过多资源浪费，也可以使用其他方式生成订单id
            seckillOrder.setId(txNo);
            //保存try日志
            distributedCacheService.addSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            isSaveTryLog = true;
            //保存订单
            seckillOrderDomainService.saveSeckillOrder(seckillOrder);
//            int i = 1 / 0;
        } catch (Exception e) {
            if (isSaveTryLog) {
                //异常清除try日志
                distributedCacheService.removeSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            }
            if (e instanceof SeckillException) {
                throw e;
            } else {
                throw new SeckillException(ErrorCode.ORDER_FAILED);
            }
        }
        return txNo;
    }

}
