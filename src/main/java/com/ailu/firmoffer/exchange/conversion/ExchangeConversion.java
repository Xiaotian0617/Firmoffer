package com.ailu.firmoffer.exchange.conversion;

import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalance;
import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalanceExample;
import com.ailu.firmoffer.dao.mapper.FirmOfferExchangeBalanceMapper;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferExchangeBalanceExt;
import com.alibaba.fastjson.JSONArray;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易所类型转换实体
 */
public interface ExchangeConversion {

    /**
     * 将账户资产进行入库更新操作
     * @param key Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    void balance(String[] key,String contents);

    /**
     * 将期货订单列表进行入库更新操作
     * @param key Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    void orderList(String[] key,String contents);

    /**
     * 将现货订单列表进行入库更新操作
     * @param key Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    void matchList(String[] key,String contents);

    /**
     * 将持仓信息进行入库更新操作
     * @param key Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    void position(String[] key,String contents);

    /**
     * 将账单流水操作列表进行入库更新操作
     * @param key Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    default void ledgerList(String[] key,String contents){};

    /**
     * 将充值历史操作列表进行入库更新操作
     * @param key Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    default void depositList(String[] key,String contents){};

    /**
     * 从数据中心获取最新价格的换算
     * @param instrumentId
     * @return
     */
    BigDecimal getNowPriceByInstrumentId(String instrumentId);


    default <T> List<T> getListFromContents(String contents,Class clazz){
        List<T> list = JSONArray.parseArray(contents).toJavaList(clazz);
        return list;
    }


}
