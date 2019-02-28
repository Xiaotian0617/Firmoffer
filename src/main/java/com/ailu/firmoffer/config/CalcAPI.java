package com.ailu.firmoffer.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 数据中心获取API
 */
@Component
public class CalcAPI {

    @Value("${calc.url-prefix}")
    String urlPrefix;


    @Value("${calc.get-all-market-by-exchange}")
    String getAllMarketByExchange;

    public String getAllMarketByExchange() {
        return urlPrefix + getAllMarketByExchange;
    }

}