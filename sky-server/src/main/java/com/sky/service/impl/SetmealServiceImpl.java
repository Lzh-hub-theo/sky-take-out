package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Override
    public List<Setmeal> queryByCategoryId(Long categoryId) {
        List<Setmeal> setmeals = setmealMapper.queryByCategoryId(categoryId);
        return setmeals;
    }

    @Override
    public List<DishItemVO> queryDishById(Long id) {
        List<DishItemVO> dishes = setmealDishMapper.queryDishBySetmealId(id);
        return dishes;
    }

    /**
     * 修改套餐以及修改菜品
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void modifyWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.modify(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        setmealDishMapper.deleteBatchBySetmealId(setmeal.getId());

        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Transactional
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> lists = setmealMapper.listWithCategoryName(setmealPageQueryDTO);

        PageResult res=new PageResult();
        res.setTotal(lists.getTotal());
        res.setRecords(lists.getResult());

        return res;
    }

    @Transactional
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);

        Long setmealId = setmeal.getId();
        setmealDTO.setId(setmealId);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public void modifyStatus(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealMapper.modify(setmeal);
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(Long[] ids) {
        setmealMapper.deleteBatch(ids);
        setmealDishMapper.deleteBatchBySetmealIds(ids);
    }

    @Transactional
    @Override
    public SetmealVO queryById(Long id) {
        SetmealVO setmealVO=new SetmealVO();
        Setmeal setmeal = setmealMapper.queryById(id);
        BeanUtils.copyProperties(setmeal,setmealVO);

        String categoryName = setmealMapper.queryCategoryNameByid(id);
        setmealVO.setCategoryName(categoryName);

        List<SetmealDish> lists = setmealDishMapper.querySetmealDishBySetmealId(id);
        setmealVO.setSetmealDishes(lists);

        return setmealVO;
    }


}
