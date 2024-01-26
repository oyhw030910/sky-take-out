package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {


    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number = number +1 where id=#{id}")
    void inc(ShoppingCart cart);

    void insert(ShoppingCart shoppingCart);

    void delete(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number = number -1 where id=#{id}")
    void dec(ShoppingCart shoppingCart);
}
