package com.ailu.firmoffer.exchange.conversion;

import com.ailu.firmoffer.config.UnitName;
import com.ailu.firmoffer.dao.bean.*;
import com.ailu.firmoffer.dao.mapper.FirmOfferExchangeBalanceMapper;
import com.ailu.firmoffer.dao.mapper.FirmOfferPositionMapper;
import com.ailu.firmoffer.dao.mapper.ext.*;
import com.ailu.firmoffer.service.SymbolRateManager;
import com.ailu.firmoffer.util.Dic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ailu.firmoffer.config.ExchangeName.EX_CHANGE_BITMEX;
import static com.ailu.firmoffer.service.MetaService.EX_CHANGE_HUOBI;
import static com.ailu.firmoffer.service.MetaService.EX_CHANGE_OKEX;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/23 12:00
 */
@Slf4j
@Component
public class BitmexConversion implements ExchangeConversion {

    @Resource
    FirmOfferExchangeBalanceExt balanceExt;

    @Resource
    FirmOfferExchangeBalanceMapper firmOfferExchangeBalanceMapper;

    @Resource
    FirmOfferOrderHistExt orderHistExt;

    @Resource
    FirmOfferPositionMapper firmOfferPositionMapper;

    @Resource
    FirmOfferPositionExt firmOfferPositionExt;

    @Resource
    FirmOfferMatchHistExt matchhistExt;

    @Resource
    SymbolRateManager symbolRateManager;

    @Resource
    private FirmOfferLedgerHistExt firmOfferLedgerHistExt;

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
        List<FirmOfferExchangeBalance> firmOfferExchangeBalances = getListFromContents(contents, FirmOfferExchangeBalance.class);
        if (Dic.DELETE.equalsIgnoreCase(key[3])) {
            log.debug("userId {},type {},变动数据:{}", key[4], key[2], contents);
            //批量更新
            updateblances(key, firmOfferExchangeBalances);
        } else {
            //批量添加
            if (firmOfferExchangeBalances.size() > 0) {
                balanceExt.exchangeBalanceUpdate(firmOfferExchangeBalances);
            }
        }
    }

    private void updateblances(String[] key, List<FirmOfferExchangeBalance> firmOfferExchangeBalances) {
        FirmOfferExchangeBalanceExample firmOfferExchangeBalanceExample = new FirmOfferExchangeBalanceExample();
        firmOfferExchangeBalanceExample.or().andUserIdEqualTo(Long.valueOf(key[4])).andTypeEqualTo(key[2]);
        List<FirmOfferExchangeBalance> oldBalances = firmOfferExchangeBalanceMapper.selectByExample(firmOfferExchangeBalanceExample);
        if (!oldBalances.isEmpty()) {
            String collect = oldBalances.stream().map(blance -> blance.getId().toString()).collect(Collectors.joining("','", "'", "'"));
            balanceExt.deleteByIds(collect);
        }
        balanceExt.exchangeBalanceInsert(firmOfferExchangeBalances);
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
        List<FirmOfferOrderHist> listFromContents = getListFromContents(contents, FirmOfferOrderHist.class);
        List<FirmOfferOrderHist> firmOfferOrderHists = new ArrayList<>();
        for (FirmOfferOrderHist firmOfferOrderHist : listFromContents) {
            if (firmOfferOrderHist.getPrice() == null || firmOfferOrderHist.getOrderStatus() == null) {
                continue;
            }
            firmOfferOrderHists.add(firmOfferOrderHist);
        }
        orderHistExt.insertDuplicateUpdate(firmOfferOrderHists);
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
        matchhistExt.insertDuplicateUpdate(getListFromContents(contents, FirmOfferMatchHist.class));
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
        List<FirmOfferPosition> listFromContents = getListFromContents(contents, FirmOfferPosition.class);
        FirmOfferPositionExample firmOfferPositionExample = new FirmOfferPositionExample();
        firmOfferPositionExample.or().andUserIdEqualTo(Long.valueOf(key[4]));
        firmOfferPositionMapper.deleteByExample(firmOfferPositionExample);
        if (listFromContents.isEmpty()) {
            return;
        }
        firmOfferPositionExt.insertFirmOfferPositionUpdate(listFromContents);
    }

    /**
     * 从数据中心获取最新价格的换算
     *
     * @param tradingOn
     * @return
     */
    @Override
    public BigDecimal getNowPriceByInstrumentId(String tradingOn) {
        if (!StringUtils.hasText(tradingOn)) {
            return BigDecimal.ZERO;
        }
        String unit = "BTC";
        if (tradingOn.contains("XBT")) {
            unit = "USD";
        }
        if (tradingOn.contains("18")) {
            String substring = tradingOn.substring(0, tradingOn.length() - 3);
            tradingOn = substring + "THISMONTH";
        }
        String onlykey = EX_CHANGE_BITMEX + "_" + tradingOn + "_" + unit;
        BigDecimal nowPrice = symbolRateManager.getBitmexSymbolRate(onlykey);
        return nowPrice;
    }

    /**
     * 将账单流水操作列表进行入库更新操作
     *
     * @param key      Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ledgerList(String[] key, String contents) {
        List<FirmOfferLedgerHist> listFromContents = getListFromContents(contents, FirmOfferLedgerHist.class);
        listFromContents.stream().forEach(o -> o.setBalance(new BigDecimal(0)));
        if (listFromContents.isEmpty()) {
            return;
        }
        firmOfferLedgerHistExt.insertDuplicateUpdate(listFromContents);
    }
}
