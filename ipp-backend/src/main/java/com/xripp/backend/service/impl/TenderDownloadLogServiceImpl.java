package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.TenderDownloadLog;
import com.xripp.backend.mapper.TenderDownloadLogMapper;
import com.xripp.backend.service.ITenderDownloadLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class TenderDownloadLogServiceImpl
        extends ServiceImpl<TenderDownloadLogMapper, TenderDownloadLog>
        implements ITenderDownloadLogService {

    /**
     * 尝试写入下载日志，利用唯一约束 (user_id, tender_id) 实现幂等性。
     *
     * <p>成功插入 → 首次下载，返回 true，调用方需扣减下载次数。
     * <p>DuplicateKeyException → 重复下载，返回 false，跳过扣减。
     */
    @Override
    public boolean tryLog(Long userId, Long tenderId) {
        TenderDownloadLog entry = new TenderDownloadLog();
        entry.setUserId(userId);
        entry.setTenderId(tenderId);
        entry.setDownloadedAt(new Date());
        try {
            save(entry);
            return true; // 首次下载
        } catch (DuplicateKeyException e) {
            return false; // 已下载过，跳过扣减
        } catch (Exception e) {
            // 日志写入异常不阻断下载主流程，但告警
            log.error("[TenderDownload] 日志写入失败 userId={} tenderId={} err={}",
                    userId, tenderId, e.getMessage());
            return false; // 保守策略：异常时视为已下载，不扣减
        }
    }
}
