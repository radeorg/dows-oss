package org.dows.oss.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.dows.oss.listener.AutoFillDataListener;
import org.dows.rade.crud.BaseEntity;

import java.util.Date;

/**
 * OSS文件上传过程 实体类。
 *
 * @author tangsm
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OSS文件上传过程表")
@Table(value = "oss_uploader_process", onUpdate = AutoFillDataListener.class, onInsert = AutoFillDataListener.class)
public class OssUploaderProcessEntity extends BaseEntity<OssUploaderProcessEntity> {

    @Schema(description = "OSS上传过程标识ID")
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long ossUploaderProcessId;

    @Schema(description = "OSS上传ID")
    @Column(value = "oss_uploader_id")
    private Long ossUploaderId;

    @Schema(description = "账号实例ID")
    @Column(value = "account_instance_id")
    private Long accountInstanceId;

    @Schema(description = "状态码（0待处理，1已完成，2处理失败）")
    @Column(value = "state")
    private Integer state;

    @Schema(description = "失败原因")
    @Column(value = "failed_reason")
    private String failedReason;

    @Schema(description = "触发器（UPLOAD上传、[OTT解析]）")
    @Column(value = "trigger")
    private String trigger;

    @Schema(description = "应用ID")
    @Column(value = "app_id", tenantId = true)
    private String appId;

    @Schema(description = "操作者ID")
    @Column(value = "operator_id")
    private Long operatorId;

    @Schema(description = "乐观锁，默认为0")
    @Column(value = "ver", onUpdateValue = "ver+1")
    private Integer ver;

    @Schema(description = "逻辑删除，0未删除，1删除")
    @Column(value = "deleted", isLogicDelete = true)
    private Integer deleted;

    @Schema(description = "创建时间")
    @Column(value = "ts")
    private Date ts;

    @Schema(description = "最后更新时间")
    @Column(value = "ut")
    private Date ut;
}
