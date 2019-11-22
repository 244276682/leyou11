package com.leyou.item.web;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@RestController
public class GoodsController {

   @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询spu
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
   @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
           @RequestParam(value = "page", defaultValue = "1") Integer page,
           @RequestParam(value = "rows", defaultValue = "5") Integer rows,
           @RequestParam(value = "saleable", required = false) Boolean saleable,
           @RequestParam(value = "key", required = false) String key
   ){
       return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,saleable,key));
   }

    /**
     * 保存商品信息
     * @param spu
     * @return
     */
   @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu){
          goodsService.saveGoods(spu);
          return ResponseEntity.status(HttpStatus.CREATED).build();
   }
    /**
     * 根据spuid查询商品详情detail
     */
   @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail>  queryDetailById(@PathVariable("id") Long spuId){
       return ResponseEntity.ok(goodsService.queryDetailById(spuId));
   }

    /**
     * 根据spuid查询sku列表
     * @param id
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id")Long id){
        return ResponseEntity.ok(goodsService.querySkuBySpuId(id));
    }
    /**
     * 修改商品信息
     * @param spu
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("/spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    /**
     * 根据sku的id集合查询所有sku
     * @param ids
     * @return
     */
    @GetMapping("/sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids")List<Long> ids){
        return ResponseEntity.ok(goodsService.querySkuByIds(ids));
    }

    /**
     * 减库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts){
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
