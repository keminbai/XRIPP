package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.ActivityDisplayApplication;
import com.xripp.backend.mapper.ActivityDisplayApplicationMapper;
import com.xripp.backend.service.IActivityDisplayApplicationService;
import org.springframework.stereotype.Service;

@Service
public class ActivityDisplayApplicationServiceImpl
        extends ServiceImpl<ActivityDisplayApplicationMapper, ActivityDisplayApplication>
        implements IActivityDisplayApplicationService {}
