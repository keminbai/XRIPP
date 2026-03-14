package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.CustomerServiceTicketFile;
import com.xripp.backend.mapper.CustomerServiceTicketFileMapper;
import com.xripp.backend.service.ICustomerServiceTicketFileService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceTicketFileServiceImpl extends ServiceImpl<CustomerServiceTicketFileMapper, CustomerServiceTicketFile>
        implements ICustomerServiceTicketFileService {}
