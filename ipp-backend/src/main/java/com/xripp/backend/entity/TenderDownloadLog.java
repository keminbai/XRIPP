package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 标书下载日志（防重复扣减核心表）
 *
 * <p>业务规则：VIP/SVIP 会员重复下载同一标书不扣减次数。
 * <p>实现方式：唯一约束 (user_id, tender_id)，DuplicateKeyException = 已下载过。
 * <p>⚠️ 此表只增不减，禁止 DELETE。
 *
 * @see docs/DDL_Phase2_Migration.sql § 9) tender_download_logs
 */
@Data
@TableName("tender_download_logs")
public class TenderDownloadLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("tender_id")
    private Long tenderId;

    @TableField("downloaded_at")
    private Date downloadedAt;
}
