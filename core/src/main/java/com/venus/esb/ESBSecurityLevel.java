package com.venus.esb;

import com.venus.esb.annotation.ESBDesc;

/**
 * 大小请限制在 int 最大值以内 0x8000,0000
 */
@ESBDesc("认证权限定义")
public enum ESBSecurityLevel {

    @ESBDesc("无认证")
    none(0x00000000),

    @ESBDesc("设备认证")
    deviceAuth(0x00000001),

    @ESBDesc("账号认证")
    accountAuth(0x00000010),

    @ESBDesc("用户认证")
    userAuth(0x00000100),

    @ESBDesc("保密认证")
    secretAuth(0x00001000),

    @ESBDesc("复杂认证，三方自定义验证方式pid+秘钥")
    integrated(0x00010000),

    @ESBDesc("一次性认证使用")
    once(0x00100000),

    @ESBDesc("延长认证，仅仅用于refresh，请使用前面token的定义")
    extend(0x01000000),

    @ESBDesc("其他用途认证")
    other(0x10000000);

    private int code;


    public static ESBSecurityLevel valueOf(int code) {

        if (none.code == code) {
            return none;
        } else if (deviceAuth.code == code) {
            return deviceAuth;
        } else if (accountAuth.code == code) {
            return accountAuth;
        } else if (userAuth.code == code) {
            return userAuth;
        } else if (integrated.code == code) {
            return integrated;
        } else if (secretAuth.code == code) {
            return secretAuth;
        } else if (once.code == code) {
            return once;
        } else if (extend.code == code) {
            return extend;
        } else if (other.code == code) {
            return other;
        } else {
            return none;
        }

        //以下全部删除
//        else if (Test.code == code) {
//            return Test;
//        } else if (None.code == code) {
//            return None;
//        } else if (SeceretUserToken.code == code) {
//            return SeceretUserToken;
//        } else if (OAuthVerified.code == code) {
//            return OAuthVerified;
//        } else if (RegisteredDevice.code == code) {
//            return RegisteredDevice;
//        } else if (User.code == code) {
//            return User;
//        } else if (UserTrustedDevice.code == code) {
//            return UserTrustedDevice;
//        } else if (MobileOwner.code == code) {
//            return MobileOwner;
//        } else if (MobileOwnerTrustedDevice.code == code) {
//            return MobileOwnerTrustedDevice;
//        } else if (UserLogin.code == code) {
//            return UserLogin;
//        } else if (UserLoginAndMobileOwner.code == code) {
//            return UserLoginAndMobileOwner;
//        } else if (Integrated.code == code) {
//            return Integrated;
//        } else if (Internal.code == code) {
//            return Internal;
//        } else if (Document.code == code) {
//            return Document;
//        } else {
//            return None;
//        }
    }

    /**
     * @param code security16进制编码
     */
    private ESBSecurityLevel(int code) {
        this.code = code;
    }


    public int getCode() {
        return code;
    }

    /**
     * 检查auth权限是否包含当前权限
     */
    public boolean check(int auth) {
        return (auth & code) == code;
    }

    /**
     * 检查当前权限是否包含auth权限
     */
    public boolean contains(int auth) {
        return (auth & code) != 0;
    }

    /**
     * 检查auth权限是否包含当前权限
     */
    public boolean check(ESBSecurityLevel auth) {
        return (auth.code & code) == code;
    }

    /**
     * 在auth权限的基础上增加当前权限
     */
    public int authorize(int auth) {
        return auth | this.code;
    }

    /**
     * 判断auth权限是否为空
     */
    public static boolean isNone(int auth) {
        return auth == 0;
    }

    /**
     * 判断auth是否会过期, 包含 OAuthVerified, User, UserTrustedDevice, MobileOwner, MobileOwnerTrustedDevice, UserLogin
     * 其中之一的auth都可能会过期
     */
    public static boolean expirable(int auth) {
        return (auth & ( accountAuth.code | userAuth.code | secretAuth.code | extend.code | once.code | other.code)) != 0;
    }

    /**
     * 判断auth是否需要验证token, 包含 OAuthVerified, RegisteredDevice, User, UserTrustedDevice, MobileOwner, MobileOwnerTrustedDevice, UserLogin
     */
    public static boolean requireToken(int auth){
        return (auth & ( deviceAuth.code | accountAuth.code | userAuth.code | secretAuth.code)) != 0;
    }
}
