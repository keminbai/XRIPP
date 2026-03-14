package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.NotificationSendLog;
import com.xripp.backend.mapper.NotificationSendLogMapper;
import com.xripp.backend.service.INotificationSendLogService;
import org.springframework.stereotype.Service;

@Service
public class NotificationSendLogServiceImpl extends ServiceImpl<NotificationSendLogMapper, NotificationSendLog>
        implements INotificationSendLogService {}
