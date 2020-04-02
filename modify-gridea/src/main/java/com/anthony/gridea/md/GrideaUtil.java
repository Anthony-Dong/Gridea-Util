package com.anthony.gridea.md;

import com.anthony.gridea.utils.FileUtil;
import com.anthony.gridea.utils.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;

/**
 * 导出工具
 *
 * @date:2020/4/1 21:20
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
public class GrideaUtil {

    private static final Logger logger = Logger.getInstance(GrideaUtil.class);


    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();

        // 设置日志输出位置
        Logger.configLogFile("D:\\MyDesktop\\out\\log.log");

        // 设置文件源
        String source = "C:\\Users\\12986\\Documents\\Gridea\\posts";

        // 设置我们文件的输出地
        String des = "D:\\MyDesktop\\out";


        // 首先记录一下,过滤一下文件下面的文档. 看看哪些使我们需要的. 默认是全部都需要.
        List<File> list = new ArrayList<>();
        FileUtil.recordFile(new File(source), FileUtil.DEFAULT_FILTER, list);


        // 计数器
        LongAdder count = new LongAdder();

        // 多线程并发处理
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        list.forEach(file -> pool.execute(() -> {
            try {
                MD md = new MD(file);
                md.setNeedWriteTitle(true);
                md.setNeedWritePicture(true);
                md.write(des);
                count.increment();
            } catch (IOException e) {
                logger.error(String.format("文件<%s>未修改成功的原因:%s.", file.getName(), e.getMessage()));
            }
        }));

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        logger.info(String.format("耗时:%dms ,一共%d个文件 , 共修改成功%d个文件.", System.currentTimeMillis() - start, list.size(), count.sum()));
    }
}
