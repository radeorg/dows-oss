package org.dows.oss.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.mybatisflex.annotation.Table;

import java.lang.Long;
import java.util.Date;
import java.lang.String;
import java.lang.Integer;

import org.dows.oss.AutoFillDataListener;
import org.dows.rade.crud.BaseEntity;

/**
 * 实体类。
 *
 * @author lait.zhang@gmail.com
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "文件标识表")
@Table(value = "oss_identifier", onUpdate = AutoFillDataListener.class, onInsert = AutoFillDataListener.class)
public class OssIdentifierEntity extends BaseEntity<OssIdentifierEntity> {

    /**
     * 账号标识ID
     */
    @Schema(description = "账号标识ID")
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long ossIdentifierId;

    /**
     * 来源
     */
    @Schema(description = "来源（uim/hrm...)")
    @Column(value = "source")
    private String source;

    /**
     * 密钥ID
     */
    @Schema(description = "密钥ID")
    @Column(value = "secret_id")
    private String secretId;

    /**
     * 密钥KEY
     */
    @Schema(description = "密钥KEY")
    @Column(value = "secret_key")
    private String secretKey;

    /**
     * 存储通道[cos,oss,qiniu]
     */
    @Schema(description = "存储通道[cos,oss,qiniu]")
    @Column(value = "channel")
    private String channel;

    /**
     * 第三方存储JSON配置
     */
    @Schema(description = "第三方存储JSON配置")
    @Column(value = "channel_config")
    private String channelConfig;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    @Column(value = "app_id")
    private String appId;

    /**
     * 版本号，默认0
     */
    @Schema(description = "版本号，默认0")
    @Column(value = "ver")
    private Integer ver;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    @Column(value = "disable")
    private Integer disable;

    /**
     * 是否删除，默认0
     */
    @Schema(description = "是否删除，默认0")
    @Column(value = "deleted", isLogicDelete = true)
    private Integer deleted;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @Column(value = "ts")
    private Date ts;

    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间")
    @Column(value = "ut")
    private Date ut;


}
