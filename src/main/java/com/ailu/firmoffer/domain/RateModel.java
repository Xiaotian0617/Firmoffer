package com.ailu.firmoffer.domain;

import com.ailu.firmoffer.service.SymbolRateManager;
import com.ailu.firmoffer.util.OnlyKeysUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@Slf4j
public class RateModel {

    private SymbolRateManager symbolRateManager;

    private String exchangeName;

    private Integer coinId;

    private BigDecimal amount;

    private BigDecimal price = BigDecimal.ZERO;

    private String symbol;

    private String getOnlykey() {
        try {
           return OnlyKeysUtil.getOnlyKeyByExchange(getExchangeByName(),getSymbol());
        }catch (Exception e){
            log.error("未获取到匹配 Onlykey 数据");
            return "";
        }
    }

    public String getOnlykey(String coinName) {
        try {
            return OnlyKeysUtil.getOnlyKeyByExchange(getExchangeByName(),coinName);
        }catch (Exception e){
            log.error("未获取到匹配 Onlykey 数据");
            return "";
        }
    }

    /**
     * TODO 本方法需要在本Model中注入其他Service 否则会报空指针
     *
     * @return
     */
    public BigDecimal getPriceByOnlyKey(SymbolRateManager symbolRateManager) {
        BigDecimal usdTemp = BigDecimal.ZERO;
        BigDecimal btcTemp = BigDecimal.ZERO;
        String onlyKey = getOnlykey();
        if (onlyKey == null) {
            return BigDecimal.ZERO;
        }
        String[] keys = onlyKey.split("_");
        if (Objects.equals(keys[1], keys[2])) {
            //如果单位和市场相同，返回其量为价格即可
            log.debug(" OnlyKey 为{}的计算数量为{},计算结果为：{},当前兑换BTC汇率为：{}，兑换USDT汇率为：{}", onlyKey, amount, amount, btcTemp, usdTemp);
            return amount;
        }
        usdTemp = getRatePrice(symbolRateManager, usdTemp, onlyKey).multiply(amount);
        if (usdTemp.compareTo(BigDecimal.ZERO) != 0) {
            return usdTemp;
        }
        onlyKey = keys[0] + "_" + keys[1] + "_" + "USDT";
        usdTemp = getRatePrice(symbolRateManager, usdTemp, onlyKey).multiply(amount);
        if (usdTemp.compareTo(BigDecimal.ZERO) != 0) {
            return usdTemp;
        }
        onlyKey = keys[0] + "_" + keys[1] + "_" + "BTC";
        if ("BTC".equals(keys[1])) {
            if (amount != null) {
                btcTemp = amount;
            }
            usdTemp = btcTemp.multiply(getRatePrice(symbolRateManager, usdTemp, getOnlykey("BTC")));
        } else {
            btcTemp = getRatePrice(symbolRateManager, btcTemp, onlyKey);
            if (btcTemp == null) {
                //如果此币种连BTC都没有换算，就没有换算必要了
                return BigDecimal.ZERO;
            } else {
                usdTemp = btcTemp.multiply(getRatePrice(symbolRateManager, usdTemp, getOnlykey("BTC"))).multiply(amount);
            }
        }
        //        BigDecimal multiply = usdTemp.multiply(amount);
        BigDecimal multiply = usdTemp;
        if (multiply.compareTo(BigDecimal.ZERO) != 0) {
            log.debug(" OnlyKey 为{}的计算数量为{},计算结果为：{},当前兑换BTC汇率为：{}，兑换USDT汇率为：{}", onlyKey, amount, multiply, btcTemp, usdTemp);
        }
        return multiply;
    }

    private BigDecimal getRatePrice(SymbolRateManager symbolRateManager, BigDecimal temp, String onlyKey) {
        return OnlyKeysUtil.getRatePrice(symbolRateManager,temp,getExchangeByName(),onlyKey);
    }

    public String getExchangeByName() {
        return exchangeName;
    }

}
