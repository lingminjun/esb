package com.venus.esb.utils;

import com.venus.esb.sign.utils.Base64Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by lingminjun on 17/9/2.
 */
public final class GZIP {
    public static String compressToBase64String(String str, String encoding) {
        byte[] bytes = compress(str,encoding);
        if (bytes != null) {
            return Base64Util.encodeToString(bytes);
        }
        return null;
    }

    public static String uncompressFromBase64String(String str) {
        byte[] bytes = uncompress(Base64Util.decode(str));
        if (bytes != null) {
            return new String(bytes);
        }
        return null;
    }

    public static byte[] compress(String str, String encoding) {
        if (str == null || str.length() == 0) {
            return null;
        }
        GZIPOutputStream gzip;
        byte[] bytes = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes(encoding));
            gzip.close();
            bytes = out.toByteArray();
            out.close();
        } catch (Throwable e) {}
        return bytes;
    }

    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        byte[] result = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            result = out.toByteArray();
            out.close();
            ungzip.close();
            in.close();
        } catch (Throwable e) {}

        return result;
    }
}
