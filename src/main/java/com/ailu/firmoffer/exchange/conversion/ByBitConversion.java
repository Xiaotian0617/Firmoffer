package com.ailu.firmoffer.exchange.conversion;

import com.ailu.firmoffer.dao.bean.*;
import com.ailu.firmoffer.dao.mapper.FirmOfferExchangeBalanceMapper;
import com.ailu.firmoffer.dao.mapper.FirmOfferPositionMapper;
import com.ailu.firmoffer.dao.mapper.ext.*;
import com.ailu.firmoffer.service.SymbolRateManager;
import com.ailu.firmoffer.util.ContractUtil;
import com.ailu.firmoffer.util.Dic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ailu.firmoffer.service.MetaService.EX_CHANGE_OKEX;

/**
 * @Description:
 * @author: mr.wang
 * @version: V1.0
 * @date: 2018年11月9日15:42:50
 */
@Slf4j
@Component
public class ByBitConversion implements ExchangeConversion {

    @Resource
    private FirmOfferExchangeBalanceMapper balanceMapper;

    @Resource
    private FirmOfferExchangeBalanceExt balanceExt;

    @Resource
    private FirmOfferOrderHistExt firmOfferOrderHistExt;

    @Resource
    private FirmOfferPositionExt firmOfferPositionExt;

    @Resource
    private FirmOfferPositionMapper firmOfferPositionMapper;

    @Resource
    private FirmOfferLedgerHistExt firmOfferLedgerHistExt;

    /**
     * 将账户资产进行入库更新操作
     *
     * @param key      Kafka 中蜘蛛标记的状态 eg:OKEX-BLANCE-FUTURE-DELETE-75
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
        List<FirmOfferExchangeBalance> oldBalances = balanceMapper.selectByExample(firmOfferExchangeBalanceExample);
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
        List<FirmOfferOrderHist> listFromContentList = new ArrayList<>();
        for (FirmOfferOrderHist firmOfferOrderHist : listFromContents) {
            if (firmOfferOrderHist.getOrderStatus() == null) {
                continue;
            }
            listFromContentList.add(firmOfferOrderHist);
        }
        firmOfferOrderHistExt.insertDuplicateUpdate(listFromContentList);
    }

    @Override
    public void matchList(String[] key, String contents) {

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
        listFromContents.forEach(firmOfferPosition -> {
            if ("short".equals(firmOfferPosition.getType())) {
                firmOfferPosition.setInstrumentId(firmOfferPosition.getTradingOn() + "空");
            } else {
                firmOfferPosition.setInstrumentId(firmOfferPosition.getTradingOn() + "多");
            }
        });
        FirmOfferPositionExample firmOfferPositionExample = new FirmOfferPositionExample();
        firmOfferPositionExample.or().andUserIdEqualTo(Long.valueOf(key[4]));
        firmOfferPositionMapper.deleteByExample(firmOfferPositionExample);
        if (listFromContents.isEmpty()) {
            return;
        }
        firmOfferPositionExt.insertFirmOfferPositionUpdate(listFromContents);
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
        firmOfferLedgerHistExt.insertDuplicateUpdate(getListFromContents(contents, FirmOfferLedgerHist.class));
    }

    @Override
    public BigDecimal getNowPriceByInstrumentId(String instrumentId) {
        return null;
    }
}
