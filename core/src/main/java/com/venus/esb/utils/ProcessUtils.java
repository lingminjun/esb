package com.venus.esb.utils;

import com.venus.esb.brave.HttpBrave;
import com.venus.esb.lang.ESBT;
import org.springframework.core.io.UrlResource;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by lingminjun on 17/9/17.
 */
public final class ProcessUtils {
    // fork java进程
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
    private static String exitChildProcess(Class<?> clz, boolean limit) {
        String out = null;
        try {
            //mac不支持ps -x
            //ps -aux | grep com.venus.esb.brave.HttpBrave | grep -v grep
            out = ProcessUtils.exec("ps -ef | grep \"" + clz.getName() + "\" | grep -v grep",10*1000);
        } catch (Throwable e) {
            e.printStackTrace();//直接退出
            return null;
        }

        String processId = null;
        if (!ESBT.isEmpty(out)) {
            String[] lines = out.split(System.lineSeparator());
            //保留第一个
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (ESBT.isEmpty(line)) {
                    continue;
                }
                String[] ss = line.split("\\s+");
                String pid = ss[1];
                if (i == 0) {
                    processId = pid;
                }

                if (!limit) {
                    break;
                }

                //停止多余进程
                if (i > 0 && limit) {
                    try {
                        ProcessUtils.exec("kill " + pid, 5000);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }
        return processId;
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
        String pid = exitChildProcess(clz,true);
        if (!ESBT.isEmpty(pid)) {
            System.out.println("已经存在相似进程 " + pid + " ，不重新启动，详细： java -cp " + classpath + " " + clz.getName());
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
        pb.redirectErrorStream(false);

        p = pb.start();

        System.out.println(pb.command());

        if (p != null) {
            System.out.println("brave进程启动完成!!");
        } else {
            System.out.println("brave进程启动失败!!");
        }

        if (debug) {
            // process error and output message
            StreamWatch outputWatch = new StreamWatch(p, true);
            // start to watch
            outputWatch.start();

            //wait for exit
            int exitVal = p.waitFor();
        }
    }

    public static String exec(String cmd, int timeout) throws IOException {

        if (cmd == null || cmd.trim().length() == 0) {
            return null;
        }

//        String[] cmds = cmd.split("\\s+");
        // build my command as a list of strings
        List<String> command = new ArrayList<String>();
        command.add("/bin/sh");
        command.add("-c");
        command.add(cmd);
//        for (String c : cmds) {
//            command.add(c);
//        }
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);

        Process p = pb.start();

        StreamWatch outputWatch = new StreamWatch(p, true);
        System.out.println(pb.command());

        outputWatch.start();
        List<String> outs = outputWatch.waitAllOuts();
        StringBuilder builder = new StringBuilder();
        for (String line : outs) {
            builder.append(line);
            builder.append("\n");
        }
        return builder.toString();
    }

    static class StreamWatch extends Thread {
        private Process process;
        private BufferedReader is;
        private BufferedReader es;
        private BlockingQueue<String> outs = new LinkedBlockingQueue(100);
        private BlockingQueue<String> errs = new LinkedBlockingQueue(100);
        private boolean debug = false;
        private boolean exit = false;

        StreamWatch(Process process) {
            this(process, false);
        }

        StreamWatch(Process process, boolean debug) {
//            this.is = process.getInputStream();
//            this.es = process.getErrorStream();
            this.process = process;
            // 获取标准输出
            this.is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // 获取错误输出
            this.es = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            this.debug = debug;
        }

        public void run() {
            try {
                String line = null;
                String err = null;
                while((line = this.is.readLine()) != null || (err = this.es.readLine()) != null){
                    if (line != null) {
                        outs.offer(line);
                    }
                    if (err != null) {
                        errs.offer(err);
                    }
                    if (debug) {
                        if (line != null) {
                            System.out.println("OUTPUT >" + line);
                        }
                    }
                    if (err != null) {
                        System.out.println("ERROR >" + err);
                    }
                    line = null;
                    err = null;
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                exit = true;
                this.process.destroy();
                try {
                    this.is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    this.es.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public String waitOutLine() {
            try {
                return outs.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public List<String> waitAllOuts() {
            List<String> list = new ArrayList<>();
            while (!outs.isEmpty()) {
                String line = outs.poll();
                if (line != null) {
                    list.add(line);
                }
            }
            while (!exit) {
                String line = outs.poll();
                if (line != null) {
                    list.add(line);
                }
            }
            return list;
        }

        public String waitError() {
            try {
                return errs.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public List<String> waitAllErrors() {
            List<String> list = new ArrayList<>();
            while (!errs.isEmpty()) {
                String line = errs.poll();
                if (line != null) {
                    list.add(line);
                }
            }
            while (!exit) {
                String line = errs.poll();
                if (line != null) {
                    list.add(line);
                }
            }
            return list;
        }
    }
}
