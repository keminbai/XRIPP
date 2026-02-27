package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.ActivityRegistration;
import com.xripp.backend.mapper.ActivityRegistrationMapper;
import com.xripp.backend.service.IActivityRegistrationService;
import org.springframework.stereotype.Service;

@Service
public class ActivityRegistrationServiceImpl
        extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration>
        implements IActivityRegistrationService {}
