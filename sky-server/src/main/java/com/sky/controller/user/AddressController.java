package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags = "地址相关接口")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping
    @ApiOperation("新增地址")
    public Result add(@RequestBody AddressBook addressBook){
        log.info("新增地址,{}",addressBook);
        addressService.save(addressBook);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询用户当前所有地址")
    public Result<List<AddressBook>> list(){
        log.info("查询用户当前所有地址");
        List<AddressBook> list=addressService.list();
        return Result.success(list);
    }

    @GetMapping("/default")
    @ApiOperation("查询所有默认地址")
    public Result<AddressBook> listDefault(){
        log.info("查询所有默认地址");
        AddressBook addressBook=addressService.listDefault();
        return Result.success(addressBook);
    }

    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook){
        log.info("设置默认地址,{}",addressBook);
        addressService.setDefault(addressBook);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id){
        log.info("根据id查询地址,{}",id);
        AddressBook addressBook=addressService.getById(id);
        return Result.success(addressBook);
    }

    @PutMapping
    @ApiOperation("修改地址")
    public Result change(@RequestBody AddressBook addressBook){
        log.info("修改地址,{}",addressBook);
        addressService.change(addressBook);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除地址")
    public Result delete(Long id){
        log.info("删除地址,{}",id);
        addressService.delete(id);
        return Result.success();
    }
}
