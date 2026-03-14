package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.CustomerServiceTicket;
import com.xripp.backend.mapper.CustomerServiceTicketMapper;
import com.xripp.backend.service.ICustomerServiceTicketService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceTicketServiceImpl extends ServiceImpl<CustomerServiceTicketMapper, CustomerServiceTicket>
        implements ICustomerServiceTicketService {}
