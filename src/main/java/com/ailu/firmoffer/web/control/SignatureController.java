package com.ailu.firmoffer.web.control;

import com.ailu.firmoffer.web.WebResult;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.ailu.firmoffer.util.MD5Util.buildMysignV1;

/**
 * @Description: 返回给各个交易所需要的 签名
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/5/4 14:51
 */
@Slf4j
@RestController
@RequestMapping(path = "/signature", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SignatureController {

    @PostMapping(value = "/okex")
    public WebResult okexSignature(@RequestParam("apiKey") String apiKey, @RequestParam("apiKeySecret") String apiKeySecret, @RequestParam("json") String json) {
        String str = null;
        try {
            Map<String, String> params;
            params = JSON.parseObject(json, Map.class);
            str = buildMysignV1(params, apiKeySecret);
        } catch (Exception e) {
            return WebResult.failResult(9999);
        }
        return WebResult.okResult(str);
    }


}
