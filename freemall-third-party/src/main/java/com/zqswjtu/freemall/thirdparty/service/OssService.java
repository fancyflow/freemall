package com.zqswjtu.freemall.thirdparty.service;

import java.util.Map;

public interface OssService {

    /**
     * 返回服务端的签名信息
     * @return
     */
    Map<String, String> getUploadPolicy();
}

