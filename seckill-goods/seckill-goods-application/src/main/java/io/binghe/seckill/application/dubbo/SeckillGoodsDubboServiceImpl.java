package io.binghe.seckill.application.dubbo;

import io.binghe.seckill.application.service.SeckillGoodsService;
import io.binghe.seckill.common.cache.distribute.DistributedCacheService;
import io.binghe.seckill.common.constants.SeckillConstants;
import io.binghe.seckill.common.exception.ErrorCode;
import io.binghe.seckill.common.exception.SeckillException;
import io.binghe.seckill.common.model.dto.SeckillGoodsDTO;
import io.binghe.seckill.dubbo.interfaces.goods.SeckillGoodsDubboService;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.hmily.annotation.HmilyTCC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@DubboService(version = "1.0.0")
public class SeckillGoodsDubboServiceImpl implements SeckillGoodsDubboService {
    private final Logger logger = LoggerFactory.getLogger(SeckillGoodsDubboServiceImpl.class);
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private DistributedCacheService distributedCacheService;

    @Override
    public SeckillGoodsDTO getSeckillGoods(Long id, Long version) {
        return seckillGoodsService.getSeckillGoods(id, version);
    }

    @Override
    public boolean updateDbAvailableStock(Integer count, Long id) {
        return seckillGoodsService.updateDbAvailableStock(count, id);
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    public boolean updateAvailableStock(Integer count, Long id, Long txNo) {
        //幂等处理
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo)){
            logger.warn("updateAvailableStock|更新库存已经执行过Try方法|{}", txNo);
            return false;
        }
        //悬挂处理
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo)
                || distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo)){
            logger.warn("updateAvailableStock|更新库存已经执行过Confirm方法或者Cancel方法|{}", txNo);
            return false;
        }
        boolean result;
        boolean isSaveTryLog = false;
        try{
            distributedCacheService.addSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo);
            isSaveTryLog = true;
            result = seckillGoodsService.updateAvailableStock(count, id);
        }catch (Exception e){
            if (isSaveTryLog){
                distributedCacheService.removeSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo);
            }
            if (e instanceof SeckillException){
                throw e;
            }else {
                throw new SeckillException(ErrorCode.STOCK_LT_ZERO);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmMethod(Integer count, Long id, Long txNo){
        if (!distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo)){
            logger.warn("confirmMethod|未过Try方法|{}", txNo);
            return false;
        }
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo)){
            logger.warn("confirmMethod|更新库存已经执行过Confirm方法|{}", txNo);
            return false;
        }
        logger.info("confirmMethod|更新库存执行Confirm方法|{}", txNo);
        boolean isSaveConfirmLog = false;
        try{
            distributedCacheService.addSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo);
            isSaveConfirmLog = true;
        }catch (Exception e){
            if (isSaveConfirmLog){
                distributedCacheService.removeSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo);
            }
        }
        return isSaveConfirmLog;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelMethod(Integer count, Long id, Long txNo){
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo)){
            logger.warn("cancelMethod|更新库存已经执行过Cancel方法|{}", txNo);
            return false;
        }
        logger.info("cancelMethod|更新库存执行Cancel方法|{}", txNo);
        boolean result = false;
        boolean isSaveCancelLog = false;
        try{
            distributedCacheService.addSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo);
            isSaveCancelLog = true;
            result = seckillGoodsService.incrementAvailableStock(count, id);
        }catch (Exception e){
            if (isSaveCancelLog){
                distributedCacheService.removeSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.GOODS_KEY), txNo);
            }
        }
        return result;

    }
}
