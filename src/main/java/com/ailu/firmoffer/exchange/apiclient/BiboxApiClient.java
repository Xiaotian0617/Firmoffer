package com.ailu.firmoffer.exchange.apiclient;

import com.ailu.firmoffer.domain.BiboxOrderQuery;
import com.ailu.firmoffer.exchange.signature.BiboxSignature;
import com.ailu.firmoffer.util.OkHttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/5/4 11:08
 */
@Slf4j
public class BiboxApiClient extends BiboxSignature {

    static final String API_HOST = "https://api.bibox.com";
    static final String APIKEY_PARAMS_STRING = "apikey";
    static final String SIGN_PARAMS_STRING = "sign";
    static final String CMDS_PARAMS_STRING = "cmds";

    final String accessKeyId;
    final String accessKeySecret;

    /**
     * @param accessKeyId
     * @param accessKeySecret
     */
    public BiboxApiClient(String accessKeyId, String accessKeySecret) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
    }

    /**
     * 获取用户余额
     *
     * @return
     */
    public String getDetailBalance() {
        HashMap map = new HashMap(1);
        map.put("select", "1");
        return post("/v1/transfer", "transfer/assets", map);
    }

    /**
     * 获取用户历史订单
     *
     * @param query
     * @return
     */
    public String getOrderHistory(BiboxOrderQuery query) {
        HashMap map = new HashMap(8);
        map.put("pair", query.getPair());
        map.put("account_type", query.getAccountType());
        map.put("page", query.getPage());
        map.put("size", query.getSize());
        map.put("coin_symbol", query.getCoinSymbol());
        map.put("currency_symbol", query.getCurrencySymbol());
        map.put("order_side", query.getOrderSide());
        map.put("hide_cancel", query.getHideCancel());
        return post("/v1/orderpending", "orderpending/pendingHistoryList", map);
    }

    /**
     * @param query
     * @return
     */
    public String getOrderActive(BiboxOrderQuery query) {
        HashMap map = new HashMap(7);
        map.put("pair", query.getPair());
        map.put("account_type", query.getAccountType());
        map.put("page", query.getPage());
        map.put("size", query.getSize());
        map.put("coin_symbol", query.getCoinSymbol());
        map.put("currency_symbol", query.getCurrencySymbol());
        map.put("order_side", query.getOrderSide());
        map.put("hide_cancel", query.getHideCancel());
        return post("/v1/orderpending", "orderpending/orderPendingList", map);
    }

    public String getDepositWithdrawalHistory(String id) {
        HashMap map = new HashMap(1);
        map.put("id", id);
        return post("/v1/transfer", "transfer/withdrawInfo", map);
    }

    public String getTestApi() {
        return post("/v1/user", "user/userInfo", new HashMap<>(1));
    }

    /**
     * send a POST request
     *
     * @param uri
     * @param params
     * @return
     */
    private String post(String uri, String cmd, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        return call("POST", uri, cmd, params);
    }

    /**
     * send a GET request
     *
     * @param uri
     * @param params
     * @return
     */
    private String get(String uri, String cmd, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        return call("GET", uri, cmd, params);
    }


    /**
     * call api by endpoint
     *
     * @param method
     * @param uri
     * @param params
     * @return
     */
    private String call(String method, String uri, String cmd, Map<String, Object> params) {
        log.trace("准备开始调用 Bibox API 调用方法 {} url {}", method, uri);
        String cmds = getCmdsString(cmd, params);
        String sign = null;
        try {
            sign = getSignString(this.accessKeySecret, cmds);
        } catch (Exception e) {
            log.error("Bibox API 生成密钥出错 {} {}", e, e.getMessage());
        }
        try {
            Request.Builder builder;
            if (OkHttpClientUtil.REQUEST_POST.equals(method)) {
                RequestBody body = new FormBody.Builder()
                        .add(APIKEY_PARAMS_STRING, this.accessKeyId)
                        .add(SIGN_PARAMS_STRING, sign)
                        .add(CMDS_PARAMS_STRING, cmds)
                        .build();
                builder = new Request.Builder().url(API_HOST + uri).post(body);
            } else {
                builder = new Request.Builder().url(API_HOST + uri).get();
            }
            Request request = builder.build();
            Response response = OkHttpClientUtil.client.newCall(request).execute();
            String s = response.body().string();
            return s;
        } catch (IOException e) {
            log.error("调用 Bibox API 调用方法 IO 错误 {} url {} ERROR {} , {}", method, uri, e, e.getMessage());
            throw new RuntimeException("调用 Bibox API 调用方法 IO 错误," + e.getMessage());
        } catch (Exception e) {
            log.error("调用 Bibox API 调用方法 错误 {} url {} ERROR {} , {}", method, uri, e, e.getMessage());
            throw new RuntimeException("调用 Bibox API 调用方法 IO 错误," + e.getMessage());
        }
    }

}
