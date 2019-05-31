package com.venus.esb;


import com.venus.esb.config.ESBConfigCenter;
import com.venus.esb.lang.*;
import com.venus.esb.utils.MD5;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



/**
 * Created by lingminjun on 17/6/13.
 * 默认的风控嗅探器,简单粗暴,仅仅针对无验证的方法和ip频次做了识别
 * ip + method.none 1分钟内60次 就提示11,返回需要图片验证码
 * ip + method.none 1分钟内10000次 就直接拒绝
 */
public class ESBAPIRisky implements ESB.APIRisky {

//    private static Logger logger  = LoggerFactory.getLogger(ESBAPIRisky.class);
    private static final long DANGER_THRESHOLD = 1000;
    private static final long DENIED_THRESHOLD = 2000;
    private static final long FILE_CACHE_DURATION = 30000;
    private static final String SPLIT_STRING = ", ";


    @Override
    public ESB.APIRiskyLevel sniffer(ESB esb, ESBAPIInfo info, ESBAPIContext context, Map<String, String> params, Map<String, ESBCookie> cookies) throws ESBException {

        // 自动验证(说明提交了验证码)
        if (!ESBT.isEmpty(context.captcha)) {
            String salt = context.dtoken;
            if (ESBT.isEmpty(salt)) {
//                if (ESBT.isEmpty(context.did)) {
//                    salt = context.did;
//                } else {
                    salt = context.did + context.ua;
//                }
            }
            String code = context.captcha;
            String session = context.getRightValue(ESBSTDKeys.CAPTCHA_SESSION_KEY,params,cookies,-1);

            boolean result = context.verifyCaptcha(salt,code,session);
            if (result) {

                // FIXME: 清除风控记录（ip或did或uid）

                return ESB.APIRiskyLevel.SAFETY;
            } else {
                throw ESBExceptionCodes.CAPTCHA_ERROR("验证错误");
            }
        }


        //5分钟启动一次检查

        //非None的权限,后面都有对应的权限验证,所以不受此风控影响
        if (!ESBSecurityLevel.isNone(info.api.security)) {
            return ESB.APIRiskyLevel.SAFETY;
        }


        //开始检查黑白名单
        //http://zhensheng.im/2013/11/16/2050/MIAO_LE_GE_MI
        ESB.APIRiskyLevel result = sniffer(context.cip);
        //直接异常抛出去拒绝访问
        if (result == ESB.APIRiskyLevel.DENIED) {
            throw ESBExceptionCodes.ACCESS_DENIED("ip被检查到访问过于频发,拒绝访问");
        } else if (result == ESB.APIRiskyLevel.DANGER) {

            //如果此时发现已经提交验证码
            if (!ESBT.isEmpty(context.captcha)) {
                //进一步验证验证码
                //TODO : 进一步验证验证码
            }

            throw ESBExceptionCodes.NEED_CAPTCHA("ip检查到访问频繁,需要进一步确认");
        }

        return result;
    }

    /**
     * 校验ip
     * @param ip
     * @return
     */
    private ESB.APIRiskyLevel sniffer(String ip) {
        if (ESBT.isEmpty(ip)) {//直接不允许无ip访问
            return ESB.APIRiskyLevel.DENIED;
        }

        Properties props=System.getProperties(); //系统属性
        String bk_path = props.getProperty("user.home") + File.separator + ".risk_monitor" + File.separator + "ip_tables.log";
        String wt_path = props.getProperty("user.home") + File.separator + ".risk_monitor" + File.separator + "wt_ips.log";

        //先判断白名单列表
        if (inWhiteIPTablesFile(wt_path,ip)) {
            return ESB.APIRiskyLevel.SAFETY;
        }

        //再处理黑名单
        return readIPTablesFile(bk_path,ip);
    }

