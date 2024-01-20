package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.MealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;

    @Autowired
    MealMapper mealMapper;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO){
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long id=dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null&&!flavors.isEmpty()){
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(id));
        }
        dishFlavorMapper.insertBatch(flavors);

    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> dishVOPage=dishMapper.pageQuary(dishPageQueryDTO);
        return new PageResult(dishVOPage.getTotal(),dishVOPage.getResult());
    }

    @Override
    @Transactional
    public void deleteDishBatch(List<Long> ids) {

        List<Dish> dishes=dishMapper.getDishByIds(ids);
        for (Dish dish : dishes) {
            if(dish.getStatus().equals(StatusConstant.ENABLE))throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
//        for (Long id : ids) {
//            Dish dish=dishMapper.getDishById(id);
//            if(dish.getStatus().equals(StatusConstant.ENABLE))throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
//        }

        List<Long> mealIds=mealMapper.getMealIdByDishIds(ids);
        if(mealIds!=null&&!mealIds.isEmpty())throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);

        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
//        for (Long id : ids) {
//            dishMapper.delete(id);
//            dishFlavorMapper.deleteByDishId(id);
//        }
    }

    @Override
    public DishVO getDishById(Long id) {
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dishMapper.getDishById(id),dishVO);
        List<DishFlavor> dishFlavors=dishFlavorMapper.getFlavorsByDishId(id);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }


    @Override
    @Transactional
    public void changeDish(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> dishFlavors=dishDTO.getFlavors();
        if(dishFlavors!=null&&!dishFlavors.isEmpty()){
            for (DishFlavor dishFlavor : dishFlavors) {
                dishFlavor.setDishId(dishDTO.getId());
            }
        }
        dishFlavorMapper.insertBatch(dishDTO.getFlavors());
    }

    @Override
    public void changeStatus(Dish dish) {
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> getByCategoryId(Long id) {
        Dish dish=Dish.builder().categoryId(id).status(StatusConstant.ENABLE).build();
        return dishMapper.list(dish);

    }

    @Transactional
    @Override
    public List<DishVO> getDishVoByCategoryId(Long categoryId) {
        List<DishVO> dishVOList=new ArrayList<>();
        List<Dish> dishes=dishMapper.getDishByCategoryId(categoryId);
        for (Dish dish : dishes) {
            DishVO dishVO=new DishVO();
            BeanUtils.copyProperties(dish,dishVO);
            List<DishFlavor> flavors=dishFlavorMapper.getFlavorsByDishId(dish.getId());
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }
}
