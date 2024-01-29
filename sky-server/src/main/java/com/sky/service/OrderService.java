package com.sky.service;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    void paySuccess(String outTradeNo);

    PageResult historyQuery(int page, int pageSize, Integer status);

    OrderVO orderDetail(Long id);

    void cancel(Long id);

    void repeat(Long id);

    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO getStatistics();
}
