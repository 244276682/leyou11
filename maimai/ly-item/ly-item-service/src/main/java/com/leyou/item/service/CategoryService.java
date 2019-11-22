package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 查询分类列表
     * @param pid
     * @return
     */
    public List<Category> queryCategoryListByPid(Long pid) {
        // 查询条件 会把非空属性作为查询条件
        Category c=new Category();
        c.setParentId(pid);
        List<Category> list = categoryMapper.select(c);
        if(CollectionUtils.isEmpty(list)){
           throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return list;
    }

    /**
     *根据id集合查询分类名称组
     */
    public List<Category> queryByIds(List<Long> ids){
        List<Category> list = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return list;
    }
}
