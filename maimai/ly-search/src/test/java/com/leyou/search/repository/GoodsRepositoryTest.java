package com.leyou.search.repository;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
   @Autowired
    private GoodsRepository goodsRepository;
   @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
   @Autowired
    private GoodsClient goodsClient;
   @Autowired
   private SearchService searchService;

    /**
     * 添加索引
     */
   @Test
    public void testCreateIndex(){
       elasticsearchTemplate.createIndex(Goods.class);
       elasticsearchTemplate.putMapping(Goods.class);
   }

    /**
     * 导入数据
     */
   @Test
    public void loadData(){
       int page=1;
       int rows=100;
       int pagesize=0;
       do {
           //查询spu数据
           PageResult<Spu> result = goodsClient.querySpuByPage(page, 100, true, null);
           List<Spu> spuList = result.getItems();
           if(CollectionUtils.isEmpty(spuList)){
                  break;
           }
           //构建goods
           List<Goods> goodslist = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());
           //存入索引库
           goodsRepository.saveAll(goodslist);
           page++;
           pagesize=spuList.size();
       }while(pagesize==100);
   }

   @Test
   public void testGoods(){
       PageResult<Spu> result = goodsClient.querySpuByPage(1, 100, true, null);
       System.out.println(result.getItems());
   }

}