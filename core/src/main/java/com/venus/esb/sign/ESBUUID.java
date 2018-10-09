package com.venus.esb.sign;

import com.venus.esb.lang.ESBConsts;
import com.venus.esb.lang.ESBException;
import com.venus.esb.lang.ESBExceptionCodes;
import com.venus.esb.lang.ESBT;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lingminjun on 17/5/6.
 */
public final class ESBUUID {
    // 基准时间
    private long twepoch = TIME_2018_01_01; //1483200000000L; //2017/1/1 00:00:00
    // 区域标志位数
    private final static long regionIdBits = 3L;
    // 机器标识位数
    private final static long workerIdBits = 10L;
    // 序列号识位数
    private final static long sequenceBits = 10L;

    // 区域标志ID最大值
    private final static long maxRegionId = -1L ^ (-1L << regionIdBits);
    // 机器ID最大值
    private final static long maxWorkerId = -1L ^ (-1L << workerIdBits);//1023
    // 序列号ID最大值
    private final static long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 机器ID偏左移10位
    private final static long workerIdShift = sequenceBits;
    // 业务ID偏左移20位
    private final static long regionIdShift = sequenceBits + workerIdBits;
    // 时间毫秒左移23位
    private final static long timestampLeftShift = sequenceBits + workerIdBits + regionIdBits;

    private static long lastTimestamp = -1L;

    private long sequence = 0L;
    private final long workerId;
    private final long regionId;

    /**
     * long行uuid
     * @return
     */
    public static long commonGenerate() {
        return SingletonHolder.INSTANCE.generate();
    }

    /**
     * uuid
     * @return
     */
    public static String generateNormalUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * local_id_hash|processId|threadId|timestamp 精简版 hexString 32位
     * 唯一性
     * @return
     */
    public static String genSimplifyCID() {
        // ip 8位
        // pid 4位 一般PID_MAX=0x8000（可改），因此进程号的最大值为0x7fff，即32767。
        // threadid 4位 0xffff = 65536
        // 16位 timestamp - 2018-01-01 00:00:00.000 = timestamp - 1483200000000 =
        return getLocalIPLowerHexString()
                + getProcessIDLowerHexString()
                + getThreadIDLowerHexString()
                + getTimestampLowerHexString();
    }

    /**
     * 将精简版cid转明文
     * @param cid
     * @return
     */
    public static String convertProclaimedCID(long cid) {
        return convertProclaimedCID(String.format("%032x",cid));
    }
    public static String convertProclaimedCID(String cid) {
        if (cid == null || cid.length() != 32) {return cid;}
        StringBuilder builder = new StringBuilder();
        builder.append(Long.parseLong(cid.substring(0,2),16));
        builder.append(".");
        builder.append(Long.parseLong(cid.substring(2,4),16));
        builder.append(".");
        builder.append(Long.parseLong(cid.substring(4,6),16));
        builder.append(".");
        builder.append(Long.parseLong(cid.substring(6,8),16));
        builder.append("|");
        builder.append(Long.parseLong(cid.substring(8,12),16));
        builder.append("|");
        builder.append(Long.parseLong(cid.substring(12,16),16));
        builder.append("|");
        builder.append(TIME_2018_01_01 + Long.parseLong(cid.substring(16,32),16));
        return builder.toString();
    }

    /**
     * 将明文cid转精简版
     * @param cid
     * @return
     */
    public static String convertSimplifyCID(String cid) {
        if (cid == null || cid.length() == 0) {return null;}
        String[] strs = cid.split("\\|");
        if (strs.length != 4) {return null;}
        String[] ips = strs[0].split("\\.");
        if (ips.length != 4) {return null;}
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%02x",Integer.parseInt(ips[0])));
        builder.append(String.format("%02x",Integer.parseInt(ips[1])));
        builder.append(String.format("%02x",Integer.parseInt(ips[2])));
        builder.append(String.format("%02x",Integer.parseInt(ips[3])));
        builder.append(String.format("%04x",Integer.parseInt(strs[1])));
        builder.append(String.format("%04x",Integer.parseInt(strs[2])));
        builder.append(String.format("%016x",(Long.parseLong(strs[3]) - TIME_2018_01_01)));
        return builder.toString();
    }

