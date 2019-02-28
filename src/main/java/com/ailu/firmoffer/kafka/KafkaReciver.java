package com.ailu.firmoffer.kafka;

import com.ailu.firmoffer.dao.bean.FirmOfferLedgerHist;
import com.ailu.firmoffer.exchange.conversion.*;
import com.ailu.firmoffer.service.FirmOfferLedgerHist2Service;
import com.ailu.firmoffer.util.Dic;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 09/03/2018 15:19
 */
@Component
@Slf4j
public class KafkaReciver {

    @Resource
    OkexV3Conversion okexV3Conversion;

    @Resource
    HuobiConversion huobiConversion;

    @Resource
    BinanceConversion binanceConversion;

    @Resource
    BitmexConversion bitmexConversion;

    @Resource
    ByBitConversion byBitConversion;

    @Resource
    BitfinexConversion bitfinexConversion;

    @Autowired
    FirmOfferLedgerHist2Service firmOfferLedgerHist2Service;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @KafkaListener(topics = "${kafka.okex}", groupId = "${kafka.groupid}")
    public void receiveOkex(ConsumerRecord consumerRecord) {
        try {
            log.trace("received okex topic value='{}' ", consumerRecord);
            Object key = consumerRecord.key();
            Object value = consumerRecord.value();
            if (key == null || value == null) {
                log.warn("received okex topic value is null !");
                return;
            }
            threadPoolExecutor.submit(() -> checkKeysToFunction(key.toString(), value.toString(), okexV3Conversion));
        } catch (Throwable e) {
            log.error("接收Kafka中行情信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.huobi}", groupId = "${kafka.groupid}")
    public void receiveHuobi(ConsumerRecord consumerRecord) {
        try {
            log.trace("received huobi topic payload='{}' ", consumerRecord);
            Object key = consumerRecord.key();
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received huobi topic value is null !");
                return;
            }
            checkKeysToFunction(key.toString(), value.toString(), huobiConversion);
        } catch (Throwable e) {
            log.error("接收Kafka中市值信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.bitmex}", groupId = "${kafka.groupid}")
    public void receiveBitmex(ConsumerRecord consumerRecord) {
        try {
            log.trace("received bitmex topic value='{}' ", consumerRecord);
            Object key = consumerRecord.key();
            Object value = consumerRecord.value();
            if (key == null || value == null) {
                log.warn("received bitmex topic value is null !");
                return;
            }
            checkKeysToFunction(key.toString(), value.toString(), bitmexConversion);
        } catch (Throwable e) {
            log.error("接收Bitmex Kafka中行情信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.bybit}", groupId = "${kafka.groupid}")
    public void receiveBybit(ConsumerRecord consumerRecord) {
        try {
            log.trace("received bybit topic value='{}' ", consumerRecord);
            Object key = consumerRecord.key();
            Object value = consumerRecord.value();
            if (key == null || value == null) {
                log.warn("received bybit topic value is null !");
                return;
            }
            checkKeysToFunction(key.toString(), value.toString(), byBitConversion);
        } catch (Throwable e) {
            log.error("接收bybit Kafka中行情信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.binance}", groupId = "${kafka.groupid}")
    public void receiveBinance(ConsumerRecord consumerRecord) {
        try {
            log.trace("received binance topic payload='{}' ", consumerRecord);
            Object key = consumerRecord.key();
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received binance topic value is null !");
                return;
            }
            checkKeysToFunction(key.toString(), value.toString(), binanceConversion);
        } catch (Throwable e) {
            log.error("接收Kafka中市值信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.ledger}", groupId = "${kafka.groupid}")
    public void receiveLedger(ConsumerRecord consumerRecord) {
        try {
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received ledger topic value is null !");
                return;
            }
            JSONObject jsonObject = JSONObject.parseObject(value.toString());
            FirmOfferLedgerHist firmOfferLedgerHist2 = JSONObject.toJavaObject(jsonObject, FirmOfferLedgerHist.class);
            threadPoolExecutor.submit(() -> firmOfferLedgerHist2Service.insertFirmOfferLedgerHist(firmOfferLedgerHist2));
        } catch (Throwable e) {
            log.error("接收Kafka中市值信息出错", e);
        }
    }

//    @KafkaListener(topics = "${kafka.bitfinex}", groupId = "${kafka.groupid}")
//    public void receiveBitfinex(ConsumerRecord consumerRecord) {
//        try {
//            log.trace("received bitfinex topic payload='{}' ", consumerRecord);
//            Object key = consumerRecord.key();
//            Object value = consumerRecord.value();
//            if (value == null) {
//                log.warn("received binance marketcap topic value is null !");
//                return;
//            }
//            checkKeysToFunction(key.toString(),value.toString(),bitfinexConversion);
//        } catch (Throwable e) {
//            log.error("接收Kafka中市值信息出错", e);
//        }
//    }

    /**
     * 根据 Key 值区分不同的类型
     *
     * @param key                eg:OKEX-BLANCE-FUTURE-DELETE-75 交易所-类型-订单类型-更新方式-用户 ID
     * @param content
     * @param exchangeConversion
     */
    private void checkKeysToFunction(String key, String content, ExchangeConversion exchangeConversion) {
        String[] keys = key.split("-");
        if (keys.length == 0) {
            log.error("从 Kafka中接收Key 值有误，跳过本次账户更新！Key:" + key);
            return;
        }
        if (key.contains(Dic.BLANCE)) {
            exchangeConversion.balance(keys, content);
            return;
        }
        if (key.contains(Dic.ORDER)) {
            exchangeConversion.orderList(keys, content);
            return;
        }
        if (key.contains(Dic.POSITION)) {
            exchangeConversion.position(keys, content);
            return;
        }
        if (key.contains(Dic.MATCH)) {
            exchangeConversion.matchList(keys, content);
            return;
        }
        if (key.contains(Dic.DEPOSIT)) {
            exchangeConversion.depositList(keys, content);
            return;
        }
        if (key.contains(Dic.LEDGER)) {
            exchangeConversion.ledgerList(keys, content);
            return;
        }
        log.warn("未匹配到相应的 key:{}  是加新类型的吗？", key);
    }


}
