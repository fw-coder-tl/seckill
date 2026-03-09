package io.binghe.seckill.common.utils.uuid;

import java.util.UUID;
public class UUIDUtils {

    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
