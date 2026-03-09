package io.binghe.seckill.user.infrastructure.repository;

import io.binghe.seckill.user.domain.model.entity.SeckillUser;
import io.binghe.seckill.user.domain.repository.SeckillUserRepository;
import io.binghe.seckill.user.infrastructure.mapper.SeckillUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class SeckillUserRepositoryImpl implements SeckillUserRepository {

    @Autowired
    private SeckillUserMapper seckillUserMapper;

    @Override
    public SeckillUser getSeckillUserByUserName(String userName) {
        return seckillUserMapper.getSeckillUserByUserName(userName);
    }
}
