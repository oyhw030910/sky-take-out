package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    void update(Orders orders);

    Page<OrderVO> getByStatus(int status);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    Page<Orders> AdminPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where status=#{status} and order_time<#{time}")
    List<Orders> getByStatusAndTime(Integer status, LocalDateTime time);


    Double getSumByMap(Map map);

    Integer getCountByMap(Map map);

    List<GoodsSalesDTO> getTop(LocalDate begin, LocalDate end);
}
