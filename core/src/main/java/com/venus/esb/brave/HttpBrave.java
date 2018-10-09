package com.venus.esb.brave;

import com.venus.esb.lang.ESBConsts;
import com.venus.esb.utils.GZIP;
import okhttp3.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by lingminjun on 17/9/15.
 * 日志上报client,单独进程
 */
public final class HttpBrave {


    public static void main(String[] vars) {

        if (vars == null || vars.length < 2) {
            return;
        }

        String zipkinHost = vars[0];
        String braveDir = vars[1];

        if (zipkinHost == null || zipkinHost.length() == 0) {
            return;
        }

        System.out.println("监听进程成功启动:" + zipkinHost + ";" + braveDir);
        String url = zipkinHost + (zipkinHost.endsWith("/")?"":"/") + "api/v1/spans";

        String dirPath = braveDir;
        if (dirPath == null || dirPath.length() == 0) {
            dirPath = System.getProperties().getProperty("user.home") + "/logs/brave/";
            if (!File.separator.equals("/")) {//防止windows
                dirPath = System.getProperties().getProperty("user.home") + "\\logs\\brave\\";
            }
        } else if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }

        //检查是否有其他上报进程在

        while (true) {
            try {
                working(url, dirPath);
                System.out.println("brave once");
                Thread.sleep(60*1000);//延迟一分钟后继续检查
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static OkHttpClient _client;
    private static OkHttpClient getHttpClient() {
        if (_client != null) {
            return _client;
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5000, TimeUnit.MILLISECONDS);
        builder.readTimeout(5000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(120000, TimeUnit.MILLISECONDS);

        _client = builder.build();

        return _client;
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static boolean post(String url, String json) throws IOException {
        if (!json.startsWith("[")) {
            json = "[" + json + "]";
        }

//        return postV2(url,json);


        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = getHttpClient().newCall(request).execute();
        String result = response.body().string();
        com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSON.parseObject(result);
        if (object != null) {
            Integer status = object.getInteger("status");
            if (status != null && (status == 200 || status == 202)) {
                return true;
            }
        } else if (object == null) {
            return true;
        }
        System.out.println("report error:" + result);
        return false;

    }

    /*
    public static boolean postV2(String url, String ajson) throws IOException {
        if (!ajson.startsWith("[")) {
            ajson = "[" + ajson + "]";
        }

        HttpURLConnection connection = (HttpURLConnection)(new URL(url)).openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(3000);
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.addRequestProperty("Content-Encoding", "gzip");

        //获得压缩的结果
        byte[] json = GZIP.compress(ajson,ESBConsts.UTF8_STR);


        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(json.length);
        connection.getOutputStream().write(json);
        connection.getOutputStream().flush();
        connection.getOutputStream().close();

        InputStream e1 = connection.getInputStream();//实际请求发生点

        if (e1 != null) {
            String result = IOUtils.toString(e1, ESBConsts.UTF8_STR);

            try {
                e1.close();
            } catch (Throwable var51) {
                var51.printStackTrace();
            }

            com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSON.parseObject(result);
            if (object != null) {
                Integer status = object.getInteger("status");
                if (status != null && (status == 200 || status == 202)) {
                    return true;
                }
            } else if (object == null) {
                return true;
            }
            System.out.println("report error:" + result);

        } else {

            InputStream err1 = connection.getErrorStream();

            String result = IOUtils.toString(err1, ESBConsts.UTF8_STR);
            try {
                err1.close();
            } catch (Throwable var49) {
                var49.printStackTrace();
            }
            System.out.println("report error:" + result);
        }

        return false;
    }*/

    private static void lookDir(String url, File dir, boolean removeEmpty) {
        if (dir == null) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            if (removeEmpty) {
                dir.delete();
            }
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
//                lookDir(url, file, true);
                lookDir(url, file, false);//若多个上报进程同时存在,则会出现异常
            } else if (file.canRead()) {
                if (!file.getName().endsWith(".log")) {
                    continue;
                }

                String lck = file.getAbsolutePath() + ".lck";
                if (new File(lck).exists()) {//正在写入
                    continue;
                }

                boolean result = false;
                try {
                    result = reportFile(url,file);
                } catch (Throwable e) {e.printStackTrace();}

                if (result) {
                    System.out.println("report file:" + file);
                    file.delete();//删除文件
                }
            }
        }
    }

    private static boolean reportFile(String url, File file) throws IOException {
        String json = null;
        try {
            json = readFile(file, ESBConsts.UTF8_STR);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //没内容
        if (json == null || json.length() == 0) {
            return true;
        }

        json = json.trim();//去掉收尾符号

        if (json.endsWith(",")) {
            json = json.substring(0,json.length() - 1);
        }
        String msg = "[" + json + "]";

        return post(url,msg);
    }


    private static String readFile(File file, String encoding) throws IOException {
        InputStream in = null;
        try {
            // 一次读多个字节
            byte[] tempbytes = new byte[1024];
            int byteread = 0;
            in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            while ((byteread = in.read(tempbytes)) != -1) {
                out.write(tempbytes,0,byteread);
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
    }

    /**
     * 日志文件内容采用json格式存放:{},{},
     */
    private static void working(String url, String dir) throws IOException {

        //遍历目录
        File df = new File(dir);
        if (!df.exists()) {
            return;
        }

        //开始循环遍历目录
        lookDir(url,df,false);

    }
}
