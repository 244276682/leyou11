package com.leyou.common.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.base.insert.InsertMapper;

@RegisterMapper  //能够被扫描到
public interface BaseMapper<T> extends Mapper<T> ,IdListMapper<T,Long>,InsertListMapper<T> {
}
