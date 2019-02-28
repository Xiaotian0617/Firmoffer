package com.ailu.firmoffer.web.vo;

import com.ailu.firmoffer.service.SymbolRateManager;
import com.ailu.firmoffer.util.OnlyKeysUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@Slf4j
public class UserAssetRatioVo {

    private Integer coin;

    private String symbol;

    private String exchange;

    private BigDecimal amount;//原数量

    private BigDecimal price;//计算价格

    private BigDecimal ratio;//比率

    private String getOnlykey() {
        try {
          return OnlyKeysUtil.getOnlyKeyByExchange(getExchange(),getSymbol());
        } catch (Throwable e) {
            log.warn("CoinContrastManager 未准备就绪或未找到此币种,{},{}", symbol, getExchangeByName());
        }
        return "";
    }

    public String getOnlykey(String coinName) {
        return OnlyKeysUtil.getOnlyKeyByExchange(getExchange(),coinName);
    }

    /**
     * TODO 本方法需要在本Model中注入其他Service 否则会报空指针
     * NOTE:
     * 1.先将所有币种换算成BTC
     * 2.然后拿BTC换算USDT或USD
     * 3.最后相乘得出价格
     *
     * @return
     */
    public void setPriceByCurrPrice(SymbolRateManager symbolRateManager) {
        BigDecimal usdTemp = BigDecimal.ZERO;
        BigDecimal btcTemp = BigDecimal.ZERO;
        String onlyKey = getOnlykey();
        if (onlyKey == null) {
            price = BigDecimal.ZERO;
            return;
        }
        String[] keys = onlyKey.split("_");
        if (Objects.equals(keys[1], keys[2])) {
            //如果单位和市场相同，返回其量为价格即可
            price = amount;
            log.debug("OnlyKey 为{}的计算结果为：{},当前兑换BTC汇率为：{}，兑换USDT汇率为：{}", onlyKey, price, btcTemp, usdTemp);
            return;
        }
        usdTemp = getRatePrice(symbolRateManager, usdTemp, onlyKey).multiply(amount);
        if (usdTemp.compareTo(BigDecimal.ZERO) != 0) {
            price = usdTemp;
            return;
        }
        onlyKey = keys[0] + "_" + keys[1] + "_" + "USDT";
        usdTemp = getRatePrice(symbolRateManager, usdTemp, onlyKey).multiply(amount);
        if (usdTemp.compareTo(BigDecimal.ZERO) != 0) {
            price = usdTemp;
            return;
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
                price = BigDecimal.ZERO;
            } else {
                usdTemp = btcTemp.multiply(getRatePrice(symbolRateManager, usdTemp, getOnlykey("BTC"))).multiply(amount);
            }
        }
        //        BigDecimal multiply = usdTemp.multiply(amount);
        BigDecimal multiply = usdTemp;
        if (multiply.compareTo(BigDecimal.ZERO) != 0) {
            log.debug(" OnlyKey 为{}的计算数量为{},计算结果为：{},当前兑换BTC汇率为：{}，兑换USDT汇率为：{}", onlyKey, amount, multiply, btcTemp, usdTemp);
        }
        price = multiply;
    }

    private BigDecimal getRatePrice(SymbolRateManager symbolRateManager, BigDecimal temp, String onlyKey) {
       return OnlyKeysUtil.getRatePrice(symbolRateManager,temp,getExchange(),onlyKey);
    }

    public String getExchangeByName() {
        return exchange;
    }
}
