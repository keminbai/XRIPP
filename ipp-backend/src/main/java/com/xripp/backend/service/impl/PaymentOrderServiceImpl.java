package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.PaymentOrder;
import com.xripp.backend.mapper.PaymentOrderMapper;
import com.xripp.backend.service.IPaymentOrderService;
import org.springframework.stereotype.Service;

@Service
public class PaymentOrderServiceImpl extends ServiceImpl<PaymentOrderMapper, PaymentOrder> implements IPaymentOrderService {}
