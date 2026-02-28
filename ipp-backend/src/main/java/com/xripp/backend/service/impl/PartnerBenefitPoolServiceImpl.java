package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.PartnerBenefitPool;
import com.xripp.backend.mapper.PartnerBenefitPoolMapper;
import com.xripp.backend.service.IPartnerBenefitPoolService;
import org.springframework.stereotype.Service;

@Service
public class PartnerBenefitPoolServiceImpl extends ServiceImpl<PartnerBenefitPoolMapper, PartnerBenefitPool> implements IPartnerBenefitPoolService {
}
