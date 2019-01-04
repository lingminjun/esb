package com.venus.esb.utils;

import org.springframework.core.io.UrlResource;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by lingminjun on 17/9/17.
 */
public final class ProcessUtils {
    public static void fork(final Class<?> clz, final String[] vars, boolean async, final boolean debug) {
        if (clz == null) {
            return;
        }

        if (async) {
            new Thread() {
                @Override
                public void run() {
                    fork(clz,vars,false,debug);
                }
            }.start();
            return;
        }

        try {
            forkProcess(clz,vars,debug);
        } catch (Throwable e) {e.printStackTrace();}
    }

    //判断子进程是否已经存在
    private static boolean exitChildProcess(Class<?> clz) {
        /*
        ProcessBuilder pb = new ProcessBuilder("ps -ef | grep \" " + clz.getName() + " \" | grep -qv grep");
        try {
            Process p = pb.start();

        } catch (Throwable e) {
            e.printStackTrace();
        }
        */
        return false;
    }

    private static String getClassPath() {
        String classpath = System.getProperty("java.class.path");
        //当前是Web Application
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
//        if (loader.getClass().getSimpleName().equals("WebappClassLoader") // tomcat7
//                || loader.getClass().getSimpleName().equals("ParallelWebappClassLoader") //tomcat8
//                || loader.getClass().getSimpleName().equals("WebappClassLoaderBase")) {
        if (loader.getClass().getSimpleName().contains("WebappClassLoader")) {// tomcat7和tomcat8使用的ebappClassLoader不一样
            try {
                URL url = loader.getResource("../lib");
                if (url != null) {
//                    classpath = url.getPath() + "*.jar";
                    File file = new File(url.getPath());
                    File[] fls = file.listFiles();
                    StringBuilder builder = new StringBuilder(".");
                    for (File f : fls) {
                        //window和linux环境支持
                        if (File.separator.equals("/")) {
                            builder.append(":");
                        } else {
                            builder.append(";");
                        }
                        builder.append(f.getAbsolutePath());
                    }
                    classpath = builder.toString();
                }

//                Method method = loader.getClass().getMethod("getJarPath");
//                if (method != null) {
//                    String jarp = (String) method.invoke(loader,null);
//                    if (jarp != null && jarp.length() > 0) {
//                        classpath = url.getPath() + jarp.substring(1, jarp.length()) + File.separator + "*.jar";
//                    }
//                }
            } catch (Throwable e) {e.printStackTrace();}
        }
        return classpath;
    }

    private static void forkProcess(Class<?> clz, String[] vars, boolean debug) throws IOException, InterruptedException {
        List<String> list = new ArrayList<String>();
        ProcessBuilder pb = null;
        Process p = null;

        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

        //取classPath
        String classpath = getClassPath();

        //[/Library/Java/JavaVirtualMachines/jdk1.7.0_71.jdk/Contents/Home/jre/bin/java, -classpath, "/usr/local/tmcat7/bin/bootstrap.jar:/usr/local/tmcat7/bin/tomcat-juli.jar", com.venus.esb.brave.HttpBrave, http://127.0.0.1:9411/, /Users/lingminjun/logs/brave/]
        //针对tomcat单个进程,需要控制,并不需要启用多个上报进程
        if (exitChildProcess(clz)) {
            System.out.println("java -cp " + classpath + " " + clz.getName() + " 进程已经被启动");
            return;
        }

        // list the files and directorys under C:\
        list.add(java);
        list.add("-classpath");
        list.add("\""+classpath+"\"");
        list.add(clz.getName());

        if (vars != null) {
            for(String v : vars) {
                list.add(v);
            }
        }

        pb = new ProcessBuilder(list);

        p = pb.start();

        System.out.println(pb.command());

        if (p != null) {
            System.out.println("brave进程启动完成!!");
        } else {
            System.out.println("brave进程启动失败!!");
        }

        if (debug) {
            // process error and output message
            StreamWatch errorWatch = new StreamWatch(p.getErrorStream(), "ERROR", true);
            StreamWatch outputWatch = new StreamWatch(p.getInputStream(), "OUTPUT", true);
            // start to watch
            errorWatch.start();
            outputWatch.start();

            //wait for exit
            int exitVal = p.waitFor();
        }




//
            //print the content from ERROR and OUTPUT
//            System.out.println("ERROR: " + errorWatch.getOutput());
//            System.out.println("OUTPUT: " + outputWatch.getOutput());
//            System.out.println("the return code is " + exitVal);
    }

    public static String exec(String cmd, int timeout) throws IOException {

        if (cmd == null || cmd.trim().length() == 0) {
            return null;
        }

        String[] cmds = cmd.split("\\s");
        // build my command as a list of strings
        List<String> command = new ArrayList<String>();
        for (String c : cmds) {
            command.add(c);
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        Process p = pb.start();

        PrintWriter pw = null;
        InputStreamReader isr = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(isr);

        String line = null;
        long now = System.currentTimeMillis();
        long expired = now + timeout;
        while ((line = br.readLine()) != null) {
            if (line != null && line.length() > 0) {
                return line;
            }
            now = System.currentTimeMillis();
            if (now > expired) {
                break;
            }
        }

        return line;
    }

    static class StreamWatch extends Thread {
        InputStream is;
        String type;
//        List<String> output = new ArrayList<String>();
        boolean debug = false;

        StreamWatch(InputStream is, String type) {
            this(is, type, false);
        }

        StreamWatch(InputStream is, String type, boolean debug) {
            this.is = is;
            this.type = type;
            this.debug = debug;
        }

        public void run() {
            try {
//                PrintWriter pw = null;
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
//                    output.add(line);
                    if (debug) {
                        System.out.println(type + ">" + line);
                    }
                }
//                if (pw != null)
//                    pw.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

//        public List<String> getOutput() {
//            return output;
//        }
    }
}
