package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupById(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> list = specGroupMapper.select(specGroup);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPRC_GROUP_NOT_FOUND);
        }
        return list;
    }

    public List<SpecParam> queryParamList(Long gid,Long cid,Boolean searching) {
       SpecParam specParam=new SpecParam();
       specParam.setGroupId(gid);
       specParam.setCid(cid);
       specParam.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(specParam);
       if(CollectionUtils.isEmpty((list))){
           throw new LyException(ExceptionEnum.SPRC_PARM_NOT_FOUND);
       }
       return list;
    }

    public List<SpecGroup> queryGroupByCid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = queryGroupById(cid);
        if(specGroups==null){
            throw new LyException(ExceptionEnum.SPRC_GROUP_NOT_FOUND);
        }
        //查询该组的规格参数
        List<SpecParam> specParams = queryParamList(null, cid, null);
        //先把规格参数变成map，map的key是规格组，map的值是组下的所有参数
        Map<Long,List<SpecParam>> map=new HashMap<>();
        for (SpecParam specParam : specParams) {
           if(!map.containsKey(specParam.getGroupId())){
               map.put(specParam.getGroupId(),new ArrayList<>());
           }
           map.get(specParam.getGroupId()).add(specParam);
        }
        //填充信息
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;

    }
}
