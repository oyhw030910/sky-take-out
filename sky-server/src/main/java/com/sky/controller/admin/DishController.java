package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("AdminDishController")
@RequestMapping("/admin/dish")
@Api(tags="菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    @CacheEvict(cacheNames = "mealDish",allEntries = true)
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品,{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        deleteCache("dishes_"+dishDTO.getCategoryId());
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);
        log.info("菜品分页查询,{}",dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteDishBatch(@RequestParam List<Long> ids){
        log.info("批量删除菜品,{}",ids);
        dishService.deleteDishBatch(ids);
        deleteCache("dishes_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getDishById(@PathVariable Long id){
        log.info("查询菜品,{}",id);
        DishVO dishVO=dishService.getDishById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "mealDish",allEntries = true)
    public Result changeDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品,{}",dishDTO);
        dishService.changeDish(dishDTO);
        deleteCache("dishes_*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable int status,Long id){
        Dish dish= Dish.builder().status(status).id(id).build();
        dishService.changeStatus(dish);
        deleteCache("dishes_*");
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation("根据分类id获取菜品")
    public Result<List<Dish>> getByCategoryId(@RequestParam("categoryId") Long id){
        log.info("根据分类查询菜品,{}",id);
        List<Dish> dishes=dishService.getByCategoryId(id);
        return Result.success(dishes);
    }


    private void deleteCache(String pattern){
        Set keys=redisTemplate.keys(pattern);
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }
}
