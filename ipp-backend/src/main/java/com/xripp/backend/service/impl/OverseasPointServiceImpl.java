package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.OverseasPoint;
import com.xripp.backend.mapper.OverseasPointMapper;
import com.xripp.backend.service.IOverseasPointService;
import org.springframework.stereotype.Service;

@Service
public class OverseasPointServiceImpl extends ServiceImpl<OverseasPointMapper, OverseasPoint>
        implements IOverseasPointService {
}
