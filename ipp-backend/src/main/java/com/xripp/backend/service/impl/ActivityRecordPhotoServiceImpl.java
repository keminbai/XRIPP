package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.ActivityRecordPhoto;
import com.xripp.backend.mapper.ActivityRecordPhotoMapper;
import com.xripp.backend.service.IActivityRecordPhotoService;
import org.springframework.stereotype.Service;

@Service
public class ActivityRecordPhotoServiceImpl extends ServiceImpl<ActivityRecordPhotoMapper, ActivityRecordPhoto> implements IActivityRecordPhotoService {}
