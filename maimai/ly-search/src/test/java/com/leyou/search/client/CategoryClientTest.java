package com.leyou.search.client;

import com.leyou.item.pojo.Category;
import lombok.AllArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {
    @Autowired
    private CategoryClient categoryClient;

    @Test
    public void queryCategoryByPids() {
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(1L, 2L, 3L));
        //断言有3条 如果没有就报错
        Assert.assertEquals(3,categories.size());
        for (Category category : categories) {
            System.out.println("category = " + category);
        }
    }
}