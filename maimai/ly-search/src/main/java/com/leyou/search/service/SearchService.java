package com.leyou.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonToUtils;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;

import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.Field;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate template;

    /**
     * 把商品信息封装到goods中
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu){


        /**
         * 搜索字段，包含标题，分类，品牌，规格等
         * */
         //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        //查询品牌
        Brand brand = brandClient.queryBrandBybId(spu.getBrandId());
        if(brand==null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //搜索字段
        String all=spu.getTitle()+ StringUtils.join(names," ")+brand.getName();

        /**
         * 所有sku的价格的集合
         * */
        //查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
         //处理sku
        List<Map<String,Object>> skumaps = new ArrayList<>();
        //价格集合
        Set<Long> prices=new HashSet<>();
        for (Sku sku : skuList) {
            Map<String,Object> skumap=new HashMap<>();
            skumap.put("id",sku.getId());
            skumap.put("title",sku.getTitle());
            skumap.put("price",sku.getPrice());
            skumap.put("image",StringUtils.substringBefore(sku.getImages(),","));
            skumaps.add(skumap);
            //处理价格
            prices.add(sku.getPrice());
        }
        //查询价格
       // Set<Long> prices = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());

        /**
         * 所有sku集合的json数据
         * */
        ObjectMapper objectMapper=new ObjectMapper();
        String skusjson = null;
        try {
             skusjson = objectMapper.writeValueAsString(skumaps);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        /**
         * 所有可搜索的规格参数
         * */
        Map<String, Object> specs=new HashMap<String, Object>();
         //查询规格参数
        List<SpecParam> parms = specificationClient.queryParamByGid(null, spu.getCid3(), true);
        //查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spu.getId());
         //获取通用规格参数
        String genericSpecjson = spuDetail.getGenericSpec();
           //转成Map
        Map<Long, String> genericSpecMap = JsonUtils.parseMap(genericSpecjson, Long.class, String.class);
        //获取特有规格参数
        String specialSpecjson = spuDetail.getSpecialSpec();
           //转成Map
        Map<String, List<String>> specialSpecListMap = JsonUtils.nativeRead(specialSpecjson, new TypeReference<Map<String, List<String>>>() {
        });
        for (SpecParam parm : parms) {
            //规格名称
            String key = parm.getName();
            Object value="";
            if(parm.getGeneric()){
                value=genericSpecMap.get(parm.getId());
                //判断是否是数值类型
                if(parm.getNumeric()){
                   //处理成段
                    value= chooseSegment(value.toString(), parm);
                }
            }else{
                value=specialSpecListMap.get(parm.getId());
            }
            //存入map
            specs.put(key,value);
        }

        Goods goods=new Goods();
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(all);   // 搜索字段，包含标题，分类，品牌，规格等
        goods.setPrice(prices); // 所有sku的价格的集合
        goods.setSkus(skusjson);// 所有sku集合的json数据
        goods.setSpecs(specs); // 所有可搜索的规格参数
        return goods;
    }

    /**
     * 处理搜索数值区间分段的方法
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    public SearchResult search(SearchRequest searchRequest) {
        int page=searchRequest.getPage()-1;
        int size=searchRequest.getSize();

        //创建查询构建器
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();
        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        //分页
        queryBuilder.withPageable(PageRequest.of(page,size));
        //过滤
        QueryBuilder basicQuery = buildBasicQuery(searchRequest);
        queryBuilder.withQuery(basicQuery);
        //聚合分类和品牌
        String categoryAggName="category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        String brandAggName="brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //查询
       // Page<Goods> result = goodsRepository.search(queryBuilder.build());
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        //解析结果
        long total= result.getTotalElements();  //总条数
        int totalPages = result.getTotalPages();  //总页数
        List<Goods> goodsList = result.getContent(); //当前页数据
        //解析聚合结果
        Aggregations aggregations = result.getAggregations();
        List<Category> categories=parseCategoryAgg(aggregations.get(categoryAggName));
        List<Brand> brands=parseBrandAgg(aggregations.get(brandAggName));
        //其他的规格参数
        List<Map<String,Object>> specs=null;
        if(categories!=null&&categories.size()==1){
            specs=buildSpecificationAgg(categories.get(0).getId(),basicQuery);
        }
        return new SearchResult(total,(long) totalPages,goodsList,categories,brands,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //创建布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        Map<String,String> map=request.getFilter();
        for(Map.Entry<String,String> entry:map.entrySet()){
            String key=entry.getKey();
            //处理key
            if(!"cid3".equals(key)&&!"brandId".equals(key)){
                key="specs."+key+".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return queryBuilder;
    }

    private List<Map<String,Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs=new ArrayList<>();
        //查询需要聚合的规格参数
        List<SpecParam> params = specificationClient.queryParamByGid(null, cid, true);
        //聚合
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //带上查询条件
        nativeSearchQueryBuilder.withQuery(basicQuery);
        for (SpecParam param : params) {
            String name=param.getName();
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        //获取结果
        AggregatedPage<Goods> result = template.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        //解析结果
        Aggregations aggs=result.getAggregations();
        for (SpecParam param : params) {
            //规格参数名称
            String name=param.getName();
            /**
             * 这里的内存和机身存储的索引库没有存进去所以直接跳过
             */
            if(name.equals("内存")||name.equals("机身存储")){
                continue;
            }
            StringTerms terms = aggs.get(name);
            List<String> options = terms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList());
            //准备Map
            Map<String,Object> map=new HashMap<>();
            map.put("k",name);
            map.put("options",options);
            specs.add(map);
        }

        return specs;
    }

    private List<Category> parseCategoryAgg(LongTerms aggregation){
        try{
        List<Long> ids = aggregation.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
        List<Category> categories = categoryClient.queryCategoryByIds(ids);
        return categories;}
        catch (Exception e){
            return null;
        }
    }
    private List<Brand> parseBrandAgg(LongTerms aggregation){
       try{
        List<Long> ids = aggregation.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
        List<Brand> brands = brandClient.queryBrandByIds(ids);
        return brands;
       }catch (Exception e){
            return null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods
        Goods goods = buildGoods(spu);
        //存入索引库
        goodsRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
