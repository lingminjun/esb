package server;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Description: 提供生成代码所属环境，工具方法
 * User: lingminjun
 * Date: 2018-06-12
 * Time: 下午11:30
 */
public abstract class Generator {

    public final String packageName;
    public final String projectDir;

    public final String resourcesPath;
    public final String packagePath;

    public Generator(String packageName) {
        this(packageName,null);
    }

    public Generator(String packageName, String projectDir) {
        //工程目录
        File projFile = getCurrentProjectDirFile();
        if (projectDir == null || projectDir.length() == 0) {
            projectDir = projFile.getAbsolutePath();
        }

        if (projectDir.endsWith(File.separator)) {
            projectDir = projectDir.substring(0,projectDir.length() - 1);
        }
        this.packageName = packageName;
        this.projectDir = projectDir;

        //包名路径
        String[] pcks = packageName.split("\\.");
        StringBuilder srcBuilder = new StringBuilder(projectDir);
        srcBuilder.append(File.separator);
        srcBuilder.append("src");
        srcBuilder.append(File.separator);
        srcBuilder.append("main");
        resourcesPath = srcBuilder.toString() + File.separator + "resources";
        new File(resourcesPath).mkdirs();

        srcBuilder.append(File.separator);
        srcBuilder.append("java");
        for (String pck : pcks) {
            if (pck.length() > 0 && !pck.equals("/") && !pck.equals("\\")) {
                srcBuilder.append(File.separator);
                srcBuilder.append(pck);
            }
        }
        packagePath = srcBuilder.toString();
        new File(packagePath).mkdirs();
    }

    private static Set<String> excludes = new HashSet<String>();
    static {
        excludes.add("com");

        excludes.add("core");
        excludes.add("gen");
        excludes.add("generator");
        excludes.add("utils");
        excludes.add("util");
        excludes.add("db");
        excludes.add("dao");
        excludes.add("persistence");

        excludes.add("test");
        excludes.add("autotest");

        excludes.add("api");
        excludes.add("service");
        excludes.add("manager");
        excludes.add("config");

        excludes.add("caller");
        excludes.add("esb");
        excludes.add("parent");
        excludes.add("lmj");
        excludes.add("venus");//通用的不参与
    }

    public final String getProjectSimpleName() {
        //从packageName中获取（猜测）
        if (this.packageName != null) {
            String[] strs = this.packageName.split("\\.");
            for (int i = strs.length; i >= 0; i-- ) {
                if (!excludes.contains(strs[i])) {
                    return strs[i];
                }
            }
        }

        //从project名字中获取（猜测）
        if (this.projectDir != null) {
            String[] strs = this.projectDir.split("\\-");
            for (int i = strs.length; i >= 0; i-- ) {
                String s = strs[i];
                String[] ss = s.split("_");
                for (int j = ss.length; j >= 0; j-- ) {
                    if (!excludes.contains(ss[j])) {
                        return strs[j];
                    }
                }
            }
        }

        return "";
    }

    public abstract boolean gen();

    //转驼峰命名
    public static String toHumpString(String string, boolean head) {
        StringBuilder name = new StringBuilder();
        boolean toUpper = head;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (toUpper) {
                name.append(("" + c).toUpperCase());
                toUpper = false;
            } else if (c == '_') {
                toUpper = true;
            } else {
                name.append(c);
            }
        }
        return name.toString();
    }

    public static String toLowerHeadString(String string) {
        if (string.length() == 0) {
            return string;
        } else if (string.length() == 1) {
            return string.toLowerCase();
        }
        String head = string.substring(0,1);
        String end = string.substring(1,string.length());
        return head.toLowerCase() + end;
    }

    public static File getCurrentProjectDirFile() {
//        String filePath = System.getProperty("user.dir");//当前运行目录[可能是根目录]
        //当前运行目录
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String path = url.getPath();
        File file = new File(path);
        return file.getParentFile().getParentFile();
    }

    public static String getCurrentProjectDir() {
//        String filePath = System.getProperty("user.dir");//当前运行目录[可能是根目录]
        //当前运行目录
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String path = url.getPath();
        File file = new File(path);
        return file.getParentFile().getParent();
    }

    public static String getSqlsContent(String sqlsSourcePath) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(sqlsSourcePath);
        String content = null;
        try {
            content = readFile(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    protected static String readFile(InputStream in) throws IOException {
        try {
//            System.out.println("以字节为单位读取文件内容，一次读多个字节：");
            // 一次读多个字节
            byte[] tempbytes = new byte[1024];
            int byteread = 0;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ReadFromFile.showAvailableBytes(in);
            // 读入多个字节到字节数组中，byteread为一次读入的字节数
            while ((byteread = in.read(tempbytes)) != -1) {
                out.write(tempbytes,0,byteread);
//                System.out.write(tempbytes, 0, byteread);
            }
            return new String(out.toByteArray(), "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    protected static String readFile(String path) throws IOException {
        InputStream in = null;
        try {
//            System.out.println("以字节为单位读取文件内容，一次读多个字节：");
            // 一次读多个字节
            byte[] tempbytes = new byte[1024];
            int byteread = 0;
            in = new FileInputStream(path);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ReadFromFile.showAvailableBytes(in);
            // 读入多个字节到字节数组中，byteread为一次读入的字节数
            while ((byteread = in.read(tempbytes)) != -1) {
                out.write(tempbytes,0,byteread);
//                System.out.write(tempbytes, 0, byteread);
            }
            return new String(out.toByteArray(), "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    protected static boolean writeFile(File filePath, String content) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            out.write(content.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return true;
    }

    protected static boolean writeFile(String path, String content) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(content.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return true;
    }

    protected static String formatSpaceParam(String methodFragment) {
        int idx = methodFragment.indexOf("(");
        if (idx <= 0 || idx > methodFragment.length()) {
            return "";
        }

        StringBuilder builder = new StringBuilder(" ");
        for (int i = 0; i < idx; i++) {
            builder.append(" ");
        }

        return builder.toString();
    }

}
