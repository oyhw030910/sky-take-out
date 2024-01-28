package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/order")
@Api(tags = "订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("订单查询")
    public Result<PageResult> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO){
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        log.info("查询订单,{}",ordersPageQueryDTO);
        PageResult pageResult=orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
}
