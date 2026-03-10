package io.binghe.seckill.order.application.place.impl;

import com.alibaba.fastjson.JSONObject;
import io.binghe.seckill.common.constants.SeckillConstants;
import io.binghe.seckill.common.exception.ErrorCode;
import io.binghe.seckill.common.exception.SeckillException;
import io.binghe.seckill.common.lock.DistributedLock;
import io.binghe.seckill.common.lock.factoty.DistributedLockFactory;
import io.binghe.seckill.common.model.dto.SeckillGoodsDTO;
import io.binghe.seckill.order.application.command.SeckillOrderCommand;
import io.binghe.seckill.order.application.place.SeckillPlaceOrderService;
import io.binghe.seckill.order.domain.model.entity.SeckillOrder;
import org.dromara.hmily.annotation.HmilyTCC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "place.order.type", havingValue = "lock")
public class SeckillPlaceOrderLockService extends SeckillPlaceOrderBaseService implements SeckillPlaceOrderService {
    private final Logger logger = LoggerFactory.getLogger(SeckillPlaceOrderLockService.class);

    @Autowired
    private DistributedLockFactory distributedLockFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HmilyTCC(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    public Long placeOrder(Long userId, SeckillOrderCommand seckillOrderCommand, Long txNo) {
        //幂等处理
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)) {
            logger.warn("placeOrder|基于分布式锁防止超卖的方法已经执行过Try方法,txNo:{}", txNo);
            return txNo;
        }
        //悬挂处理
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)
                || distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)) {
            logger.warn("placeOrder|基于分布式锁防止超卖的方法已经执行过Condirm方法或者Cancel方法,txNo:{}", txNo);
            return txNo;
        }
        //获取商品
        SeckillGoodsDTO seckillGoods = seckillGoodsDubboService.getSeckillGoods(seckillOrderCommand.getGoodsId(), seckillOrderCommand.getVersion());
        //检测商品信息
        this.checkSeckillGoods(seckillOrderCommand, seckillGoods);
        String lockKey = SeckillConstants.getKey(SeckillConstants.ORDER_LOCK_KEY_PREFIX, String.valueOf(seckillOrderCommand.getGoodsId()));
        DistributedLock lock = distributedLockFactory.getDistributedLock(lockKey);
        // 获取内存中的库存信息
        String key = SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, String.valueOf(seckillOrderCommand.getGoodsId()));
        //是否扣减了缓存中的库存
        boolean isDecrementCacheStock = false;
        //是否保存try日志
        boolean isSaveTryLog = false;
        try {
            //未获取到分布式锁
            if (!lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                throw new SeckillException(ErrorCode.RETRY_LATER);
            }
            // 查询库存信息
            Integer stock = distributedCacheService.getObject(key, Integer.class);
            //库存不足
            if (stock < seckillOrderCommand.getQuantity()) {
                throw new SeckillException(ErrorCode.STOCK_LT_ZERO);
            }
            //扣减库存
            Long result = distributedCacheService.decrement(key, seckillOrderCommand.getQuantity());
            if (result < 0) {
                throw new SeckillException(ErrorCode.STOCK_LT_ZERO);
            }
            //正常执行了扣减缓存中库存的操作
            isDecrementCacheStock = true;
            //构建订单
            SeckillOrder seckillOrder = this.buildSeckillOrder(userId, seckillOrderCommand, seckillGoods);
            //巧妙的使用事务编号作为订单id，避免过多资源浪费，也可以使用其他方式生成订单id
            seckillOrder.setId(txNo);
            //保存订单
            seckillOrderDomainService.saveSeckillOrder(seckillOrder);
            //保存try日志
            distributedCacheService.addSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            isSaveTryLog = true;
            //扣减数据库库存
            seckillGoodsDubboService.updateAvailableStock(seckillOrderCommand.getQuantity(), seckillOrderCommand.getGoodsId(), txNo);
//            int i = 1 / 0;
            //库存数据库库存
            return seckillOrder.getId();
        } catch (Exception e) {
            //已经扣减了缓存中的库存，则需要增加回来
            if (isDecrementCacheStock) {
                distributedCacheService.increment(key, seckillOrderCommand.getQuantity());
            }
            if (isSaveTryLog) {
                distributedCacheService.removeSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            }
            if (e instanceof InterruptedException) {
                logger.error("SeckillPlaceOrderLockService|下单分布式锁被中断|参数:{}|异常信息:{}", JSONObject.toJSONString(seckillOrderCommand), e.getMessage());
                throw new SeckillException(ErrorCode.ORDER_FAILED);
            } else if (e instanceof SeckillException) {
                SeckillException se = (SeckillException) e;
                throw new SeckillException(se.getCode(), se.getMessage());
            } else {
                logger.error("SeckillPlaceOrderLockService|分布式锁下单失败|参数:{}|异常信息:{}", JSONObject.toJSONString(seckillOrderCommand), e.getMessage());
                throw new SeckillException(ErrorCode.ORDER_FAILED);
            }
        } finally {
            lock.unlock();
        }
    }

}

