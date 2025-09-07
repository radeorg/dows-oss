package org.dows.oss.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.dows.rade.crud.AutoFillDataListener;
import org.dows.rade.crud.BaseEntity;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "文件失败表")
@Table(value = "oss_fail", onUpdate = AutoFillDataListener.class, onInsert = AutoFillDataListener.class)
public class OssFailEntity extends BaseEntity<OssFailEntity> {

    @Schema(description = "文件上传失败标识ID")
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long ossFailId;

    @Schema(description = "md5")
    @Column(value = "md5")
    private String md5;

    @Schema(description = "文件名")
    @Column(value = "file_name")
    private String fileName;

    @Schema(description = "文件路径")
    @Column(value = "file_path")
    private String filePath;

    @Schema(description = "文件全量路径")
    @Column(value = "file_link")
    private String fileLink;

    @Schema(description = "文件后缀")
    @Column(value = "file_ext")
    private String fileExt;

    @Schema(description = "触发器（UPLOAD上传、OTT解析）")
    @Column(value = "trigger")
    private String trigger;

    @Schema(description = "存储通道[cos,oss,qiniu]")
    @Column(value = "channel")
    private String channel;

    @Schema(description = "来源（uim/hrm...）")
    @Column(value = "source")
    private String source;

    @Schema(description = "失败原因")
    @Column(value = "failed_reason")
    private String failedReason;

    @Schema(description = "操作人ID")
    @Column(value = "operator_id")
    private Long operatorId;

    @Schema(description = "应用ID")
    @Column(value = "app_id")
    private String appId;

    @Schema(description = "版本号，默认0")
    @Column(value = "ver", onUpdateValue = "ver+1")
    private Integer ver;

    @Schema(description = "是否删除，默认0")
    @Column(value = "deleted", isLogicDelete = true)
    private Integer deleted;

    @Schema(description = "创建时间")
    @Column(value = "ts")
    private Date ts;

    @Schema(description = "最后更新时间")
    @Column(value = "ut")
    private Date ut;
}
