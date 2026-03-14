package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.SysPermissionProfile;
import com.xripp.backend.mapper.SysPermissionProfileMapper;
import com.xripp.backend.service.ISysPermissionProfileService;
import org.springframework.stereotype.Service;

@Service
public class SysPermissionProfileServiceImpl
        extends ServiceImpl<SysPermissionProfileMapper, SysPermissionProfile>
        implements ISysPermissionProfileService {
}
