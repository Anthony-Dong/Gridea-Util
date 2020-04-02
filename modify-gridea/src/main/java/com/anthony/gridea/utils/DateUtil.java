package com.anthony.gridea.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
public class DateUtil {


    public static final String PATTERN_1 = "yyyy-MM-dd HH:mm:ss";

    public static final String PATTERN_2 = "yyyy-MM-dd HH:mm:ss SSS";


    /**
     * 详细可以看{@link DateTimeFormatterBuilder#appendPattern(String)} 这个详细
     * <p>
     */
    public static final char year = 'y';

    public static final char month = 'M';

    public static final char day = 'd';

    public static final char hour_24 = 'H';

    public static final char hour_12 = 'h';


    public static final char minute = 'm';


    public static final char second = 's';


    public static final char millisecond = 'S';

    /**
     * 日期格式化
     *
     * @param date      日期
     * @param formatter 格式化
     * @return 字符串
     */
    public static String formatDate(Date date, DateTimeFormatter formatter) {
        LocalDateTime time = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return time.format(formatter);
    }


    public static String formatDate(Date date, String pattern) {
        return formatDate(date, DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatDate(Date date) {
        return formatDate(date, PATTERN_1);
    }


    public static String formatNow(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }


    public static Date formatDate(long timestamp) {
        return new Date(timestamp);
    }

    public static int getMonth() {
        return LocalDate.now().getMonth().getValue();
    }


    public static Date localDateToDate(LocalDate localDate) {
        return null == localDate ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate dateToLocalDate(Date date) {
        return null == date ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    public static LocalDateTime dateToLocalDateTime(Date date) {
        return null == date ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}