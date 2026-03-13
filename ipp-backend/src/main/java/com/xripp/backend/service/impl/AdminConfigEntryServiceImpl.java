package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.AdminConfigEntry;
import com.xripp.backend.mapper.AdminConfigEntryMapper;
import com.xripp.backend.service.IAdminConfigEntryService;
import org.springframework.stereotype.Service;

@Service
public class AdminConfigEntryServiceImpl
        extends ServiceImpl<AdminConfigEntryMapper, AdminConfigEntry>
        implements IAdminConfigEntryService {}
