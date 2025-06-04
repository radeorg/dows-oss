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
 * OSS文件上传 实体类。
 *
 * @author tangsm
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OSS文件上传表")
@Table(value = "oss_uploader", onUpdate = AutoFillDataListener.class, onInsert = AutoFillDataListener.class)
public class OssUploaderEntity extends BaseEntity<OssUploaderEntity> {

    @Schema(description = "OSS上传标识ID")
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long ossUploaderId;

    @Schema(description = "账号实例ID")
    @Column(value = "account_instance_id")
    private Long accountInstanceId;

    @Schema(description = "文件存储路径（业务系统告知）")
    @Column(value = "file_base_path")
    private String fileBasePath;

    @Schema(description = "文件上传到本地的临时路径，删除本地文件时需要")
    @Column(value = "file_temp_path")
    private String fileTempPath;

    @Schema(description = "上传时的文件名")
    @Column(value = "file_name")
    private String fileName;

    @Schema(description = "文件内容md5")
    @Column(value = "file_md5")
    private String fileMd5;

    @Schema(description = "文件大小")
    @Column(value = "file_size")
    private Long fileSize;

    @Schema(description = "文件后缀")
    @Column(value = "file_ext")
    private String fileExt;

    @Schema(description = "文件全量路径")
    @Column(value = "file_link")
    private String fileLink;

    @Schema(description = "文本全量路径")
    @Column(value = "txt_link")
    private String txtLink;

    @Schema(description = "文本存储路径（业务系统告知）")
    @Column(value = "txt_base_path")
    private String txtBasePath;

    @Schema(description = "过期时间，秒，传0代表无过期时间限制（过期自动删除本地文件）")
    @Column(value = "expire_time")
    private Long expireTime;

    @Schema(description = "到期时间=创建时间+过期时间")
    @Column(value = "expire_date")
    private Date expireDate;

    /**
     * 默认9个0，每位上的数字0代表未执行，1代表已执行；目前第一位代表上传，第二位代表解析，例：
     * 000000000，该文件还未上传至COS，也未进行解析
     * 100000000，该文件已上传至COS，但未进行解析
     * 110000000，该文件已上传至COS，已进行解析
     * 010000000，该文件还未上传至COS，但已 进行解析
     */
    @Schema(description = "状态码")
    @Column(value = "state_code")
    private String stateCode;

    @Schema(description = "触发器（[解析、压缩]）")
    @Column(value = "trigger")
    private String trigger;

    @Schema(description = "回调业务系统路径")
    @Column(value = "callback_url")
    private String callbackUrl;

    @Schema(description = "业务系统回调状态（0待回调，1已回调，2回调失败）")
    @Column(value = "callback_state")
    private Integer callbackState;

    @Schema(description = "业务系统唯一标识ID")
    @Column(value = "biz_id")
    private Long bizId;

    @Schema(description = "来源（uim/hrm...)")
    @Column(value = "source")
    private String source;

    @Schema(description = "应用ID")
    @Column(value = "app_id", tenantId = true)
    private String appId;

    @Schema(description = "操作者ID")
    @Column(value = "operator_id")
    private Long operatorId;

    @Schema(description = "所属人ID")
    @Column(value = "owner_id")
    private Long ownerId;

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
