package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.CustomerServiceMessage;
import com.xripp.backend.mapper.CustomerServiceMessageMapper;
import com.xripp.backend.service.ICustomerServiceMessageService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceMessageServiceImpl extends ServiceImpl<CustomerServiceMessageMapper, CustomerServiceMessage>
        implements ICustomerServiceMessageService {}
