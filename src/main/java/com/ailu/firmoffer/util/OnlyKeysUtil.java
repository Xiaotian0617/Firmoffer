package com.ailu.firmoffer.util;

import com.ailu.firmoffer.config.ExchangeName;
import com.ailu.firmoffer.config.UnitName;
import com.ailu.firmoffer.domain.CoinContrasts;
import com.ailu.firmoffer.service.CoinContrastManager;
import com.ailu.firmoffer.service.SymbolRateManager;

import java.math.BigDecimal;

import static com.ailu.firmoffer.config.ExchangeName.EX_CHANGE_BITMEX;
import static com.ailu.firmoffer.service.MetaService.*;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company Henan ailu
 * @Date 2018/12/4 17:59
 */
public class OnlyKeysUtil {

    public static String getOnlyKeyByExchange(String exchange,String symbol) {
        if (EX_CHANGE_BITFINEX.equals(exchange)) {
            return exchange + "_" + CoinContrastManager.bitfinexContrasts.getOrDefault(symbol.toUpperCase(), new CoinContrasts()).getSymbol() + "_" + UnitName.UNIT_FOR_BITFINEX;
        } else if (EX_CHANGE_HUOBI.equals(exchange)) {
            return exchange + "_" + CoinContrastManager.huobiContrasts.getOrDefault(symbol.toUpperCase(), new CoinContrasts()).getSymbol() + "_" + UnitName.UNIT_FOR_HUOBI;
        } else if (EX_CHANGE_BIBOX.equals(exchange)) {
            return exchange + "_" + CoinContrastManager.biboxContrasts.getOrDefault(symbol.toUpperCase(), new CoinContrasts()).getSymbol() + "_" + UnitName.UNIT_FOR_BIBOX;
        } else if (EX_CHANGE_BINANCE.equals(exchange)) {
            return exchange + "_" + CoinContrastManager.binanceContrasts.getOrDefault(symbol.toUpperCase(), new CoinContrasts()).getSymbol() + "_" + UnitName.UNIT_FOR_BINANCE;
        } else if (EX_CHANGE_OKEX.equals(exchange)) {
            return exchange + "_" + CoinContrastManager.okexContrasts.getOrDefault(symbol.toUpperCase(), new CoinContrasts()).getSymbol() + "_" + UnitName.UNIT_FOR_OKEX;
        } else if (EX_CHANGE_BITMEX.equals(exchange)) {
            return exchange + "_" + CoinContrastManager.bitmexContrasts.getOrDefault(symbol.toUpperCase(), new CoinContrasts()).getSymbol() + "_" + UnitName.UNIT_FOR_BITMEX;
        } else if ("Bybit".equals(exchange)) {
            return EX_CHANGE_BITFINEX + "_" + CoinContrastManager.bitfinexContrasts.getOrDefault(symbol.toUpperCase(), new CoinContrasts()).getSymbol() + "_" + UnitName.UNIT_FOR_BITFINEX;
        }
        return "";
    }

    public static BigDecimal getRatePrice(SymbolRateManager symbolRateManager, BigDecimal temp, String exchange,String onlyKey) {
        if (exchange.equals(ExchangeName.EX_CHANGE_BITFINEX)) {
            temp = symbolRateManager.getBitfinexSymbolRate(onlyKey);
        } else if (exchange.equals(ExchangeName.EX_CHANGE_HUOBI)) {
            temp = symbolRateManager.getHuobiSymbolRate(onlyKey);
        } else if (exchange.equals(ExchangeName.EX_CHANGE_OKEX)) {
            temp = symbolRateManager.getOkexSymbolRate(onlyKey);
        } else if (exchange.equals(ExchangeName.EX_CHANGE_BINANCE)) {
            temp = symbolRateManager.getBinanceSymbolRate(onlyKey);
        } else if (exchange.equals(ExchangeName.EX_CHANGE_BIBOX)) {
            temp = symbolRateManager.getBiboxSymbolRate(onlyKey);
        } else if (exchange.equals(EX_CHANGE_BITMEX)) {
            temp = symbolRateManager.getBitmexSymbolRate(onlyKey);
        }else if (exchange.equals("Bybit")) {
            temp = symbolRateManager.getBitfinexSymbolRate(onlyKey);
        }
        if (temp == null) {
            return BigDecimal.ZERO;
        }
        return temp;
    }

}
