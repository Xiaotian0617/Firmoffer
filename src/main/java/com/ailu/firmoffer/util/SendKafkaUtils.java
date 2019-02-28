package com.ailu.firmoffer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * file:PushKafkaUtils
 * <p>
 * 发送队列往Kafka推送数据方法
 *
 * @author 11:03  王楷
 * @author yangjunxiao
 * @version 11:03 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
public class SendKafkaUtils {

    @Resource
    private KafkaTemplate kafkaTemplate;

    @Value("${kafka.firmOffer_push.topic}")
    private String firmOfferTopic;

    @Value("${kafka.firm_offer_web.meta}")
    private String firmOfferMeta;

    @Value("${kafka.firm_offer_web.history}")
    private String firmOfferHistory;

    public void sendFirmOffer(String event) {
        kafkaTemplate.send(firmOfferTopic, event);
    }

    public void sendFirmOfferMeta(String event) {
        kafkaTemplate.send(firmOfferMeta, event);
    }

    public void sendFirmOfferHistory(String event) {
        kafkaTemplate.send(firmOfferHistory, event);
    }

}
