package com.zqswjtu.freemall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.zqswjtu.freemall.thirdparty.component.SmsComponent;
import com.zqswjtu.freemall.thirdparty.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class FreemallThirdPartyApplicationTests {

    @Autowired
    SmsComponent smsComponent;

    @Test
    void smsSend() {
        smsComponent.sendSmsCode("18370588582", "12345");
/*        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "cd9aea20edd643ab96f045e1554d4e94";
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", "18370588582");
        querys.put("param", "**code**:12345,**minute**:5");

//smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html

        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<>();


        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Autowired
    OSSClient oss;
    @Test
    void test() throws Exception {
        // 文件上传流
        InputStream inputStream = new FileInputStream("E:\\Recruitment\\Project\\freemall\\freemall-product\\src\\main\\resources\\static\\imgs\\Zelda.png");
        oss.putObject("freemall-bucket", "Zelda.png", inputStream);
        // 关闭OSSClient
        oss.shutdown();
        System.out.println("上传成功!");
    }

}