    /**
     * 进程16进制表示 4位
     */
    public static String getProcessIDLowerHexString() {
        if (process_hex_id != null) {
            return process_hex_id;
        }
        synchronized (ESBUUID.class) {//dubbo check 在jdk5以下不安全
            if (process_hex_id == null) {
                process_hex_id = String.format("%04x",ESBT.longInteger(getProcessID()));
            }
            return process_hex_id;
        }
    }

    /**
     * 当前线程id,大于65535则取模
     */
    public static String getThreadIDLowerHexString() {
        return String.format("%04x",(Thread.currentThread().getId()%65536l));
    }

    /**
     * 当前时间hexString 16位
     */
    public static String getTimestampLowerHexString() {
        return String.format("%016x",(System.currentTimeMillis()-TIME_2018_01_01));
    }

    private static final long TIME_2018_01_01 = ESBConsts.TIME_2018_01_01;

    /**
     * Converts long trace or span id to String.
     *
     * @param id trace, span or parent span id.
     * @return String representation.
     */
    public static String convertToString(final long id) {
        return Long.toHexString(id);
    }

    /**
     * Parses a 1 to 32 character lower-hex string with no prefix into an unsigned long, tossing any
     * bits higher than 64.
     */
    public static long convertToLong(String lowerHex) throws ESBException {
        int length = lowerHex.length();
        if (length < 1 || length > 32) {throw ESBExceptionCodes.INTERNAL_SERVER_ERROR("should be a 1 to 32 character lower-hex string with no prefix");}

        // trim off any high bits
        int beginIndex = length > 16 ? length - 16 : 0;

        return convertToLong(lowerHex, beginIndex);
    }

    /**
     * Parses a 16 character lower-hex string with no prefix into an unsigned long, starting at the
     * specified index.
     */
    public static long convertToLong(String lowerHex, int index) throws ESBException {
        long result = 0;
        for (int endIndex = Math.min(index + 16, lowerHex.length()); index < endIndex; index++) {
            char c = lowerHex.charAt(index);
            result <<= 4;
            if (c >= '0' && c <= '9') {
                result |= c - '0';
            } else if (c >= 'a' && c <= 'f') {
                result |= c - 'a' + 10;
            } else {
                throw ESBExceptionCodes.INTERNAL_SERVER_ERROR("should be a 1 to 32 character lower-hex string with no prefix");
            }
        }
        return result;
    }

    /**
     * local_id_hash|processId|threadId|timestamp
     * 唯一性
     * @return
     */
    public static String generateSeqID() {
        return getLocalIPLowerHexString()+"|"+getProcessID()+"|"+Thread.currentThread().getId()+"|"+System.currentTimeMillis();
    }

    /**
     * local_id|processId|threadId|timestamp
     * 唯一性保障
     * @return
     */
    public static String generateSeqIDV2() {
        return getLocalIP()+"|"+getProcessID()+"|"+Thread.currentThread().getId()+"|"+System.currentTimeMillis();
    }

    /**
     * 获取当前机器workId
     * @return
     */
    public static String getWorkId() {
        if (work_id != null) {
            return work_id;
        }
        synchronized (ESBUUID.class) {//dubbo check 在jdk5以下不安全
            if (work_id == null) {
                work_id = getLocalIP() + "|" + getProcessID();//忽略线程问题
            }
            return work_id;
        }
    }
    private static String work_id = null;
    private static String process_hex_id = null;

    public ESBUUID(long workerId, long regionId) {

        // 如果超出范围就抛出异常
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("worker Id can't be greater than %d or less than 0");
        }
        if (regionId > maxRegionId || regionId < 0) {
            throw new IllegalArgumentException("datacenter Id can't be greater than %d or less than 0");
        }

