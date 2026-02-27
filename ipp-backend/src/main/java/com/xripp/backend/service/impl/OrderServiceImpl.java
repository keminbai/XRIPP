package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.mapper.OrderMapper;
import com.xripp.backend.service.IOrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements IOrderService {}
