package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.ActivityRecord;
import com.xripp.backend.mapper.ActivityRecordMapper;
import com.xripp.backend.service.IActivityRecordService;
import org.springframework.stereotype.Service;

@Service
public class ActivityRecordServiceImpl extends ServiceImpl<ActivityRecordMapper, ActivityRecord> implements IActivityRecordService {}
