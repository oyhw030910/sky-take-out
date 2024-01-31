package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Api(tags = "统计相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation("统计营业额")
    public Result<TurnoverReportVO> turnoverStats(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("统计营业额,{},{}",begin,end);
        TurnoverReportVO turnoverReportVO=reportService.turnoverStats(begin,end);
        return Result.success(turnoverReportVO);
    }

    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userStats(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("统计用户,{},{}",begin,end);
        UserReportVO userReportVO=reportService.userStats(begin,end);
        return Result.success(userReportVO);
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> orderStats(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("统计订单,{},{}",begin,end);
        OrderReportVO orderReportVO=reportService.orderStats(begin,end);
        return Result.success(orderReportVO);
    }

    @GetMapping("/top10")
    @ApiOperation("销量前十")
    public Result<SalesTop10ReportVO> getTop(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("统计销量前十,{},{}",begin,end);
        SalesTop10ReportVO salesTop10ReportVO=reportService.getTop(begin,end);
        return Result.success(salesTop10ReportVO);
    }

}
