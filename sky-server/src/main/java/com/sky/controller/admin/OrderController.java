package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/statistics")
    @ApiOperation("统计订单数据")
    public Result<OrderStatisticsVO> getStatistics(){
        log.info("查询订单数据");
        OrderStatisticsVO orderStatisticsVO=orderService.getStatistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getDetail(@PathVariable Long id){
        log.info("查询订单数据详情,{}",id);
        OrderVO orderVO=orderService.orderDetail(id);
        return Result.success(orderVO);
    }
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单,{}",ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO.getId());
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result reject(@RequestBody OrdersRejectionDTO rejectionDTO){
        log.info("拒单,{}",rejectionDTO);
        orderService.rejectOrder(rejectionDTO);
        return Result.success();
    }
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result reject(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单,{}",ordersCancelDTO);
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result deliver(@PathVariable Long id){
        log.info("派送订单,{}",id);
        orderService.deliver(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        log.info("完成订单,{}",id);
        orderService.complete(id);
        return Result.success();
    }
}
