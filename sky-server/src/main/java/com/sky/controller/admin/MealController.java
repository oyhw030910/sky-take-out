package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.MealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/setmeal")
@Api("套餐相关接口")
@Slf4j
public class MealController {
    @Autowired
    MealService mealService;


    @PostMapping
    @ApiOperation("新增套餐")
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
}
