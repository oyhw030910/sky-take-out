package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;
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

    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
