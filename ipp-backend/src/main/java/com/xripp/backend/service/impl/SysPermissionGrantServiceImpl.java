package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.SysPermissionGrant;
import com.xripp.backend.mapper.SysPermissionGrantMapper;
import com.xripp.backend.service.ISysPermissionGrantService;
import org.springframework.stereotype.Service;

@Service
public class SysPermissionGrantServiceImpl
        extends ServiceImpl<SysPermissionGrantMapper, SysPermissionGrant>
        implements ISysPermissionGrantService {
}
