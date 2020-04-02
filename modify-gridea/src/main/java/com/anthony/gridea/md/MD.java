package com.anthony.gridea.md;

import com.anthony.gridea.utils.IOUtils;
import com.anthony.gridea.utils.Logger;
import com.anthony.gridea.utils.StringUtils;

import java.io.*;
import java.util.function.Predicate;

/**
 * md 文件类.
 *
 * @date:2020/4/2 0:33
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
public class MD {
    private static final Logger logger = Logger.getInstance(MD.class);


    // 这些字符都是不可以当做文件夹名称的
    private static final String[] str = {"/", "\\", ":", "*", "\"", "<", ">", "|", "?"};

    // 写了个正则 . 替换.
    private static final String _PATTERN = "[\\\\\"*|/><@#:?)(]";


    private static final String _START = "---";

    private static final String _TITLE = "title:";

    private static final String _PIC = "feature:";

    private static final String _TAGS = "tags:";

    private static final String _Date = "date:";

    private static final String _FILE_FORMAT = ".md";

    /**
     * 获取系统的分隔符
     */
    private static final String _FILE_SEPARATOR = System.getProperty("file.separator");

    /**
     * 是否需要写出图片
     */
    private boolean needWritePicture = false;


    /**
     * 是否需要写入标题
     */
    private boolean needWriteTitle = false;

    /**
     * 设置输出需要写图片
     */
    public void setNeedWritePicture(boolean needWritePicture) {
        this.needWritePicture = needWritePicture;
    }

    /**
     * 设置输出需要写标题
     */
    public void setNeedWriteTitle(boolean needWriteTitle) {
        this.needWriteTitle = needWriteTitle;
    }

    /**
     * 全局行
     */
    private String[] lines;

    /**
     * 是否需要些
     */
    private boolean[] isWrite;


    /**
     * 文件
     */
    private File file;


    /**
     * 文章内容过滤器 , 默认是不需要规律的.
     * true  代表需要过滤,不会写出到新的文字中
     * false 代表不过滤
     */
    private Predicate<String> filter = DEFAULT_FILTER;

    //设置过滤器.
    public void setFilter(Predicate<String> filter) {
        this.filter = filter;
    }

    /**
     * 构造器 . 看需求
     */
    public MD(String fileName) {
        this(new File(fileName));
    }

    public MD(File file) {
        this.file = file;
    }

    /**
     * 文章的图片链接
     */
    private String pic;

    /**
     * 文章的标题链接
     */
    private String title;

    /**
     * 文章的标签
     */
    private String[] tags;

    /**
     * 文章创建时间
     */
    private String date;

    // 一些状态量
    private boolean hasGetInfo = false;

    // 如果你的文章初始化好了. 记得设置为true.
    private boolean hasInit = false;

    private boolean hasInitLine = false;

    /**
     * 读取文件中的数据
     */
    private void initLine() throws IOException {
        if (this.file != null) {
            this.lines = IOUtils.readLines(file);
            this.isWrite = new boolean[lines.length];
            hasInitLine = true;
        } else {
            throw new RuntimeException("文件为空,初始化失败");
        }
    }


    /**
     * 初始化信息 , 包含title信息
     */
    private void init() {
        // 判断 边界条件
        if (lines == null || isWrite == null) {
            throw new RuntimeException("初始化失败");
        }
        int start_count = 0;
        // 记录一些初始化信息
        for (int i = 0; i < lines.length; i++) {
            // 没有拿到初始化完 前面的信息.
            if (!hasGetInfo) {
                // 直接不写.
                isWrite[i] = true;
                // 读取
                String line = lines[i];
                // 如果开始.
                if (line.equals(_START)) {
                    if (++start_count == 2) {
                        hasGetInfo = true;
                    }
                    continue;
                }
                if (line.startsWith(_TITLE)) {
                    this.title = handlerTitle(line);
                    continue;
                }
                if (line.startsWith(_PIC)) {
                    this.pic = handlerPic(line);
                    continue;
                }
                if (line.startsWith(_TAGS)) {
                    this.tags = handlerTag(line);
                    continue;
                }
                if (line.startsWith(_Date)) {
                    this.date = handlerData(line);
                    continue;
                }
            } else {
                // 默认是不需要初始化全部的. 所以直接break
                if (filter == DEFAULT_FILTER) {
                    break;
                }
                // 你要是还要过滤就自己实现一个.
                isWrite[i] = filter.test(lines[i]);
            }
        }
        // 设置初始化完成.
        hasInit = true;
    }

    private static final Predicate<String> DEFAULT_FILTER = s -> false;

    /**
     * 写出文件
     */
    public void write(String filePath) throws IOException {
        // 初始化文件的内容
        if (!hasInitLine) {
            initLine();
        }

        // 如果没有初始化. 防止用户调用 init 然后再调用 write.
        if (!hasInit) {
            init();
        }

        String file;
        if (filePath.endsWith(_FILE_SEPARATOR)) {
            file = filePath + this.title + _FILE_FORMAT;
        } else {
            file = filePath + _FILE_SEPARATOR + this.title + _FILE_FORMAT;
        }

        try (BufferedWriter stream = new BufferedWriter(new FileWriter(file))) {
            // 可能需要写某些玩意
            writeIntoFile(file, stream);
        } catch (FileNotFoundException e) {
            // 修改 title
            String new_path = handlerFilterPath(title);
            logger.error(String.format("写出失败 : %s , 尝试修改标题 : %s  ->  %s", this.file.getName(), title, new_path));
            fail(filePath, new_path);
        }
    }

    private void writeIntoFile(String file, BufferedWriter stream) throws IOException {
        prepare(stream);
        for (int i = 0; i < this.isWrite.length; i++) {
            if (!isWrite[i]) {
                stream.write(this.lines[i]);
                stream.newLine();
            }
        }
        logger.info(String.format("写出成功 : %s , 文档标题是 : %s , 保存位置 : %s", this.file.getName(), this.title, file));
    }

    /**
     * 过滤标题的特殊字符
     */
    private static String handlerFilterPath(String filePath) {
        String _1 = filePath.replaceAll(_PATTERN, " ");
        String _2 = _1.replace('/', ' ');
        return _2.replace('\\', ' ');
    }


    /**
     * 如果是因为 分隔符的问题. 根据正则去替换
     *
     * @param filePath
     * @throws IOException
     */
    private void fail(String filePath, String new_title) throws IOException {
        String file;
        if (filePath.endsWith(_FILE_SEPARATOR)) {
            file = filePath + new_title + _FILE_FORMAT;
        } else {
            file = filePath + _FILE_SEPARATOR + new_title + _FILE_FORMAT;
        }

        try (BufferedWriter stream = new BufferedWriter(new FileWriter(file))) {
            // 可能需要写某些玩意
            writeIntoFile(file, stream);
        }
    }

    private static final String TITLE_FORMAT = "# %s";
    private static final String PIC_FORMAT = "![](%s)";

    /**
     * 可能需要写入其他东西
     */
    private void prepare(BufferedWriter stream) throws IOException {
        if (needWriteTitle) {
            if (StringUtils.isNotBlack(this.title)) {
                String title_line = String.format(TITLE_FORMAT, this.title);
                stream.write(title_line);
                stream.newLine();
            }
        }

        if (needWritePicture) {
            if (StringUtils.isNotBlack(this.pic)) {
                String pic_line = String.format(PIC_FORMAT, this.pic);
                stream.write(pic_line);
                stream.newLine();
            }
        }
    }

    public String getPic() {
        return pic;
    }

    public String getTitle() {
        return title;
    }

    public String[] getTags() {
        return tags;
    }

    public String[] getLines() {
        return lines;
    }

    public boolean[] getIsWrite() {
        return isWrite;
    }


    /**
     * 处理字符串的
     */
    private static String handlerPic(String pic) {
        int length = MD._PIC.length() + 1;
        return pic.substring(length);
    }


    private static String handlerData(String data) {
        int length = MD._Date.length() + 1;
        return data.substring(length);
    }


    private static String[] handlerTag(String tags) {
        int length = MD._TAGS.length() + 1;
        String s = tags.substring(length);

        String str = s.substring(1, s.length() - 1);

        return StringUtils.split(str, ',', false);
    }

    private static String handlerTitle(String title) {
        String[] split = StringUtils.split(title, '\'', false);
        if (split.length == 2) {
            return split[1];
        }
        return title;
    }
}
