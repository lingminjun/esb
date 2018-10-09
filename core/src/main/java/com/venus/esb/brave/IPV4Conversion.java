package com.venus.esb.brave;

/**
 * Created by lmj on 17/9/1.
 */
public class IPV4Conversion {

    public  static  int convertToInt(String ipAddr){
        String[] p4 = ipAddr.split("\\.");
        int ipInt = 0;
        int part = Integer.valueOf(p4[0]);
        ipInt = ipInt | (part << 24);
        part = Integer.valueOf(p4[1]);
        ipInt = ipInt | (part << 16);
        part = Integer.valueOf(p4[2]);
        ipInt = ipInt | (part << 8);
        part = Integer.valueOf(p4[3]);
        ipInt = ipInt | (part);
        return ipInt;
    }
}
