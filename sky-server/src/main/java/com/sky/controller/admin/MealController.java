package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.MealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class MealController {
    @Autowired
    MealService mealService;


    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "mealCache",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐,{}",setmealDTO);
        mealService.save(setmealDTO);
        return Result.success();
    }

    @GetMapping("page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐,{}",setmealPageQueryDTO);
        PageResult pageResult=mealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id获取套餐信息")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id获取套餐信息,{}",id);
        return Result.success(mealService.getById(id));
    }

    @PutMapping
    @ApiOperation("修改套餐")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "mealCache",allEntries = true),
                    @CacheEvict(cacheNames = "mealDish",key="#setmealDTO.id")
            }
    )
    public Result changeMeal(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐,{}",setmealDTO);
        mealService.changeMeal(setmealDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("修改套餐状态")
    @CacheEvict(cacheNames = "mealCache",allEntries = true)
    public Result changeStatus(@PathVariable int status,Long id){
        log.info("修改套餐状态,{}",id);
        mealService.changeStatus(status,id);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "mealCache",allEntries = true)
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("批量删除套餐,{}",ids);
        mealService.deleteBatch(ids);
        return Result.success();
    }
}
