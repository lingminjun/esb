package com.venus.gen;

import com.venus.esb.lang.ESBT;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Description: 提供生成代码所属环境，工具方法
 * User: lingminjun
 * Date: 2018-06-12
 * Time: 下午11:30
 */
public abstract class Generator {

    public enum ProjectType {
        module/*普通jar*/,springboot,dubbo,servlet/*亦可是springmvc，表示为war包*/,parent
    }

    public static class ProjectModule {
        public final ProjectType projectType;
        public final String projectDir;
        public final String moduleName;
        public final String resourcesPath;
        public final String webappPath; //just projectType == .servlet
        public final String packageName;
        public final String packagePath;

        // just projectType == .parent 时才会有子module
        public final ProjectModule[] modules;

        public ProjectModule(ProjectType type, String dir, String name, String packageName, ProjectModule[] modules) {
            this.projectType = type;
            this.projectDir = dir;
            this.moduleName = name;
            this.resourcesPath = dir + File.separator + "src" + File.separator + "main" + File.separator + "resources";
            new File(this.resourcesPath).mkdirs();
            if (type == ProjectType.servlet) {
                this.webappPath = dir + File.separator + "src" + File.separator + "main" + File.separator + "webapp";
                new File(this.webappPath).mkdirs();
            } else {
                this.webappPath = null;
            }
            StringBuilder srcCodePath = new StringBuilder(dir + File.separator + "src" + File.separator + "main" + File.separator + "java");
            String[] pcks = packageName.split("\\.");
            for (String pck : pcks) {
                if (pck.length() > 0 && !pck.equals("/") && !pck.equals("\\")) {
                    srcCodePath.append(File.separator);
                    srcCodePath.append(pck);
                }
            }
            this.packagePath = srcCodePath.toString();
            new File(this.packagePath).mkdirs();
            this.packageName = packageName;
            this.modules = modules;
        }

        public ProjectModule findModuleForName(String name) {
            if (modules == null) {
                return null;
            }
            for (ProjectModule module : modules) {
                if (module.moduleName.equalsIgnoreCase(name)) {
                    return module;
                }
            }
            return null;
        }

        public ProjectModule findModuleForPath(String path) {
            if (modules == null) {
                return null;
            }
            for (ProjectModule module : modules) {
                if (module.projectDir.equals(path)) {
                    return module;
                }
            }
            return null;
        }

        public ProjectModule copyModule(String packageName) {
            return new ProjectModule(projectType,projectDir,moduleName,packageName == null ? this.packageName : packageName, modules);
        }
    }

    public final String packageName() {
        return this.project.packageName;
    }

    public final String projectDir() {
        return this.project.projectDir;
    }

    public final String resourcesPath() {
        return this.project.resourcesPath;
    }

    public final String packagePath() {
        return this.project.packagePath;
    }

    public final String webappPath() {
        return this.project.webappPath;
    }

    public final ProjectModule project;
    public final ProjectModule rootProject;

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

