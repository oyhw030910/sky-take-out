package com.sky.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.webSocket.WebSocketServer;
import javassist.tools.web.Webserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.gaode.key}")
    private String key;


    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersDTO) {
        AddressBook addressBook=addressMapper.getById(ordersDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        isReachable(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());

        ShoppingCart shoppingCart=ShoppingCart.builder().id(BaseContext.getCurrentId()).build();
        List<ShoppingCart> shoppingCartList=shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList==null||shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());

        String address=addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail();
        orders.setAddress(address);

        orderMapper.insert(orders);

        List<OrderDetail> list=new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            list.add(orderDetail);
        }
        orderDetailMapper.insertBatch(list);

        shoppingCartMapper.delete(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());

        OrderSubmitVO orderSubmitVO=OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
        return orderSubmitVO;
    }

    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject=new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        paySuccess(ordersPaymentDTO.getOrderNumber());

        return vo;
    }

    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        Map map=new HashMap<>();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号："+outTradeNo);
        String json=JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult historyQuery(int page, int pageSize, Integer status) {
        PageHelper.startPage(page,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);


        Page<Orders> pages=orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOS=new ArrayList<>();
        for (Orders orders : pages) {
            OrderVO orderVO=new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);
            orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orderVO.getId()));
            orderVOS.add(orderVO);
        }

        return new PageResult(pages.getTotal(),orderVOS);
    }

    @Override
    public OrderVO orderDetail(Long id) {
        Orders orders=orderMapper.getById(id);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(id));
        return orderVO;
    }

    @Override
    public void cancel(Long id) {
        Orders orders=Orders.builder().id(id).status(Orders.CANCELLED).build();
        orderMapper.update(orders);
    }

    @Override
    public void repeat(Long id) {
        List<OrderDetail> list=orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList=new ArrayList<>();
        for (OrderDetail orderDetail : list) {
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCartList.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> pages=orderMapper.AdminPageQuery(ordersPageQueryDTO);

        List<OrderVO> list=new ArrayList<>();
        for (Orders page : pages) {
            OrderVO orderVO=new OrderVO();
            BeanUtils.copyProperties(page,orderVO);
            orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orderVO.getId()));

            List<String> temp=new ArrayList<>();
            for (OrderDetail orderDetail : orderVO.getOrderDetailList()) {
                String dishes=orderDetail.getName()+'*'+orderDetail.getNumber();
                temp.add(dishes);
            }

            orderVO.setOrderDishes(String.join(",",temp));
            list.add(orderVO);
        }
        return new PageResult(pages.getTotal(),list);
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        List<Orders> ordersList=orderMapper.AdminPageQuery(new OrdersPageQueryDTO());
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        Integer toBeConfirmed=0;
        Integer confirmed=0;
        Integer deliveryInProgress=0;
        for (Orders orders : ordersList) {
            if(orders.getStatus().equals(Orders.TO_BE_CONFIRMED))toBeConfirmed++;
            else if(orders.getStatus().equals(Orders.CONFIRMED))confirmed++;
            else if(orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS))deliveryInProgress++;
        }
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(Long id) {
        Orders orders=Orders.builder().id(id).status(Orders.CONFIRMED).build();
        orderMapper.update(orders);
    }

    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders=orderMapper.getById(ordersRejectionDTO.getId());

        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        if(orders.getPayStatus().equals(Orders.PAID))log.info("申请退款,{}",orders.getAmount());

        BeanUtils.copyProperties(ordersRejectionDTO,orders);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders=orderMapper.getById(ordersCancelDTO.getId());
        if(orders.getPayStatus().equals(Orders.PAID))log.info("申请退款,{}",orders.getAmount());
        BeanUtils.copyProperties(ordersCancelDTO,orders);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void deliver(Long id) {
        Orders orders=orderMapper.getById(id);
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders=orderMapper.getById(id);
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void remind(Long id) {
        Orders orders=orderMapper.getById(id);
        if(orders==null)throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        Map map=new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号："+orders.getNumber());

        String json=JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    private void isReachable(String address){
        log.info(shopAddress);
        log.info(key);

        Map map=new HashMap<>();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("key",key);
        String shopCoordinate= HttpClientUtil.doGet("https://restapi.amap.com/v3/geocode/geo?",map);

        JSONObject jsonObject=JSONObject.parseObject(shopCoordinate);
        if(jsonObject==null||jsonObject.getString("status").equals("0")){
            log.info("失败原因,{}",jsonObject.getString("info"));
            throw new OrderBusinessException("店铺地址解析失败");
        }
        shopCoordinate=jsonObject.getJSONArray("geocodes").getJSONObject(0).getString("location");

        map=new HashMap<>();
        map.put("address",address);
        map.put("output","json");
        map.put("key",key);
        String userCoordinate= HttpClientUtil.doGet("https://restapi.amap.com/v3/geocode/geo?",map);

        jsonObject=JSONObject.parseObject(userCoordinate);
        if(jsonObject==null||jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }
        userCoordinate=jsonObject.getJSONArray("geocodes").getJSONObject(0).getString("location");

        map.put("origins",shopCoordinate);
        map.put("destination",userCoordinate);
        map.put("type","1");
        map.put("output","json");
        map.put("key",key);
        String distance=HttpClientUtil.doGet("https://restapi.amap.com/v3/distance?",map);
        jsonObject=JSONObject.parseObject(distance);
        if(jsonObject==null||jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("获取距离失败");
        }
        log.info("{}",jsonObject);
        JSONArray results=(JSONArray) jsonObject.get("results");
        int length=Integer.valueOf((String) results.getJSONObject(0).get("distance"));

        if(length>5000){
            throw new OrderBusinessException("超出配送范围");
        }
    }
}
