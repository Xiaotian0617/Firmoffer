package com.ailu.firmoffer.service;

import com.ailu.firmoffer.config.CalcAPI;
import com.ailu.firmoffer.config.ExchangeName;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SymbolRateManager {

    @Resource
    RestTemplateBuilder restTemplateBuilder;

    @Resource
    CalcAPI calcAPI;

    private Map<String, BigDecimal> bitfinexSymbolRate = new HashMap<>();

    private Map<String, BigDecimal> huobiSymbolRate = new HashMap<>();

    private Map<String, BigDecimal> okexSymbolRate = new HashMap<>();

    private Map<String, BigDecimal> binanceSymbolRate = new HashMap<>();

    private Map<String, BigDecimal> biboxSymbolRate = new HashMap<>();

    private Map<String, BigDecimal> bitmexSymbolRate = new HashMap<>();

    /**
     * 从数据中心中获取Huobi和Bitfinex的各个币种汇率
     */
    void init() {
        setBitfinexSymbolRate(getAllMarketByExchange(ExchangeName.EX_CHANGE_BITFINEX));
        setHuobiSymbolRate(getAllMarketByExchange(ExchangeName.EX_CHANGE_HUOBI));
        setOkexSymbolRate(getAllMarketByExchange(ExchangeName.EX_CHANGE_OKEX));
        setBinanceSymbolRate(getAllMarketByExchange(ExchangeName.EX_CHANGE_BINANCE));
        setBitmexSymbolRate(getAllMarketByExchange(ExchangeName.EX_CHANGE_BITMEX));
        //setBiboxSymbolRate(getAllMarketByExchange(ExchangeName.EX_CHANGE_BIBOX));
    }

    private Map<String, BigDecimal> getAllMarketByExchange(String exchangeName) {
        log.info("开始初始化 {} 的汇率信息", exchangeName);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        JSONObject requestBody = new JSONObject();
        requestBody.put("exchangeName", exchangeName);
        HttpEntity entity = new HttpEntity<String>(requestBody.toJSONString(), headers);

        JSONObject responseEntity = restTemplateBuilder.build().postForObject(calcAPI.getAllMarketByExchange(), entity, JSONObject.class);

        if (checkResponse(responseEntity)) {
            log.warn("本次调用数据中心汇率接口返回数据无效，请求交易所为{},本次跳过更新本交易所汇率。", exchangeName);
            return new HashMap<>();
        }
        return responseEntity.getJSONArray("data").stream().map(json -> {
            JSONObject marketJson = (JSONObject) json;
            MarketVO marketVO = new MarketVO(marketJson.getString("key"), marketJson.getBigDecimal("la"));
            return marketVO;
        }).collect(Collectors.toMap(MarketVO::getKey, MarketVO::getPrice));
    }

    private boolean checkResponse(JSONObject requestBody) {
        return requestBody == null || requestBody.getInteger("code") != 0 || requestBody.getJSONArray("data") == null || requestBody.getJSONArray("data").size() == 0;
    }

    public Map<String, BigDecimal> getBitfinexSymbolRate() {
        return bitfinexSymbolRate;
    }

    public Map<String, BigDecimal> getHuobiSymbolRate() {
        return huobiSymbolRate;
    }

    public Map<String, BigDecimal> getOkexSymbolRate() {
        return okexSymbolRate;
    }

    public Map<String, BigDecimal> getBinanceSymbolRate() {
        return binanceSymbolRate;
    }

    public Map<String, BigDecimal> getBiboxSymbolRate() {
        return biboxSymbolRate;
    }

    public BigDecimal getOkexSymbolRate(String onlyKey) {
        return okexSymbolRate.get(onlyKey);
    }

    public BigDecimal getBinanceSymbolRate(String onlyKey) {
        return binanceSymbolRate.get(onlyKey);
    }

    public BigDecimal getBiboxSymbolRate(String onlyKey) {
        return biboxSymbolRate.get(onlyKey);
    }

    public BigDecimal getBitfinexSymbolRate(String onlyKey) {
        return bitfinexSymbolRate.get(onlyKey);
    }

    public BigDecimal getHuobiSymbolRate(String onlyKey) {
        return huobiSymbolRate.get(onlyKey);
    }

    private void setBitfinexSymbolRate(Map<String, BigDecimal> bitfinexSymbolRate) {
        this.bitfinexSymbolRate = bitfinexSymbolRate;
    }

    public BigDecimal getBitmexSymbolRate(String onlyKey) {
        return bitmexSymbolRate.get(onlyKey);
    }

    public SymbolRateManager setBitmexSymbolRate(Map<String, BigDecimal> bitmexSymbolRate) {
        this.bitmexSymbolRate = bitmexSymbolRate;
        return this;
    }

    private void setHuobiSymbolRate(Map<String, BigDecimal> huobiSymbolRate) {
        this.huobiSymbolRate = huobiSymbolRate;
    }

    private void setBiboxSymbolRate(Map<String, BigDecimal> biboxSymbolRate) {
        this.biboxSymbolRate = biboxSymbolRate;
    }

    private void setBinanceSymbolRate(Map<String, BigDecimal> binanceSymbolRate) {
        this.binanceSymbolRate = binanceSymbolRate;
    }

    private void setOkexSymbolRate(Map<String, BigDecimal> okexSymbolRate) {
        this.okexSymbolRate = okexSymbolRate;
    }

    @Data
    class MarketVO {
        private String key;
        private BigDecimal price;

        public MarketVO(String key, BigDecimal price) {
            this.key = key;
            this.price = price;
        }
    }
}
