package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.AuditLog;
import com.xripp.backend.mapper.AuditLogMapper;
import com.xripp.backend.service.IAuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements IAuditLogService {
}
