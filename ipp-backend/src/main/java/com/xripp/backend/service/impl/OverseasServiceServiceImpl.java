package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.OverseasService;
import com.xripp.backend.mapper.OverseasServiceMapper;
import com.xripp.backend.service.IOverseasServiceService;
import org.springframework.stereotype.Service;

@Service
public class OverseasServiceServiceImpl extends ServiceImpl<OverseasServiceMapper, OverseasService>
        implements IOverseasServiceService {
}
