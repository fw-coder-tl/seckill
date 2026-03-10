package io.binghe.seckill.common.utils.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    /**
     * yyyy-MM-dd HH:mm:ss格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * yyyy-MM-dd格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * yyyyMMdd格式
     */
    public static final String SIMPLE_YEAR_MONTH_DATE_FORMAT = "yyyyMMdd";


    /**
     * 获取当前的yyyyMMdd格式的字符串时间
     */
    public static String getCurrentDate(String format){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }
}