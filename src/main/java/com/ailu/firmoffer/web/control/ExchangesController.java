package com.ailu.firmoffer.web.control;

import com.ailu.firmoffer.exchange.apiclient.OkexApiClients;
import com.ailu.firmoffer.web.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @Description: 返回给各个交易所的接口
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/5/4 14:51
 */
@Slf4j
@RestController
@RequestMapping(path = "/exchange", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ExchangesController {

    /**
     * 期货行情URL
     */
    private final String FUTURE_TICKER_URL = "/api/v1/future_ticker.do";
    /**
     * 期货指数查询URL
     */
    private final String FUTURE_INDEX_URL = "/api/v1/future_index.do";

    /**
     * 期货交易记录查询URL
     */
    private final String FUTURE_TRADES_URL = "/api/v1/future_trades.do";

    /**
     * 期货市场深度查询URL
     */
    private final String FUTURE_DEPTH_URL = "/api/v1/future_depth.do";
    /**
     * 美元-人民币汇率查询URL
     */
    private final String FUTURE_EXCHANGE_RATE_URL = "/api/v1/exchange_rate.do";

    /**
     * 期货取消订单URL
     */
    private final String FUTURE_CANCEL_URL = "/api/v1/future_cancel.do";

    /**
     * 期货下单URL
     */
    private final String FUTURE_TRADE_URL = "/api/v1/future_trade.do";

    /**
     * 期货账户信息URL
     */
    private final String FUTURE_USERINFO_URL = "/api/v1/future_userinfo.do";

    /**
     * 逐仓期货账户信息URL
     */
    private final String FUTURE_USERINFO_4FIX_URL = "/api/v1/future_userinfo_4fix.do";

    /**
     * 期货持仓查询URL
     */
    private final String FUTURE_POSITION_URL = "/api/v1/future_position.do";

    /**
     * 期货逐仓持仓查询URL
     */
    private final String FUTURE_POSITION_4FIX_URL = "/api/v1/future_position_4fix.do";

    /**
     * 用户期货订单信息查询URL
     */
    private final String FUTURE_ORDER_INFO_URL = "/api/v1/future_order_info.do";


    /**
     * 期货账户信息
     *
     * @param api_key
     * @param secret_key
     * @return
     */
    @PostMapping(value = "/okex/future_userinfo")
    public WebResult okexSignature(@RequestParam("apiKey") String api_key, @RequestParam("apiKeySecret") String secret_key) {
        OkexApiClients futurePostV1 = new OkexApiClients(api_key, secret_key);
        String str = null;
        try {
            str = futurePostV1.future_userinfo();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WebResult.okResult(str);
    }

}
