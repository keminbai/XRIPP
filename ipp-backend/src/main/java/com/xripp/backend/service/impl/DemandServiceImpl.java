package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.Demand;
import com.xripp.backend.mapper.DemandMapper;
import com.xripp.backend.service.IDemandService;
import org.springframework.stereotype.Service;

@Service
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand> implements IDemandService {}
