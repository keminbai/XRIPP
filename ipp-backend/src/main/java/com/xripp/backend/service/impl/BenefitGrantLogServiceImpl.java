package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.BenefitGrantLog;
import com.xripp.backend.mapper.BenefitGrantLogMapper;
import com.xripp.backend.service.IBenefitGrantLogService;
import org.springframework.stereotype.Service;

@Service
public class BenefitGrantLogServiceImpl extends ServiceImpl<BenefitGrantLogMapper, BenefitGrantLog> implements IBenefitGrantLogService {
}
