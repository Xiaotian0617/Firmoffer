package com.ailu.firmoffer.exchange.apiclient;

import com.ailu.firmoffer.domain.DWHistoryQuery;
import com.ailu.firmoffer.exchange.signature.BitfinexSignature;
import com.ailu.firmoffer.util.OkHttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/19 15:13
 */
@Slf4j
public class BitfinexApiClient extends BitfinexSignature {

    static final String API_HOST = "https://api.bitfinex.com";

    final String accessKeyId;
    final String accessKeySecret;

    /**
     * @param accessKeyId
     * @param accessKeySecret
     */
    public BitfinexApiClient(String accessKeyId, String accessKeySecret) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
    }

    /**
     * 获取用户账户余额
     * 调用频率 ： 每分钟 20 次
     *
     * @return
     */
    public String getBalances() {
        String result = post("/v1/balances", null);
        log.trace("私有api : 获取用户 余额 ：{} ", result);
        return result;
    }

    /**
     * 获取用户活跃账户仓位信息
     * 调用频率 ： 每分钟 默认访问次数
     *
     * @return
     */
    public String getActivePositions() {
        String result = post("/v1/positions", null);
        log.trace("私有api : 获取用户 账户仓位信息 ：{} ", result);
        return result;
    }

    /**
     * 获取用户活跃订单信息
     * 调用频率 ： 每分钟 默认访问次数
     *
     * @return
     */
    public String getActiveOrders() {
        String result = post("/v1/orders", null);
        log.trace("私有api : 获取用户 活跃订单信息 ：{} ", result);
        return result;
    }

    /**
     * 获取用户的订单历史记录
     * 调用频率 ：限于最近3天和每分钟1个请求。
     *
     * @return
     */
    public String getOrdersHistory(int limit) {
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("limit", limit);
        String result = post("/v1/orders/hist", params);
        log.trace("私有api : 获取用户 订单历史记录 ：{} ", result);
        return result;
    }

    /**
     * 获取用户的存取款历史
     * 调用频率 ： 每分钟 20次
     *
     * @param dwQuery
     * @return
     */
    public String getDepositWithdrawalHistory(DWHistoryQuery dwQuery) {
        HashMap<String, Object> params = new HashMap<>(5);
        params.put("currency", dwQuery.getCurrency());
        params.put("method", dwQuery.getMethod());
        params.put("since", dwQuery.getSince());
        params.put("until", dwQuery.getUntil());
        params.put("limit", dwQuery.getLimit());
        String result = post("/v1/history/movements", params);
        log.trace("私有api : 获取用户 存取款历史 ：{} ", result);
        return result;
    }

    /**
     * 调用用户的订单状态
     * 默认调用时间
     *
     * @param orderId
     * @return
     */
    public String getOrderStatus(long orderId) {
        HashMap<String, Object> params = new HashMap<>(5);
        params.put("order_id", orderId);
        String result = post("/v1/order/status", params);
        log.trace("私有api : 获取用户 订单状态 ：{} ", result);
        return result;
    }


    /**
     * send a POST request
     *
     * @param uri
     * @param params
     * @return
     */
    private String post(String uri, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        return call("POST", uri, params);
    }

    /**
     * send a GET request
     *
     * @param uri
     * @param params
     * @return
     */
    private String get(String uri, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        return call("GET", uri, params);
    }

    /**
     * call api by endpoint
     *
     * @param method
     * @param uri
     * @param params
     * @return
     */
    private String call(String method, String uri, Map<String, Object> params) {
        log.trace("准备开始调用 Bitfinex API 调用方法 {} url {}", method, uri);
        try {
            Request.Builder builder;
            if (OkHttpClientUtil.REQUEST_POST.equals(method)) {
                builder = new Request.Builder().url(API_HOST + uri).post(new FormBody.Builder().build());
            } else {
                builder = new Request.Builder().url(API_HOST + uri).get();
            }

            if (params.size() < 1) {
                params = iniHeaders(this.accessKeyId, this.accessKeySecret, uri);
            } else {
                params = iniHeaders(this.accessKeyId, this.accessKeySecret, uri, params);
            }
            params.forEach((k, v) -> {
                builder.addHeader(k, String.valueOf(v));
            });
            Request request = builder.build();
            Response response = OkHttpClientUtil.client.newCall(request).execute();
            String s = response.body().string();
            return s;
        } catch (IOException e) {
            log.error("调用 Bitfinex API 调用方法 IO 错误 {} url {} ERROR {} , {}", method, uri, e, e.getMessage());
            throw new RuntimeException("调用 Bitfinex API 调用方法 IO 错误," + e.getMessage());
        } catch (Exception e) {
            log.error("调用 Bitfinex API 调用方法 错误 {} url {} ERROR {} , {}", method, uri, e, e.getMessage());
            throw new RuntimeException("调用 Bitfinex API 调用方法 IO 错误," + e.getMessage());
        }
    }

}
