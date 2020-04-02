package com.anthony.gridea.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;


/**
 * 日志工具
 *
 * @date:2020/4/2 0:34
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
public final class Logger {

    /**
     * 防止空指针. 双重监测没必要
     */
    private static PrintStream writer = System.out;

    private static boolean wConsole = true;

    private static String PATTERN = DateUtil.PATTERN_2;

    public static void configLogFile(String logFile) throws FileNotFoundException {
        writer = new PrintStream(new File(logFile));
    }

    public static void writeConsole(boolean console) {
        wConsole = console;
    }


    public static void configDatePattern(String pattern) {

    }

    private String clzzName;

    private Logger(String clzzName) {
        this.clzzName = clzzName;
    }

    public static Logger getInstance(Class<?> clazz) {
        assertNull(clazz);
        return new Logger(clazz.getName());
    }

    private static void assertNull(Object obj) {
        if (obj == null) throw new RuntimeException();
    }


    public static Logger getInstance(String className) {
        assertNull(className);
        return new Logger(className);
    }


    public void info(String info) {
        String out = String.format("[INFO] - [%s] - [%s] - [%s] - %s"
                , Thread.currentThread().getName()
                , DateUtil.formatDate(new Date(), PATTERN)
                , this.clzzName
                , info
        );
        if (wConsole && writer != System.out) {
            System.out.println(out);
        }
        writer.println(out);
    }

    public void error(String error) {

        String out = String.format("[ERROR] - [%s] - [%s] - [%s] - %s"
                , Thread.currentThread().getName()
                , DateUtil.formatDate(new Date(), PATTERN)
                , this.clzzName
                , error
        );

        if (wConsole && writer != System.out) {
            System.err.println(out);
        }
        writer.println(out);
    }
}
