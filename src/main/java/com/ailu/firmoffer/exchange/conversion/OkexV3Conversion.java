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
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
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
public class OkexV3Conversion implements ExchangeConversion {

    @Resource
    private FirmOfferExchangeBalanceMapper balanceMapper;

    @Resource
    private FirmOfferExchangeBalanceExt balanceExt;

    @Resource
    private FirmOfferOrderHistExt firmOfferOrderHistExt;

    @Resource
    private FirmOfferMatchHistExt firmOfferMatchHistExt;

    @Resource
    private FirmOfferPositionExt firmOfferPositionExt;

    @Resource
    private SymbolRateManager symbolRateManager;

    @Resource
    private FirmOfferPositionMapper firmOfferPositionMapper;

    @Resource
    private ContractUtil contractUtil;

    @Resource
    private FirmOfferLedgerHistExt firmOfferLedgerHistExt;

    @Override
    public BigDecimal getNowPriceByInstrumentId(String instrumentId) {
        String[] instrumentIds = instrumentId.split("-");
        if (instrumentIds == null || instrumentIds.length != 3) {
            return BigDecimal.ZERO;
        }
        String time = instrumentIds[2];
        String contract = contractUtil.judgeContract(time);
        String onlykey;
        switch (contract) {
            case "当周合约":
                onlykey = EX_CHANGE_OKEX + "_" + instrumentIds[0] + "THISWEEK_" + instrumentIds[1];
                break;
            case "次周合约":
                onlykey = EX_CHANGE_OKEX + "_" + instrumentIds[0] + "NEXTWEEK_" + instrumentIds[1];
                break;
            case "季度合约":
                onlykey = EX_CHANGE_OKEX + "_" + instrumentIds[0] + "QUARTER_" + instrumentIds[1];
                break;
            case "永续合约":
                onlykey = EX_CHANGE_OKEX + "_" + instrumentIds[0] + "SWAP_" + instrumentIds[1];
                break;
            default:
                onlykey = EX_CHANGE_OKEX + "_" + instrumentIds[0] + "QUARTER_" + instrumentIds[1];
        }
        BigDecimal nowPrice = symbolRateManager.getOkexSymbolRate(onlykey);
        return nowPrice;
    }


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
        if (!firmOfferExchangeBalances.isEmpty()) {
            balanceExt.exchangeBalanceInsert(firmOfferExchangeBalances);
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

    /**
     * 将现货订单列表进行入库更新操作
     *
     * @param key      Kafka 中蜘蛛标记的状态
     * @param contents Kafka 接收到的蜘蛛的信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void matchList(String[] key, String contents) {
        firmOfferMatchHistExt.insertDuplicateUpdate(getListFromContents(contents, FirmOfferMatchHist.class));
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
            BigDecimal ratio = BigDecimal.ZERO;
            if (firmOfferPosition.getTradingOn().contains(Dic.SWAP_OKEX)) {
                //EOS永续合约改为“EOS永续多”这种，“合约”2字不要，加上多空，多空判断是type字段
                if ("short".equals(firmOfferPosition.getType())) {
                    firmOfferPosition.setInstrumentId(firmOfferPosition.getInstrumentId().substring(0, firmOfferPosition.getInstrumentId().length() - 2) + "空");
                } else {
                    firmOfferPosition.setInstrumentId(firmOfferPosition.getInstrumentId().substring(0, firmOfferPosition.getInstrumentId().length() - 2) + "多");
                }
                //（settlementPrice－avgCost）avgCost*leverage
                ratio = firmOfferPosition.getSettlementPrice().subtract(firmOfferPosition.getAvgCost())
                        .divide(firmOfferPosition.getAvgCost(), 8, BigDecimal.ROUND_HALF_DOWN)
                        .multiply(new BigDecimal(firmOfferPosition.getLeverage()));
            } else {
                if (Dic.LIMITSTORE.equalsIgnoreCase(firmOfferPosition.getMarginMode())) {
                    return;
                }
                BigDecimal nowPrice = getNowPriceByInstrumentId(firmOfferPosition.getTradingOn());
                if (nowPrice == null) {
                    log.warn("行情暂未初始化，本次持仓汇率暂未计算");
                    return;
                }
                //全仓的这么算吧，（现价-开仓价）/现价*杠杆倍率  如果是空单，再乘以-1
                ratio = (nowPrice.subtract(firmOfferPosition.getAvgCost()))
                        .divide(nowPrice, 6, BigDecimal.ROUND_HALF_DOWN)
                        .multiply(new BigDecimal(firmOfferPosition.getLeverage())
                                .multiply(new BigDecimal(100)).setScale(4, BigDecimal.ROUND_HALF_DOWN));
            }
            firmOfferPosition.setPnlRatio(firmOfferPosition.getType().equalsIgnoreCase(Dic.SHORT) ? ratio.multiply(new BigDecimal("-1")) : ratio);
        });
        FirmOfferPositionExample firmOfferPositionExample = new FirmOfferPositionExample();
        FirmOfferPositionExample.Criteria or = firmOfferPositionExample.or();
        or.andUserIdEqualTo(Long.valueOf(key[4]));
        if ("SWAP".equals(key[2])) {
            or.andTradingOnLike("%-SWAP");
            List<FirmOfferPosition> firmOfferPositions = firmOfferPositionMapper.selectByExample(firmOfferPositionExample);
            String collect = firmOfferPositions.stream().map(o -> o.getId().toString()).collect(Collectors.joining("','", "'", "'"));
            firmOfferPositionExt.delById(collect);
            if (listFromContents.isEmpty()) {
                return;
            }
            firmOfferPositionExt.insertFirmOfferPositionUpdate(listFromContents);
        } else {
            or.andTradingOnNotLike("%-SWAP");
            List<FirmOfferPosition> firmOfferPositions = firmOfferPositionMapper.selectByExample(firmOfferPositionExample);
            String s = firmOfferPositions.stream().map(o -> o.getId().toString()).collect(Collectors.joining("','", "'", "'"));
            firmOfferPositionExt.delById(s);
            if (listFromContents.isEmpty()) {
                return;
            }
            firmOfferPositionExt.insertFirmOfferPositionUpdate(listFromContents);
        }
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
        if (listFromContents.isEmpty()) {
            return;
        }
        firmOfferLedgerHistExt.insertDuplicateUpdate(listFromContents);
    }
}
