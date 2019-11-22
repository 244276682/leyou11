package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.base.delete.DeleteMapper;

@org.apache.ibatis.annotations.Mapper
public interface CategoryMapper extends Mapper<Category>,IdListMapper<Category,Long>{

}
