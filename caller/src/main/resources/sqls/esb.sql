# api配置
# jdbc:mysql://localhost:3306/springbootdb?useUnicode=true&characterEncoding=utf8mb4&autoReconnect=true
CREATE DATABASE esbdb;

# 接口申明定义
CREATE TABLE IF NOT EXISTS `esb_api` (
  `id`  bigint NOT NULL AUTO_INCREMENT ,
  `domain`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '服务名',
  `module`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '模块名' ,
  `method`  varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '方法名',

  `owner`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `version`  varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '版本',
  `detail`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',

  `security`  int DEFAULT '0' COMMENT '接口权限 取值@See ESBSecurityLevel' ,

  `status`  tinyint DEFAULT '0' COMMENT '状态:0普通接口;1开放平台接口;-1禁用;' ,

  `create_at`  bigint DEFAULT '0' COMMENT '创建时间' ,
  `modified_at`  bigint DEFAULT '0' COMMENT '修改时间' ,

  PRIMARY KEY (`id`),
  UNIQUE INDEX `UNI_IDX_ID` (`domain`,`module`,`method`) USING BTREE
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  AUTO_INCREMENT=1;

# 接口定义描述，简单处理，全部放到一个json中
CREATE TABLE IF NOT EXISTS `esb_api_detail` (
  `id`  bigint NOT NULL AUTO_INCREMENT ,
  `api_id`  bigint NOT NULL COMMENT 'api id' ,
  `json`  text CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置结构',
  `create_at`  bigint DEFAULT '0' COMMENT '创建时间' ,
  `modified_at`  bigint DEFAULT '0' COMMENT '修改时间' ,
  `is_delete`  tinyint DEFAULT '0' COMMENT '0: enabled, 1: deleted' ,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `IDX_QUERY` (`api_id`) USING BTREE
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  AUTO_INCREMENT=1
;

# 账号表; 用户能绑定多个账号,所有的账号,都具备登录的能力
CREATE TABLE IF NOT EXISTS `esb_third_party_secret` (
  `id`  bigint NOT NULL AUTO_INCREMENT ,
  `platform`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第三方平台',
  `info`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '其他说明信息',

  `issu_pri_key`  varchar(4096) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '颁发的私钥',
  `issu_pub_key`  varchar(4096) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '颁发的公钥',
  `issu_algo`  varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '颁发秘钥算法' ,

  `create_at`  bigint DEFAULT '0' COMMENT '创建时间' ,
  `modified_at`  bigint DEFAULT '0' COMMENT '修改时间' ,
  `is_delete`  tinyint DEFAULT '0' COMMENT '0: enabled, 1: deleted' ,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UNI_IDX_ID` (`platform`) USING BTREE
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  AUTO_INCREMENT=1;



















