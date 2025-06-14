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
@Table(value = "oss_file")
public class OssFileEntity extends BaseEntity<OssFileEntity> {

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long ossFileId;

    /**
     * 文件存储路径（业务系统告知）
     */
    @Schema(description = "文件存储路径（业务系统告知）")
    @Column(value = "base_path")
    private String basePath;

    /**
     * 文件后缀
     */
    @Schema(description = "文件后缀")
    @Column(value = "file_ext")
    private String fileExt;

    /**
     * 原始md5
     */
    @Schema(description = "原始md5")
    @Column(value = "md5")
    private String md5;

    /**
     * 原文件名
     */
    @Schema(description = "原文件名")
    @Column(value = "file_name")
    private String fileName;

    /**
     * 原始文件大小
     */
    @Schema(description = "原始文件大小")
    @Column(value = "file_size")
    private Integer fileSize;

    /**
     * 过期时间（过期自动删除本地文件）
     */
    @Schema(description = "过期时间（过期自动删除本地文件）")
    @Column(value = "expire_time")
    private Long expireTime;

    /**
     * 批次号[年月日0001]
     */
    @Schema(description = "批次号[年月日0001]")
    @Column(value = "batch_no")
    private String batchNo;

    /**
     * 来源（uim/hrm...）
     */
    @Schema(description = "来源（uim/hrm...）")
    @Column(value = "source")
    private String source;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    @Column(value = "app_id")
    private String appId;

    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID")
    @Column(value = "operator_id")
    private Long operatorId;

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
