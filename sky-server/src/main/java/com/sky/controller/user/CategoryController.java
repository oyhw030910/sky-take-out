package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("UserCategoryController")
@RequestMapping("/user/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping("/list")
    @ApiOperation("分类相关接口")
    public Result<List<Category>> list(Integer type){
        log.info("根据分类查询菜品,{}",type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
