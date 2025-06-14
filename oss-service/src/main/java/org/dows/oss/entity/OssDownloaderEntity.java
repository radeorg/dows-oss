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
@Table(value = "oss_downloader")
public class OssDownloaderEntity extends BaseEntity<OssDownloaderEntity> {

    /**
     * 下载ID
     */
    @Schema(description = "下载ID")
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long ossDownloaderId;

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    @Column(value = "oss_file_id")
    private Long ossFileId;

    /**
     * 详情ID
     */
    @Schema(description = "详情ID")
    @Column(value = "oss_detail_id")
    private Long ossDetailId;

    /**
     * 下载进度
     */
    @Schema(description = "下载进度")
    @Column(value = "process")
    private Long process;

    /**
     * 状态
     */
    @Schema(description = "状态")
    @Column(value = "state")
    private Integer state;

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
