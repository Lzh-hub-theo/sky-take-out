package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    CategoryMapper categoryMapper;

    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO ,dish);

        dishMapper.insert(dish);

        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
        }

        dishFlavorMapper.insertBatch(flavors);

    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        List<DishVO> lists = dishMapper.pageQuery(dishPageQueryDTO);
        Page<DishVO> p=(Page<DishVO>) lists;

        PageResult pageResult = new PageResult();
        pageResult.setTotal(p.getTotal());
        pageResult.setRecords(p.getResult());
        return pageResult;
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        /*
        要求：
        起售转态不能删
        关联套餐不能删
        删完后相关联的口味都删掉
         */
        if(ids==null || ids.size()<=0){throw new BaseException("未知异常");}

        List<Integer> statusList = dishMapper.queryStatusByIds(ids);
        for(Integer status:statusList){
            if(status == StatusConstant.ENABLE) throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }

        List<Long> setmealIds = setmealDishMapper.querySetmealIdByDishIds(ids);
        if(setmealIds!=null && setmealIds.size()>0) throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);

        dishMapper.deleteBatch(ids);

        dishFlavorMapper.deleteBatchByDishIds(ids);
    }

    /**
     * 修改菜品
     * @param dishVO
     */
    @Override
    @Transactional
    public void modifyWithFlavor(DishVO dishVO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishVO,dish);

        dishMapper.modify(dish);
        List<DishFlavor> flavors = dishVO.getFlavors();
        Long id = dishVO.getId();
        flavors.forEach(flavor -> flavor.setDishId(id));

        ArrayList<Long> ids=new ArrayList<>();
        ids.add(id);
        dishFlavorMapper.deleteBatchByDishIds(ids);
        dishFlavorMapper.insertBatch(flavors);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    @Transactional
    public DishVO queryById(Long id) {
        Dish dish = dishMapper.queryById(id);

        List<DishFlavor> flavors = dishFlavorMapper.queryByDishId(id);

        String categoryName = categoryMapper.queryNameById(dish.getCategoryId());

        DishVO dishVO=new DishVO();

        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setCategoryName(categoryName);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    @Transactional
    public List<DishVO> queryByCategoryId(Long categoryId) {
        List<DishVO> dishVOs=new ArrayList<>();

        Dish d = Dish.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
        List<Dish> dishes = dishMapper.queryBatchByCategoryId(d);

        for(Dish dish:dishes){
            DishVO temp=new DishVO();
            List<DishFlavor> flavors = dishFlavorMapper.queryByDishId(dish.getId());

            BeanUtils.copyProperties(dish,temp);
            temp.setFlavors(flavors);

            dishVOs.add(temp);
        }

        return dishVOs;
    }

    @Override
    public void modifyStatus(Dish dish) {
        dishMapper.modify(dish);
    }

    @Override
    public List<Dish> listByCategoryId(Long categoryId) {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        List<Dish> list = dishMapper.queryBatchByCategoryId(dish);
        return list;
    }
}
