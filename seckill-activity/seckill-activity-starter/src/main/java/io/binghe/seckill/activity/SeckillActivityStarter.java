package io.binghe.seckill.activity;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableDubbo
@SpringBootApplication
public class SeckillActivityStarter {

    public static void main(String[] args) {
        System.setProperty("user.home", "/home/binghe/activity");
        SpringApplication.run(SeckillActivityStarter.class, args);
    }

}
