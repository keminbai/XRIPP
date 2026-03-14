package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.OverseasReport;
import com.xripp.backend.mapper.OverseasReportMapper;
import com.xripp.backend.service.IOverseasReportService;
import org.springframework.stereotype.Service;

@Service
public class OverseasReportServiceImpl extends ServiceImpl<OverseasReportMapper, OverseasReport>
        implements IOverseasReportService {
}
