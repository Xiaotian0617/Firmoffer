package com.ailu.firmoffer.exchange.conversion;

import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalance;
import com.ailu.firmoffer.dao.bean.FirmOfferMatchHist;
import com.ailu.firmoffer.dao.bean.FirmOfferOrderHist;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferExchangeBalanceExt;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferMatchHistExt;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferOrderHistExt;
import com.ailu.firmoffer.util.Dic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/23 12:00
 */
@Slf4j
@Component
public class HuobiConversion implements ExchangeConversion{

    @Resource
    FirmOfferExchangeBalanceExt balanceExt;

    @Resource
    FirmOfferOrderHistExt orderHistExt;

    @Resource
    FirmOfferMatchHistExt matchhistExt;

    /**
     * 将账户资产进行入库更新操作
     *
     * @param key      Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void balance(String[] key, String contents) {
        /*
         * 判断是否触发调用订单查询
         * 1.如果传入历史订单信息不存在，添加一次查询。
         * 2.如果历史信息存在，且如果有余额变动，添加一次查询
         */
        List<FirmOfferExchangeBalance> firmOfferExchangeBalances = getListFromContents(contents,FirmOfferExchangeBalance.class);
        if (Dic.DELETE.equalsIgnoreCase(key[3])) {
            //批量更新
            balanceExt.exchangeBalanceUpdate(firmOfferExchangeBalances);
        } else {
            //批量添加
            if (firmOfferExchangeBalances.size() > 1) {
                balanceExt.exchangeBalanceUpdate(firmOfferExchangeBalances);
            }
        }
    }

    /**
     * 将期货订单列表进行入库更新操作
     *
     * @param key      Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void orderList(String[] key, String contents) {
        orderHistExt.insertDuplicateUpdate(getListFromContents(contents,FirmOfferOrderHist.class));
    }

    /**
     * 将现货订单列表进行入库更新操作
     *
     * @param key      Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void matchList(String[] key, String contents) {
        matchhistExt.insertDuplicateUpdate(getListFromContents(contents,FirmOfferMatchHist.class));
    }

    /**
     * 将持仓信息进行入库更新操作
     *
     * @param key      Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void position(String[] key, String contents) {

    }

    /**
     * 从数据中心获取最新价格的换算
     *
     * @param instrumentId
     * @return
     */
    @Override
    public BigDecimal getNowPriceByInstrumentId(String instrumentId) {
        return null;
    }
}