        this.workerId = workerId;
        this.regionId = regionId;
    }

    public ESBUUID(long workerId) {
        // 如果超出范围就抛出异常
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("worker Id can't be greater than %d or less than 0");
        }
        this.workerId = workerId;
        this.regionId = 0;
    }

    public long generate() {
        return this.nextId(false, 0);
    }

    private static class SingletonHolder {
        static String IP = null;
        static String IP_HEX = null;
        static String PRG_ID = null;
        static {
            try {
                InetAddress ia = InetAddress.getLocalHost();
                IP = ia.getHostAddress();
            } catch (Throwable e) {
                IP = "127.0.0." + (int)((Math.random()*17));
            }
            String strs[] = IP.split("\\.");
            IP_HEX = String.format("%02x",Integer.parseInt(strs[0]))
                    + String.format("%02x",Integer.parseInt(strs[1]))
                    + String.format("%02x",Integer.parseInt(strs[2]))
                    + String.format("%02x",Integer.parseInt(strs[3]));
            String name = ManagementFactory.getRuntimeMXBean().getName();
            PRG_ID = name.split("@")[0];
        }
        private static final ESBUUID INSTANCE = new ESBUUID(Math.abs(IP.hashCode())%(maxWorkerId - 2)+1);
        private static final String LOCAL_IP = IP;
        private static final String LOCAL_IP_HEX = IP_HEX;
        private static final String PROCESS_ID = PRG_ID;
    }

    /**
     * 获取当前ip
     * @return
     */
    public static String getLocalIP() {
        return SingletonHolder.LOCAL_IP;
    }

    /**
     * 获取当前ip的十六进制 8位
     * @return
     */
    public static String getLocalIPLowerHexString() {
        return SingletonHolder.LOCAL_IP_HEX;
    }

    /**
     * 获取当前进程id
     * @return
     */
    public static String getProcessID() {
        return SingletonHolder.PROCESS_ID;
    }


    /**
     * 实际产生代码的
     *
     * @param isPadding
     * @param busId
     * @return
     */
    private synchronized long nextId(boolean isPadding, long busId) {

        long timestamp = timeGen();
        long paddingnum = regionId;

        if (isPadding) {
            paddingnum = busId;
        }

        if (timestamp < lastTimestamp) {
            try {
                throw new Exception("Clock moved backwards.  Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //如果上次生成时间和当前时间相同,在同一毫秒内
        if (lastTimestamp == timestamp) {
            //sequence自增，因为sequence只有10bit，所以和sequenceMask相与一下，去掉高位
            sequence = (sequence + 1) & sequenceMask;
            //判断是否溢出,也就是每毫秒内超过1024，当为1024时，与sequenceMask相与，sequence就等于0
            if (sequence == 0) {
                //自旋等待到下一毫秒
                timestamp = tailNextMillis(lastTimestamp);
            }
        } else {
            // 如果和上次生成时间不同,重置sequence，就是下一毫秒开始，sequence计数重新从0开始累加,
            // 为了保证尾数随机性更大一些,最后一位设置一个随机数
            sequence = new SecureRandom().nextInt(10);
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (paddingnum << regionIdShift) | (workerId << workerIdShift) | sequence;
    }

    // 防止产生的时间比之前的时间还要小（由于NTP回拨等问题）,保持增量的趋势.
    private long tailNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    // 获取当前的时间戳
    protected long timeGen() {
        return System.currentTimeMillis();
    }


    //产生一个唯一的
    private static final AtomicInteger SEQUENCER = new AtomicInteger(1);
    private static final int SEQ_SIZE = 1000;
    //分布式系统不完全唯一，由于随机数引入，基本可以忽略其冲突（用户行为观察不受影响）
    public static long genDID() {
        int random = (int)(Math.random()*SEQ_SIZE);//取小数位，具有不可预测性
        int num = SEQUENCER.getAndIncrement();
        num = num > 0 ? num % SEQ_SIZE : -num % SEQ_SIZE;
        return (System.currentTimeMillis() - TIME_2018_01_01) * SEQ_SIZE * SEQ_SIZE + random * SEQ_SIZE + num;
    }
}