        //检查工程下或者父工程下有web.xml
        this.rootProject = parseRootProject(projectDir,packageName);
        if (this.rootProject.projectType == ProjectType.parent) {
            this.project = this.rootProject.findModuleForPath(projectDir);
        } else {
            this.project = this.rootProject;
        }
    }

    // 内部构造
    protected Generator(ProjectModule rootProject, ProjectModule project) {
        this.rootProject = rootProject;
        this.project = project;
    }

    private static boolean checkServletProject(String projectDir) {
        try {
            String pom = readFile(projectDir + File.separator + "pom.xml");
            int idx = pom.indexOf("<packaging>war</packaging>");
            if (idx >= 0 && idx < pom.length()) {//一定是servlet工程
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ProjectModule parseRootProject(String projectDir,String packageName) {
        try {
            String pom = readFile(projectDir + File.separator + "pom.xml");

            boolean hasParent = false;
            boolean isServlet = false;
            List<String> modules = new ArrayList<String>();
            String rootProjectDir = projectDir;

            int idx = pom.indexOf("<modules>");
            int noparent = pom.indexOf("<parent>");
            //当当前pom不是一个容器(parent)pom时，才去寻找父目录的pom
            if (noparent >= 0 && noparent < pom.length() && (idx < 0 || idx >= pom.length())) {
                //查找父控制器的pom，一定要在父目录下才能确定是有父工程，否则仅仅pom组织
                File file = new File(projectDir);
                File parent = file.getParentFile();

                String parentPom = null;
                try {
                    parentPom = readFile(parent.getAbsolutePath() + File.separator + "pom.xml");
                } catch (Throwable e) {}

                if (parentPom != null) {
                    hasParent = true;
                    rootProjectDir = parent.getAbsolutePath();
                    pom = parentPom;
                }
            }

            //查找modules
            idx = pom.indexOf("<modules>");
            if (idx >= 0 && idx < pom.length()) {//自身就是父容器，则不向上寻找
                int end = pom.indexOf("</modules>");
                String mds = pom.substring(idx+"<modules>".length(),end).trim();
                String[] ms = mds.split("</module>");
                for (String msStr : ms) {
                    String str = msStr.trim();
                    if (str.length() > 0 && str.startsWith("<module>")) {
                        modules.add(str.substring("<module>".length()));
                    }
                }
            }

            isServlet = checkIsServletModule(pom);
            String moduleName = findModuleName(pom);

            //遍历下一级
            ProjectType type = ProjectType.module;
            if (hasParent) {
                type = ProjectType.parent;

                //遍历模块

            } else if (isServlet) {
                type = ProjectType.servlet;
            } else if (ESBT.classForName("org.springframework.boot.SpringApplication") != null) {//不准
                type = ProjectType.springboot;
            } else if (ESBT.classForName("com.alibaba.dubbo.rpc.RpcContext") != null) {
                if (checkIsDubboModule(pom,projectDir)) {
                    type = ProjectType.dubbo;
                }
            }

            if (hasParent) {
                List<ProjectModule> moduleList = findModules(modules,rootProjectDir,packageName);
                return new ProjectModule(type,rootProjectDir,moduleName,packageName,moduleList.toArray(new ProjectModule[0]));
            } else {
                return new ProjectModule(type,rootProjectDir,moduleName,packageName,null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<ProjectModule> findModules(List<String> modules, String rootProjectDir, String packageName) {
        File file = new File(rootProjectDir);
        File[] files = file.listFiles();
        List<ProjectModule> moduleList = new ArrayList<ProjectModule>();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    continue;
                }

                String pom = null;
                try {
                    pom = readFile(f.getAbsolutePath() + File.separator + "pom.xml");
                } catch (Throwable e) {}
                if (pom == null || pom.length() == 0) {
                    continue;
                }

                String moduleName = findModuleName(pom);
                //说明是废弃的module
                if (!modules.contains(moduleName)) {
                    continue;
                }
                boolean isServlet = checkIsServletModule(pom);
                ProjectType type = ProjectType.module;
                if (isServlet) {
                    type = ProjectType.servlet;
                } else if (ESBT.classForName("com.alibaba.dubbo.rpc.RpcContext") != null) {
                    if (checkIsDubboModule(pom,f.getAbsolutePath())) {
                        type = ProjectType.dubbo;
                    }
                }

                ProjectModule module = new ProjectModule(type,f.getAbsolutePath(),moduleName,packageName,null);
                moduleList.add(module);
            }
        }
        return moduleList;
    }

    public static boolean checkIsServletModule(String pom) {
        int idx = pom.indexOf("<packaging>war</packaging>");
        if (idx >= 0 && idx < pom.length()) {//一定是servlet工程
            return true;
        }
        return false;
    }

    public static boolean checkIsDubboModule(String pom, String projectDir) {
        //src/main/
        File file = new File(projectDir + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "resources" + File.separator +
                "META-INF" + File.separator +
                "spring");
        return file.exists();
    }

    public static String findModuleName(String pom) {
        int idx = pom.indexOf("<artifactId>");
        if (idx < 0 || idx >= pom.length()) {
            return null;
        }

        String prefix = pom.substring(0,idx);
        int check = prefix.indexOf("<parent>");
        if (check >= 0 && check < pom.length()) {
            pom = pom.substring(idx + "<artifactId>".length());
            idx = pom.indexOf("<artifactId>");
            if (idx < 0 || idx >= pom.length()) {
                return null;
            }
            prefix = pom.substring(0,idx);
        }

        //说明没有找到合适的artifactId
        check = prefix.indexOf("<dependency>");
        if (check >= 0 && check < pom.length()) {
            return null;
        }

        // 取artifactId name
        pom = pom.substring(idx + "<artifactId>".length());
        int end = pom.indexOf("<");
        if (end >= 0 && end < pom.length()) {
            return pom.substring(0,end).trim();
        }

        return null;
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
        //从moduleName中获取
        if (this.rootProject != this.project) {
            String[] strs = this.project.moduleName.split("\\-");
            for (int i = strs.length; i > 0; i-- ) {
                if (!excludes.contains(strs[i-1])) {
                    return strs[i-1];
                }
            }
        }

        {
            String[] strs = this.rootProject.moduleName.split("\\-");
            for (int i = strs.length; i > 0; i--) {
                if (!excludes.contains(strs[i - 1])) {
                    return strs[i - 1];
                }
            }
        }

        //从packageName中获取（猜测）
        if (this.packageName() != null) {
            String[] strs = this.packageName().split("\\.");
            for (int i = strs.length; i > 0; i-- ) {
                if (!excludes.contains(strs[i-1])) {
                    return strs[i-1];
                }
            }
        }

        //从project名字中获取（猜测）
        if (this.projectDir() != null) {
            String[] strs = this.projectDir().split("\\-");
            for (int i = strs.length; i > 0; i-- ) {
                String s = strs[i-1];
                String[] ss = s.split("_");
                for (int j = ss.length; j > 0; j-- ) {
                    if (!excludes.contains(ss[j-1])) {
                        return strs[j-1];
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

    public static String toUpperHeadString(String string) {
        if (string.length() == 0) {
            return string;
        } else if (string.length() == 1) {
            return string.toLowerCase();
        }
        String head = string.substring(0,1);
        String end = string.substring(1,string.length());
        return head.toUpperCase() + end;
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
            if (!(new File(path).exists())) {
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
