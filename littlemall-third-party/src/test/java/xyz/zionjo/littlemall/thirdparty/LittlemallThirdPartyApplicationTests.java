package xyz.zionjo.littlemall.thirdparty;

import com.aliyun.oss.OSS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LittlemallThirdPartyApplicationTests {

    @Autowired
    private OSS ossClient;

    @Test
    public void contextLoads() throws FileNotFoundException {
        ossClient.putObject("littlemall-zionjo", "test.jpg", new FileInputStream("E:\\Learning\\谷粒商城\\docs\\pics\\d511faab82abb34b.jpg"));
    }

}
