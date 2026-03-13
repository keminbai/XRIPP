package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.SupplierOnboardingCertificate;
import com.xripp.backend.mapper.SupplierOnboardingCertificateMapper;
import com.xripp.backend.service.ISupplierOnboardingCertificateService;
import org.springframework.stereotype.Service;

@Service
public class SupplierOnboardingCertificateServiceImpl extends ServiceImpl<SupplierOnboardingCertificateMapper, SupplierOnboardingCertificate> implements ISupplierOnboardingCertificateService {}
