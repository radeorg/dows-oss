package org.dows.oss.entity;

import lombok.*;
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
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "文件详情表")
@Table(value = "oss_detail", onUpdate = AutoFillDataListener.class, onInsert = AutoFillDataListener.class)
public class OssDetailEntity extends BaseEntity<OssDetailEntity> {

    /**
     * 详情ID
     */
    @Schema(description = "详情ID")
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long ossDetailId;

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    @Column(value = "oss_file_id")
    private Long ossFileId;

    /**
     * 存储通道[cos,oss,qiniu]
     */
    @Schema(description = "存储通道[cos,oss,qiniu]")
    @Column(value = "channel")
    private String channel;

    /**
     * 触发器（UPLOAD上传、OTT解析）
     */
    @Schema(description = "触发器（UPLOAD上传、OTT解析）")
    @Column(value = "trigger")
    private String trigger;

    /**
     * md5
     */
    @Schema(description = "md5")
    @Column(value = "md5")
    private String md5;

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    @Column(value = "file_name")
    private String fileName;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径（业务系统指定）")
    @Column(value = "base_path")
    private String basePath;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    @Column(value = "file_path")
    private String filePath;

    /**
     * 文件全量路径
     */
    @Schema(description = "文件全量路径")
    @Column(value = "file_link")
    private String fileLink;

    /**
     * 文件后缀
     */
    @Schema(description = "文件后缀")
    @Column(value = "file_ext")
    private String fileExt;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小")
    @Column(value = "file_size")
    private Long fileSize;

    /**
     * 状态，默认0，0代表待处理，1代表已完成，2代表处理失败
     */
    @Schema(description = "状态，默认0，0代表待处理，1代表已完成，2代表处理失败")
    @Column(value = "state")
    private Integer state;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    @Column(value = "failed_reason")
    private String failedReason;

    /**
     * 顺序
     */
    @Schema(description = "顺序")
    @Column(value = "seq")
    private Integer seq;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数")
    @Column(value = "retry_count")
    private Integer retryCount;
    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID")
    @Column(value = "operator_id")
    private Long operatorId;

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
    @Column(value = "ver", onUpdateValue = "ver+1")
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
