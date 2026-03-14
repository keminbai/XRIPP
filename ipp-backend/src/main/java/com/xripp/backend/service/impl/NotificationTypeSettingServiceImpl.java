package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.NotificationTypeSetting;
import com.xripp.backend.mapper.NotificationTypeSettingMapper;
import com.xripp.backend.service.INotificationTypeSettingService;
import org.springframework.stereotype.Service;

@Service
public class NotificationTypeSettingServiceImpl extends ServiceImpl<NotificationTypeSettingMapper, NotificationTypeSetting>
        implements INotificationTypeSettingService {}
