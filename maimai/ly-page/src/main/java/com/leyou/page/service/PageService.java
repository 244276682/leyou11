package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private TemplateEngine templateEngine;

    public Map<String,Object> loadModel(Long spuId) {
        Map<String,Object> model=new HashMap<>();
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //查询skus
        List<Sku> skus = spu.getSkus();
        //查询详情
        SpuDetail spuDetail = spu.getSpuDetail();
        //查询brand
        Brand brand = brandClient.queryBrandBybId(spu.getBrandId());
        //查询商品分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询规格参数
        List<SpecGroup> specGroups = specificationClient.queryGroupByCid(spu.getCid3());

        model.put("title",spu.getTitle());
        model.put("subTitle",spu.getSubTitle());
        model.put("skus",skus);
        model.put("detail",spuDetail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("specs",specGroups);
        return model;
    }
    public void createHtml(Long spuId){
        //上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File dest=new File("F:\\upload\\images",spuId+".html");
        //如果存在就删除
        if(dest.exists()){
           dest.delete();
        }
        try(PrintWriter writer=new PrintWriter(dest,"UTF-8")){
            //生成Html
            templateEngine.process("item",context,writer);
        }catch (Exception e){
            log.error("【静态服务】页面生成异常",e);
        }

    }

    public void deleteHtml(Long spuId) {
        File dest=new File("F:\\upload\\images",spuId+".html");
        if(dest.exists()){
            dest.delete();
        }
    }
}
