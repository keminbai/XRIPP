package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.SupplierOnboardingFile;
import com.xripp.backend.mapper.SupplierOnboardingFileMapper;
import com.xripp.backend.service.ISupplierOnboardingFileService;
import org.springframework.stereotype.Service;

@Service
public class SupplierOnboardingFileServiceImpl extends ServiceImpl<SupplierOnboardingFileMapper, SupplierOnboardingFile> implements ISupplierOnboardingFileService {}
