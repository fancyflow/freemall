package com.zqswjtu.freemall.thirdparty.component;

import com.zqswjtu.freemall.thirdparty.util.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
public class SmsComponent {

    private String host;
    private String path;
    private String appcode;
    private String sign;
    private String template;
    public void sendSmsCode(String mobile, String code) {
//        String host = "https://gyytz.market.alicloudapi.com";
//        String path = "/sms/smsSend";
//        String method = "POST";
//        String appcode = "cd9aea20edd643ab96f045e1554d4e94";
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", mobile);
        querys.put("param", "**code**:" + code + ",**minute**:5");
        // smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。
        // 参考文档：http://help.guoyangyun.com/Problem/Qm.html
        querys.put("smsSignId", sign);
        querys.put("templateId", template);
        Map<String, String> bodys = new HashMap<>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, "POST", headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
