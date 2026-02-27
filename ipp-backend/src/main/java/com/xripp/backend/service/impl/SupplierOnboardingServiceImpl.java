package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.SupplierOnboarding;
import com.xripp.backend.mapper.SupplierOnboardingMapper;
import com.xripp.backend.service.ISupplierOnboardingService;
import org.springframework.stereotype.Service;

@Service
public class SupplierOnboardingServiceImpl
        extends ServiceImpl<SupplierOnboardingMapper, SupplierOnboarding>
        implements ISupplierOnboardingService {}
