package xyz.zionjo.littlemall.product;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.zionjo.littlemall.product.entity.CategoryEntity;
import xyz.zionjo.littlemall.product.service.CategoryService;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LittlemallProductApplicationTests {

    @Autowired
    CategoryService categoryService;

    @Test
    public void contextLoads() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径： {}", Arrays.asList(catelogPath));
    }


}
