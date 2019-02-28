package com.ailu.firmoffer.exchange.apiclient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.okcoin.okex.open.api.bean.account.result.Wallet;
import com.okcoin.okex.open.api.bean.spot.result.Account;
import com.okcoin.okex.open.api.bean.spot.result.Ledger;
import com.okcoin.okex.open.api.bean.spot.result.OrderInfo;
import com.okcoin.okex.open.api.config.APIConfiguration;
import com.okcoin.okex.open.api.enums.I18nEnum;
import com.okcoin.okex.open.api.service.account.AccountAPIService;
import com.okcoin.okex.open.api.service.account.impl.AccountAPIServiceImpl;
import com.okcoin.okex.open.api.service.futures.FuturesTradeAPIService;
import com.okcoin.okex.open.api.service.futures.impl.FuturesTradeAPIServiceImpl;
import com.okcoin.okex.open.api.service.spot.SpotAccountAPIService;
import com.okcoin.okex.open.api.service.spot.SpotOrderAPIServive;
import com.okcoin.okex.open.api.service.spot.impl.SpotAccountAPIServiceImpl;
import com.okcoin.okex.open.api.service.spot.impl.SpotOrderApiServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * NOTE:
 * Okex v3 版本API接口
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/11/9 15:04
 */
@Slf4j
@Component
public class OkexV3ApiClient {

    private APIConfiguration apiConfiguration;

    private SpotAccountAPIService spotAccountAPIService;

    private SpotOrderAPIServive spotOrderAPIServive;

    private FuturesTradeAPIService futuresTradeAPIService;

    private AccountAPIService accountAPIService;

    public OkexV3ApiClient() {
    }

    public OkexV3ApiClient(String apiKey, String secretKey, String passphrase, boolean proxyEnable, String url, String port) {
        this.apiConfiguration = config(apiKey, secretKey, passphrase, proxyEnable, url, port);
        this.spotAccountAPIService = new SpotAccountAPIServiceImpl(this.apiConfiguration);
        this.futuresTradeAPIService = new FuturesTradeAPIServiceImpl(this.apiConfiguration);
        this.spotOrderAPIServive = new SpotOrderApiServiceImpl(this.apiConfiguration);
        this.accountAPIService = new AccountAPIServiceImpl(this.apiConfiguration);
    }

    public APIConfiguration config(String apiKey, String secretKey, String passphrase, boolean proxyEnable, String url, String port) {
        final APIConfiguration config = new APIConfiguration();
        config.setEndpoint("https://www.okex.com/");
        // apiKey，api注册成功后页面上有
        config.setApiKey(apiKey);
        // secretKey，api注册成功后页面上有
        config.setSecretKey(secretKey);
        config.setPassphrase(passphrase);
        config.setPrint(true);
        config.setI18n(I18nEnum.SIMPLIFIED_CHINESE);
        if (proxyEnable) {
            log.info("Okex 启动代理模式！");
            config.setEnableProxy(true);
            config.setProxyUrl(url);
            config.setProxyPort(Integer.valueOf(port));
        }
        return config;
    }

    /**
     * 获取用户spot账户信息
     */
    public List<Account> getSpotAccounts() {
        final List<Account> accounts = spotAccountAPIService.getAccounts();
        toResultString("spotAccounts", accounts);
        return accounts;
    }

    /**
     * 获取合约账户信息
     *
     * @return
     */
    public JSONObject getFutureAccounts() {
        JSONObject accounts = futuresTradeAPIService.getAccounts();
        toResultString("futureAccounts", accounts);
        return accounts;
    }

    /**
     * 获取现货订单列表
     *
     * @param product
     * @return
     */
    public List<OrderInfo> getSpotOrders(String product) {
        try {
            final List<OrderInfo> orderInfoList = spotOrderAPIServive.getOrders(product, "all", null, null, "100");
            this.toResultString("orderInfoList", orderInfoList);
            return orderInfoList;
        } catch (Exception e) {
            log.error("获取现货订单列表出错！", e);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 获取期货订单列表
     *
     * @param instrumentId 合约ID，如BTC-USD-180213
     * @return
     */
    public JSONObject getFutureOrders(String instrumentId, int status) {
        try {
            JSONObject result = futuresTradeAPIService.getOrders(instrumentId, status, 0, 0, 100);
            toResultString("Get-Orders", result);
            return result;
        } catch (Exception e) {
            log.error("获取期货订单列表出错！", e);
        }
        return new JSONObject();

    }

    /**
     * 获取现货账单流水
     */
    public List<Ledger> getLedgersByCurrency(String symbol) {
        try {
            final List<Ledger> ledgers = this.spotAccountAPIService.getLedgersByCurrency(symbol, null, null, "100");
            this.toResultString("ledges", ledgers);
            return ledgers;
        } catch (Exception e) {
            log.error("获取现货账单流水出错！", e);
        }
        return Collections.EMPTY_LIST;

    }

    /**
     * 获取期货账单流水
     *
     * @param symbol
     * @return
     */
    public JSONArray getAccountsLedgerByCurrency(String symbol) {
        try {
            JSONArray ledger = futuresTradeAPIService.getAccountsLedgerByCurrency(symbol);
            toResultString("Ledger", ledger);
            return ledger;
        } catch (Exception e) {
            log.error("获取期货账单流水出错！", e);
        }
        return new JSONArray();
    }

    public List<Wallet> getWallet() {
        try {
            List<Wallet> result = this.accountAPIService.getWallet();
            this.toResultString("wallet", result);
            return result;
        } catch (Exception e) {
            log.error("获取钱包账户出错！", e);
        }
        return Collections.EMPTY_LIST;
    }

    public void toResultString(String flag, Object object) {
        if (log.isDebugEnabled()) {
            StringBuilder su = new StringBuilder();
            su.append("\n").append("<*> ").append(flag).append(": ").append(JSON.toJSONString(object));
            log.debug(su.toString());
        }
    }

    public JSONObject getFuturePositions() {
        JSONObject positions = futuresTradeAPIService.getPositions();
        this.toResultString("futurePosition", positions);
        return positions;
    }
}
