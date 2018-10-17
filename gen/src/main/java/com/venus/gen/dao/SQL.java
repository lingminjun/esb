package com.venus.gen.dao;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * User: 凌敏均
 * Date: 17/12/06
 *
 * Example:

public interface AccountExtDAO extends AccountExtIndexQueryDAO {
    @SQL("select * from `s_account_ext` where `account_id` = #{accountId} and  and `is_delete` = 0 and `data_type` in \n" +
            "        <foreach collection=\"list\" item=\"theFieldName\" index=\"index\"  \n" +
            "           open=\"(\" close=\")\" separator=\",\">  \n" +
            "           #{theFieldName}  \n" +
            "        </foreach> \n")
    List<AccountExtDO> queryAttributesForKeys(@Param("accountId") long accountId, @Param("list") List<String> list);

    @SQL("select * from `s_account_ext` where `account_id` = #{accountId} and `data_type` = #{fieldName} \n")
    AccountExtDO getAttributeForKey(@Param("accountId") long accountId, @Param("fieldName") String fieldName);
}

 *
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SQL {
    String value();//对应的sql
}
