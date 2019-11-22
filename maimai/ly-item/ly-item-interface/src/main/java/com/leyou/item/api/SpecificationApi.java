package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Spu;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SpecificationApi {
    /**
     * 根据规格组/商品分类/是否查询来查询商品规格参数
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("/spec/params")
    List<SpecParam> queryParamByGid(
            @RequestParam(value="gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value="searching",required = false)Boolean searching
    );


    @GetMapping("/spec/group")
    List<SpecGroup> queryGroupByCid(@RequestParam("cid") Long cid);
}
