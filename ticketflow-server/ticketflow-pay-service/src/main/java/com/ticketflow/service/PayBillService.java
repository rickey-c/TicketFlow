package com.ticketflow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.entity.PayBill;
import com.ticketflow.mapper.PayBillMapper;
import org.springframework.stereotype.Service;

/**
 * @Description: 支付账单 service
 * @Author: rickey-c
 * @Date: 2025/2/10 11:34
 */
@Service
public class PayBillService extends ServiceImpl<PayBillMapper, PayBill> {
}
