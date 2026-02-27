package com.xripp.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xripp.backend.entity.TenderDownloadLog;

public interface ITenderDownloadLogService extends IService<TenderDownloadLog> {

    /**
     * 尝试记录标书下载日志。
     *
     * @return {@code true} 首次下载（需扣减次数）；{@code false} 重复下载（跳过扣减）
     */
    boolean tryLog(Long userId, Long tenderId);
}
