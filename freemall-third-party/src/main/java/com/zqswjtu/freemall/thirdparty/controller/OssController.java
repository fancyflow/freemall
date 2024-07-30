package com.zqswjtu.freemall.thirdparty.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.zqswjtu.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class OssController {

    @Autowired
    OSS client;

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;

    @RequestMapping("/oss/policy")
    public R policy() {
        String bucket = "freemall-bucket";
        String host = "https://" + bucket + "." + endpoint;
        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir = format + "/";
        Map<String, String> map = new LinkedHashMap<>();
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConditions = new PolicyConditions();
            policyConditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConditions.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = client.generatePostPolicy(expiration, policyConditions);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodePolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            map.put("accessid", accessId);
            map.put("policy", encodePolicy);
            map.put("signature", postSignature);
            map.put("dir", dir);
            map.put("host", host);
            map.put("expire", String.valueOf(expireEndTime / 1000));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return R.ok().put("data", map);
    }
}
