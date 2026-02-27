package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.MemberVerification;
import com.xripp.backend.mapper.MemberVerificationMapper;
import com.xripp.backend.service.IMemberVerificationService;
import org.springframework.stereotype.Service;

@Service
public class MemberVerificationServiceImpl
        extends ServiceImpl<MemberVerificationMapper, MemberVerification>
        implements IMemberVerificationService {}
