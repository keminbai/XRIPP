package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.PartnerProfitSettlement;
import com.xripp.backend.mapper.PartnerProfitSettlementMapper;
import com.xripp.backend.service.IPartnerProfitSettlementService;
import org.springframework.stereotype.Service;

@Service
public class PartnerProfitSettlementServiceImpl
        extends ServiceImpl<PartnerProfitSettlementMapper, PartnerProfitSettlement>
        implements IPartnerProfitSettlementService {}
