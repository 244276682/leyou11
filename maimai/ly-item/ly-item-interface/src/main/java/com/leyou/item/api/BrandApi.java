package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface BrandApi {
    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
     Brand queryBrandBybId(@PathVariable("id")Long id);

    /**
     * 根据ids查询品牌
     * @param ids
     * @return
     */
    @GetMapping("/brand/brands")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);
}
