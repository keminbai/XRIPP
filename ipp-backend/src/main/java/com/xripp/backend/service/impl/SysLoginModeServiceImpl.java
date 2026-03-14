package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.SysLoginMode;
import com.xripp.backend.mapper.SysLoginModeMapper;
import com.xripp.backend.service.ISysLoginModeService;
import org.springframework.stereotype.Service;

@Service
public class SysLoginModeServiceImpl
        extends ServiceImpl<SysLoginModeMapper, SysLoginMode>
        implements ISysLoginModeService {
}
