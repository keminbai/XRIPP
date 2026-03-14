package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.OverseasServiceFile;
import com.xripp.backend.mapper.OverseasServiceFileMapper;
import com.xripp.backend.service.IOverseasServiceFileService;
import org.springframework.stereotype.Service;

@Service
public class OverseasServiceFileServiceImpl extends ServiceImpl<OverseasServiceFileMapper, OverseasServiceFile>
        implements IOverseasServiceFileService {
}
