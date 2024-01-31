package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public TurnoverReportVO turnoverStats(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates=getLocalDates(begin,end);
        List<Double> turnoverList=new ArrayList<>();
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(localDate,LocalTime.MAX);

            Map map=new HashMap<>();
            map.put("status",Orders.COMPLETED);
            map.put("begin",beginTime);
            map.put("end",endTime);

            Double turnover=orderMapper.getSumByMap(map);

            if(turnover==null)turnover=0.0;

            turnoverList.add(turnover);
        }


        return TurnoverReportVO.builder().dateList(StringUtils.join(localDates,",")).turnoverList(StringUtils.join(turnoverList,",")).build();
    }

    @Override
    public UserReportVO userStats(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates=getLocalDates(begin,end);
        List<Integer> totalUsers=new ArrayList<>();
        List<Integer> newUsers=new ArrayList<>();
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(localDate,LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("end",endTime);
            Integer newCount=userMapper.countByMap(map);
            map.put("begin",beginTime);
            Integer allCount=userMapper.countByMap(map);
            totalUsers.add(allCount);
            newUsers.add(newCount);
        }
        return UserReportVO.builder().dateList(StringUtils.join(localDates,","))
                .newUserList(StringUtils.join(newUsers,","))
                .totalUserList(StringUtils.join(totalUsers,","))
                .build();
    }

    @Override
    public OrderReportVO orderStats(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates=getLocalDates(begin,end);
        List<Integer> newOrderCount=new ArrayList<>();
        List<Integer> validNewOrderCount=new ArrayList<>();



        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(localDate,LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            Integer newCount=orderMapper.getCountByMap(map);
            map.put("status",Orders.COMPLETED);
            Integer validCount=orderMapper.getCountByMap(map);
            newOrderCount.add(newCount);
            validNewOrderCount.add(validCount);
        }
        Integer totalOrderCount=newOrderCount.stream().reduce(Integer::sum).get();
        Integer validOrderCount=validNewOrderCount.stream().reduce(Integer::sum).get();
        Double rate=0.0;
        if (totalOrderCount!=0)rate=(double)validOrderCount/totalOrderCount;

        return OrderReportVO.builder()
                .totalOrderCount(totalOrderCount)
                .dateList(StringUtils.join(localDates,","))
                .orderCountList(StringUtils.join(newOrderCount,","))
                .validOrderCount(validOrderCount)
                .validOrderCountList(StringUtils.join(validNewOrderCount,","))
                .orderCompletionRate(rate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> list=orderMapper.getTop(begin,end);
        List<String> names=list.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers=list.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
    }

    private List<LocalDate> getLocalDates(LocalDate begin, LocalDate end){
        List<LocalDate> localDates=new ArrayList<>();
        localDates.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            localDates.add(begin);
        }
        return localDates;
    }
}
