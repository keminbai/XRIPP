package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.TenderEntity;
import com.xripp.backend.mapper.TenderMapper;
import com.xripp.backend.service.ITenderService;
import org.springframework.stereotype.Service;

@Service
public class TenderServiceImpl extends ServiceImpl<TenderMapper, TenderEntity> implements ITenderService {}
