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
@Schema(name = "$table.comment")
@Table(value = "oss_trigger")
public class OssTriggerEntity extends BaseEntity<OssTriggerEntity> {

    /**
     * 触发ID
     */
    @Schema(description = "触发ID")
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long ossTriggerId;

    /**
     * 账号标识ID
     */
    @Schema(description = "账号标识ID")
    @Column(value = "oss_identifier_id")
    private Long ossIdentifierId;

    /**
     * 触发器（[解析、编码，OCR]）
     */
    @Schema(description = "触发器（[解析、编码，OCR]）")
    @Column(value = "trigger")
    private String trigger;

    /**
     * 回调业务系统路径
     */
    @Schema(description = "回调业务系统路径")
    @Column(value = "callback_url")
    private String callbackUrl;

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
