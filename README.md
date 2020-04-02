# Gridea-Util

> ​	环境要求 JDK1.8 ,  其他就不需要了 . 有Maven最好, 其实也不需要其他的依赖.

该程序可以帮助Gridea生成的md文件转成标准md格式导出来. 同时也记录了一些必要的日志信息.  

默认Gridea生成的md文件位置在 `C:\\Users\\12986\\Documents\\Gridea\\posts` . 

主程序很简单 , 封装了一些必要的信息.

```java
public class GrideaUtil {

    private static final Logger logger = Logger.getInstance(GrideaUtil.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        LongAdder count = new LongAdder();

        // 设置日志输出位置
        Logger.configLogFile("D:\\MyDesktop\\out\\log.log");

        // 设置文件源
        String source = "C:\\Users\\12986\\Documents\\Gridea\\posts";

        // 设置我们文件的输出地
        String des = "D:\\MyDesktop\\out";

        // 首先记录一下,过滤一下文件下面的文档. 看看哪些使我们需要的. 默认是全部都需要.
        List<File> list = new ArrayList<>();
        FileUtil.recordFile(new File(source), FileUtil.DEFAULT_FILTER, list);

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
```



输出日志 : 

```java
[INFO] - [pool-1-thread-3] - [2020-04-02 11:18:51 371] - [com.anthony.gridea.md.MD] - 写出成功 : YqSHXPtCX.md , 文档标题是 : Alibaba - Sentinel , 保存位置 : D:\MyDesktop\out\Alibaba - Sentinel.md
[INFO] - [pool-1-thread-2] - [2020-04-02 11:18:51 371] - [com.anthony.gridea.md.MD] - 写出成功 : ZFz8WuXIa.md , 文档标题是 : Java - 处理空指针的三种方式 , 保存位置 : D:\MyDesktop\out\Java - 处理空指针的三种方式.md
[INFO] - [pool-1-thread-6] - [2020-04-02 11:18:51 373] - [com.anthony.gridea.md.MD] - 写出成功 : xScFeM-wm.md , 文档标题是 : Hystrix - 两种隔离模式 , 保存位置 : D:\MyDesktop\out\Hystrix - 两种隔离模式.md
[INFO] - [pool-1-thread-7] - [2020-04-02 11:18:51 375] - [com.anthony.gridea.md.MD] - 写出成功 : ZW64Jh2NE.md , 文档标题是 : 快速入门 Kafka (Java客户端版本) , 保存位置 : D:\MyDesktop\out\快速入门 Kafka (Java客户端版本).md
[INFO] - [main] - [2020-04-02 11:18:51 376] - [com.anthony.gridea.md.GrideaUtil] - 耗时:411ms ,一共176个文件 , 共修改成功176个文件.
```



注意 : 这里有些标题是不可以做文档的文件名的 ,  所以我们会失败后去过滤掉一部分标题中的字符串 ,  主要过滤逻辑代码在 `com.anthony.gridea.md.MD#handlerFilterPath`

```java
// 这些字符都是不可以当做文件夹名称的
private static final String[] str = {"/", "\\", ":", "*", "\"", "<", ">", "|", "?"};

// 写了个正则 . 去替换.
private static final String _PATTERN = "[\\\\\"*|/><@#:?)(]";

// 替换代码逻辑.
private static String handlerFilterPath(String filePath) {
    String _1 = filePath.replaceAll(_PATTERN, " ");
    String _2 = _1.replace('/', ' ');
    return _2.replace('\\', ' ');
}
```

