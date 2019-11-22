package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据分类查规格参数组
     * @param cid
     * @return
     */
    @GetMapping("/groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupById(@PathVariable("cid")Long cid){
          return ResponseEntity.ok(specificationService.queryGroupById(cid));
    }

    /**
     * 根据规格组/商品分类/是否查询来查询商品规格参数
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamByGid(
            @RequestParam(value="gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value="searching",required = false)Boolean searching
    ){
        return ResponseEntity.ok(specificationService.queryParamList(gid,cid,searching));
    }

    /**
     * 根据分类查询规格组和组参数
     * @param cid
     * @return
     */
    @GetMapping("/group")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@RequestParam("cid") Long cid){
          return ResponseEntity.ok(specificationService.queryGroupByCid(cid));
    }

}
