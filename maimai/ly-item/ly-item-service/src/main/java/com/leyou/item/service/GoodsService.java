package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.beans.Transient;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品详情
 */
@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * 查询商品列表
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
           //分页
        PageHelper.startPage(page,rows);
           //过滤
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索字段
        if(StringUtils.isNotBlank(key)) {
            criteria.orLike("title","%"+key+"%");
        }
          //上下架
        if(saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
          //默认排序
         example.setOrderByClause("last_update_time DESC");
          //查询
        List<Spu> list = spuMapper.selectByExample(example);
          //判断
        if (CollectionUtils.isEmpty(list)){
          throw new LyException(ExceptionEnum.GOOS_NOT_FOUND);
        }
          //解析分类和商品名称
         loadCategoryAndBrandName(list);

          //解析分页结果
        PageInfo<Spu> pageInfo =new PageInfo<>(list);
        PageResult<Spu> pageResult = new PageResult<>(pageInfo.getTotal(), list);
        return pageResult;
    }

    /**
     * 处理分类和品牌
     * @param list
     */
    private void loadCategoryAndBrandName(List<Spu> list){
        for (Spu spu : list) {
            //处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
            .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            //处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }

    }

    /**
     * 保存商品信息
     * @param spu
     */

    public  void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增detail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        SaveSkuAndStock(spu);
        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());

    }

    private void SaveSkuAndStock(Spu spu) {
        int count;
        //新增sku
        List<Sku> skus = spu.getSkus();
        //Stock集合
        List<Stock> stocks=new ArrayList<>();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            count = skuMapper.insert(sku);
            if(count!=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stocks.add(stock);
        }
        //批量新增库存
        count=stockMapper.insertList(stocks);
        if(count!=stocks.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    /**
     * 根据spuid查询detail
     * @param spuId
     * @return
     */
    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    /**
     * 根据spuid查询sku列表
     * @param id
     * @return
     */
    public List<Sku> querySkuBySpuId(Long id) {
        //查询sku
        Sku sku=new Sku();
        sku.setSpuId(id);
        List<Sku> list = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
      /* //查询库存
        for (Sku s : list) {
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            if(stock==null){
                throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
            }
            s.setStock(stock.getStock());
        }*/
      //批量查询库存
        List<Long> skuids = list.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stocks = stockMapper.selectByIdList(skuids);
       if(CollectionUtils.isEmpty(stocks)){
           throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
       }
       //把stock变成一个map，其中key是：sku的id，值是库存值
        Map<Long, Integer> stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
       list.forEach(
          s->s.setStock(stockMap.get(s.getId()))
       );
        return list;
    }
    @Transactional
    public void updateGoods(Spu spu) {
        if(spu.getId()==null){
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_NULL);
        }
        Sku sku=new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skulist = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skulist)){
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skulist.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
       //修改spu
        spu.setValid(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setSaleable(null);
        int count = spuMapper.updateByPrimaryKeySelective(spu);//如果属性值为空就不更新
        if(count!=1){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
       //修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(count!=1){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //新增sku和stock
        SaveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());

    }

    /**
     * 根据spu的id查询spu detail和sku
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            throw new LyException(ExceptionEnum.GOOS_NOT_FOUND);
        }
        //查询sku
        spu.setSkus(querySkuBySpuId(id));
        //查询detail
        spu.setSpuDetail(queryDetailById(id));
        return  spu;
    }

    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //批量查询库存
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(stocks)){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        //把stock变成一个map，其中key是：sku的id，值是库存值
        Map<Long, Integer> stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(
                s->s.setStock(stockMap.get(s.getId()))
        );
        return skus;
    }
    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            //减库存
            int count=stockMapper.decreaseStock(cart.getSkuId(),cart.getNum());
            if(count!=1){
               throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
