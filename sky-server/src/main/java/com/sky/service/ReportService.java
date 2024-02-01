package com.sky.service;

import com.sky.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    TurnoverReportVO turnoverStats(LocalDate begin, LocalDate end);

    UserReportVO userStats(LocalDate begin, LocalDate end);

    OrderReportVO orderStats(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getTop(LocalDate begin, LocalDate end);

    void exportBusinessData(HttpServletResponse response);
}
