package com.venus.esb.utils;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.venus.esb.lang.ESBConsts;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by lingminjun on 17/4/23.
 */
public final class FileUtils {

    /**
     * 查找当前工程里面的某个类
     * @param clazz
     * @return
     */
    public static File findProjectJavaFile(Class<?> clazz) {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if (path == null) {
            return null;
        }

        //一般build运行在/target/xxx-classes/下面,考虑到工程可能是两级module结构,所以先从第一级找起
        File file = new File(path);       //
        file = file.getParentFile();
        if (file == null) {
            return null;
        }
        file = file.getParentFile();
        if (file == null) {
            return null;
        }

        //若此时还未找到,则向上寻找
        String className = clazz.getSimpleName();
        String fileName = className + ".java";

        //需要注意包名特征
        String packagePath = clazz.getName();
        packagePath = packagePath.replace('.',File.separatorChar) + ".java";

        File target = findFiles(file.getAbsolutePath(),fileName,packagePath);
        if (target != null) {
            return target;
        }

        file = file.getParentFile();
        if (file == null) {
            return null;
        }
        return findFiles(file.getAbsolutePath(),fileName,packagePath);
    }

    public static String readFile(InputStream in, Charset encoding) throws IOException {
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
            return new String(out.toByteArray(), encoding);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        InputStream in = null;
        try {

            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                return null;
            }

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
            return new String(out.toByteArray(), encoding);
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
//        byte[] encoded = Files.readAllBytes(Paths.get(path));
//        return new String(encoded, encoding);
    }

    public static boolean writeFile(String path, String content, Charset encoding) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(content.getBytes(encoding));
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

    public static boolean deleteFile(String path) throws IOException {
        try {
            File file = new File(path);
            if(file.delete()) {
                System.out.println( file.getName() + " is deleted!");
            }else {
                System.out.println("Delete operation is failed.");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }

//    public static String readFileV2(String filePath, Charset encoding) throws IOException {
//        FileInputStream in = null;
//        ByteArrayOutputStream out = null;
//        try {
//            // 获取源文件和目标文件的输入输出流
//            in = new FileInputStream(filePath);
//            out = new ByteArrayOutputStream();
//            // 获取输入输出通道
//            FileChannel fcIn = in.getChannel();
//            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            while (true) {
//                // clear方法重设缓冲区，使它可以接受读入的数据
//                buffer.clear();
//                // 从输入通道中将数据读到缓冲区
//                int r = fcIn.read(buffer);
//                if (r == -1) {
//                    break;
//                }
//                // flip方法让缓冲区可以将新读入的数据写入另一个通道
//                buffer.flip();
//
//                fcOut.write(buffer);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (in != null && out != null) {
//                try {
//                    in.close();
//                    out.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//    }


    private static HashMap<String,String> cache = new HashMap<String, String>();
    public static String[] methodParamNames(Class<?> clazz, String methodName, int varCount) {

        String content = cache.get(clazz.getName());

        if (content == null) {
            File file = findProjectJavaFile(clazz);
            if (file == null) {
                return null;
            }


            try {
                content = readFile(file.getAbsolutePath(), ESBConsts.UTF8);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            if (StringUtils.isEmpty(content)) {
                return null;
            }

            cache.put(clazz.getName(),content);
        }

        String[] strs = null;
        int from = 0;
        do {
            int idx = content.indexOf(methodName+"(",from);
            if (idx < 0 || idx > content.length()) {
                return null;
            }

            String methodString = content.substring(idx + methodName.length() + 1);

            int end = methodString.indexOf(")");
            if (end < 0 || end > methodString.length()) {
                return null;
            }

            methodString = methodString.substring(0,end).trim();
            if (StringUtils.isEmpty(methodString)) {
                return null;
            }

            strs = methodString.split(",");//有注释就不好办了

            from = idx + end + 1;
        } while (strs.length != varCount && from < content.length());//防止死循环



        String[] names = new String[strs.length];

        for (int i = 0; i < strs.length; i++) {
            //此处就简单支持好了,用空格做分隔,第一个合法字符串就做名字
            //todo: 对于参数中间有注释的,/* 注释 */ 暂时先不做支持
            String str = strs[i];
            String[] ss = str.trim().split(" ");
            for (int j = ss.length; j > 0; j--) {
                String s = ss[j-1];
                if (verifyParamName(s)) {
                    names[i] = s;
                    break;
                }
            }
        }

        return names;
    }

    private static boolean verifyParamName(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }

        char c = name.charAt(0);
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
            Pattern pattern = Pattern.compile("[0-9|a-z|A-Z|_]*");
            return pattern.matcher(name).matches();
        }

        return false;
    }
    /**
     * 递归查找文件
     * @param baseDirName  查找的文件夹路径
     * @param targetFileName  需要查找的文件名
     */
    public static File findFiles(String baseDirName, String targetFileName, String packagePath) {
        File baseDir = new File(baseDirName);       // 创建一个File对象
        if (!baseDir.exists() || !baseDir.isDirectory()) {  // 判断目录是否存在
            System.out.println("文件查找失败：" + baseDirName + "不是一个目录！");
        }
        String tempName = null;
        //判断目录是否存在
        File tempFile = null;
        File[] files = baseDir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                tempFile = files[i];
                if (tempFile.isDirectory()) {

                    //隐藏文件直接忽略
                    if (tempFile.getName().startsWith(".")) {
                        continue;
                    }

                    File tempp = findFiles(tempFile.getAbsolutePath(), targetFileName, packagePath);
                    if (tempp != null) {
                        return tempp;
                    }
                } else if (tempFile.isFile()) {
                    tempName = tempFile.getName();
                    if (tempName.equals(targetFileName)) {
                        if (packagePath != null && tempFile.getAbsolutePath().endsWith(packagePath)) {
                            return tempFile;
                        }

                        if (packagePath == null) {
                            return tempFile;
                        }
                    }
                }
            }
        }
        return null;
    }
}
