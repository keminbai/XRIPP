package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.Partner;
import com.xripp.backend.mapper.PartnerMapper;
import com.xripp.backend.service.IPartnerService;
import org.springframework.stereotype.Service;

@Service
public class PartnerServiceImpl extends ServiceImpl<PartnerMapper, Partner> implements IPartnerService {
}
