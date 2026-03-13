package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("payment_orders")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private Long bizId;

    @TableField("user_id")
    private Long userId;

    @TableField("pay_channel")
    private String payChannel;

    private BigDecimal amount;

    @TableField("currency_code")
    private String currencyCode;

    @TableField("pay_status")
    private String payStatus;

    @TableField("provider_trade_no")
    private String providerTradeNo;

    @TableField("payment_url")
    private String paymentUrl;

    @TableField("qr_code_base64")
    private String qrCodeBase64;

    @TableField("callback_payload_json")
    private String callbackPayloadJson;

    @TableField("paid_at")
    private Date paidAt;

    @TableField("expired_at")
    private Date expiredAt;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
