package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.mapper.MemberProfileMapper;
import com.xripp.backend.service.IMemberProfileService;
import org.springframework.stereotype.Service;

@Service
public class MemberProfileServiceImpl extends ServiceImpl<MemberProfileMapper, MemberProfile> implements IMemberProfileService {
}
