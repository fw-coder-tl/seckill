package io.binghe.seckill.order.application.place.impl;

import io.binghe.seckill.common.cache.distribute.DistributedCacheService;
import io.binghe.seckill.common.constants.SeckillConstants;
import io.binghe.seckill.dubbo.interfaces.goods.SeckillGoodsDubboService;
import io.binghe.seckill.order.application.command.SeckillOrderCommand;
import io.binghe.seckill.order.domain.service.SeckillOrderDomainService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeckillPlaceOrderBaseService {

    private final Logger logger = LoggerFactory.getLogger(SeckillPlaceOrderBaseService.class);

    @Autowired
    protected DistributedCacheService distributedCacheService;
    @Autowired
    protected SeckillOrderDomainService seckillOrderDomainService;
    @DubboReference(version = "1.0.0", check = false)
    protected SeckillGoodsDubboService seckillGoodsDubboService;


    @Transactional(rollbackFor = Exception.class)
    public Long confirmMethod(Long userId, SeckillOrderCommand seckillOrderCommand, Long txNo){
        if (!distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)){
            logger.warn("confirmMethod|未执行过Try方法,txNo:{}", txNo);
            return txNo;
        }
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)){
            logger.warn("confirmMethod|提交订单已经执行过Condirm方法,txNo:{}", txNo);
            return txNo;
        }
        logger.info("confirmMethod|提交订单执行Confirm方法,txNo:{}", txNo);
        boolean isSaveConfirmLog = false;
        //保存Confirm日志
        try{
            distributedCacheService.addSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            isSaveConfirmLog = true;
        }catch (Exception e){
            if (isSaveConfirmLog){
                distributedCacheService.removeSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            }
        }
        return txNo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long cancelMethod(Long userId, SeckillOrderCommand seckillOrderCommand, Long txNo){
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)){
            logger.warn("confirmMethod|提交订单已经执行过Cancel方法,txNo:{}", txNo);
            return txNo;
        }
        logger.info("cancelMethod|提交订单执行Cancel方法,txNo:{}", txNo);
        boolean isSaveCancelLog = false;
        try{
            //保存cancel日志
            distributedCacheService.addSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            isSaveCancelLog = true;
            //删除提交的订单数据
            seckillOrderDomainService.deleteSeckillOrder(txNo);
        }catch (Exception e){
            if (isSaveCancelLog){
                distributedCacheService.removeSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            }
        }
        return txNo;
    }
}