    private boolean inWhiteIPTablesFile(String path, String ip) {
        boolean rt = false;
        try {

            File file = new File(path);
            if (!file.exists()) {
                rt = false;
            } else {
                FileReader reader = new FileReader(path);
                BufferedReader br = new BufferedReader(reader);

                String str = null;
                while ((str = br.readLine()) != null) {
                    if (str.equals(ip)) {
                        rt = true;
                        break;
                    }
                }

                br.close();
                reader.close();
            }
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return rt;
    }

    private volatile ESBVolatileReference<HashMap<String,Long>> blacks = null;//做局部缓存,提高效率,减少io
    private ESB.APIRiskyLevel readIPTablesFile(String path, String ip) {
        ESB.APIRiskyLevel rt = ESB.APIRiskyLevel.SAFETY;
        try {
            long now = System.currentTimeMillis();
            ESBVolatileReference<HashMap<String,Long>> node = blacks;
            if (node != null && now <= node.getCreateAt() + FILE_CACHE_DURATION) {//一分钟更换一次
                Long value = node.get().get(ip);
                if (value != null) {
                    return judgeTimes(value,DANGER_THRESHOLD,DENIED_THRESHOLD);
                } else {
                    return ESB.APIRiskyLevel.SAFETY;
                }
            }

            File file = new File(path);
            //文件不存在
            if (!file.exists()) {
                blacks = null;
                return ESB.APIRiskyLevel.SAFETY;
//                logger.info("未获取到risk black配置文件! {}",path);
            }

            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);

            String str = null;
            HashMap<String,Long> ips = new HashMap<String,Long>();

            //将数据全部读入
            while ((str = br.readLine()) != null) {

                if (str == null) {
                    continue;
                }

                String strs[] = str.split(SPLIT_STRING);
                if (strs.length == 2) {
                    long value = ESBT.longInteger(strs[1], 0);
                    if (value > 0) {
                        ips.put(strs[0],value);
                    }
                }
            }

            //验证次数合法性
            Long value = ips.get(ip);
            if (value != null) {
                rt = judgeTimes(value,DANGER_THRESHOLD,DENIED_THRESHOLD);
            }

            br.close();
            reader.close();

            //数据构造完在缓存
            blacks = new ESBVolatileReference<HashMap<String, Long>>(ips);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return rt;
    }

    private ESB.APIRiskyLevel judgeTimes(long value, long ok, long denied) {
        if (value < ok) {
            return ESB.APIRiskyLevel.SAFETY;
        } else if (value >= denied) {
            return ESB.APIRiskyLevel.DENIED;
        } else {
            return ESB.APIRiskyLevel.DANGER;
        }
    }

    /**
     * 写一个脚本文件,然后再运行,防止cmd在本jar中,直接不启动
     * @param path
     * @param cmd
     */
    private void wirteShellScriptFile(String path, String cmd) {
        try {

            File file = new File(path);
            if (file.exists()) {
                return;
            }
            // write string to file
            FileWriter writer = new FileWriter(path);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write("#!/bin/sh");//设置脚本头
            bw.write("\n\n");
            bw.write(cmd);
            bw.write("\n\n");
            bw.close();
            writer.close();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void startRiskMonitor() {
        try {
            //先检查目标java文件是否被编译
            Properties props=System.getProperties(); //系统属性

            String jar_path = props.getProperty("user.dir") + File.separator + props.getProperty("java.class.path");

            String cmd = "java -classpath "+ jar_path +" ESBAPIRisky abc";
            String path = props.getProperty("user.home") + File.separator + ".risk_monitor";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
            path = path + File.separator + "start.sh";

            wirteShellScriptFile(path,cmd);
            //另起进程来完成监控任务
            System.out.println("begin:" + "/bin/sh " + path);
            Process process = Runtime.getRuntime().exec("/bin/sh " + path);
            if (process != null) {
                byte[] bytes = new byte[1024];
                int k = process.getInputStream().read(bytes, 0, bytes.length);
                if (k > -1) {
                    System.out.println("成功启动监控进程");
                }
            }
            System.out.println("end:" + "/bin/sh " + path);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
//            logger.error("ESB Risky 监控启动失败, 请手动启动脚本", e);
        }
    }

    public static void main(String[] args) {
        if (args != null && args.length >= 1) {
            System.out.println("确定是脚本启动方式");
        } else {
            System.out.println("非脚本启动方式");
        }

        ESBAPIRisky risky = new ESBAPIRisky();
        risky.startRiskMonitor();

//        String str = "10.32.42.13, 3344";
//        String strs[] = str.split(", ");
//        System.out.println(strs[1]);
    }
}
