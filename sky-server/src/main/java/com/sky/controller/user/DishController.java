package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("UserDishController")
@RequestMapping("/user/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    RedisTemplate redisTemplate;


    @GetMapping("/list")
    @ApiOperation("浏览菜品")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("根据分类id查询菜品,{}",categoryId);
        String key="dishes_"+categoryId;
        List<DishVO> dishVOList= (List<DishVO>) redisTemplate.opsForValue().get(key);
        if(dishVOList!=null&& !dishVOList.isEmpty()){
            return Result.success(dishVOList);
        }
        Dish dish= Dish.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
        dishVOList=dishService.list(dish);
        redisTemplate.opsForValue().set(key,dishVOList);
        return Result.success(dishVOList);
    }
}
