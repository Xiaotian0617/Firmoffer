package com.ailu.firmoffer.task;

import com.ailu.firmoffer.domain.PendingObj;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/26 14:39
 */
public class Pending {
    /**
     * bitfinex历史订单
     **/
    public static CopyOnWriteArrayList<PendingObj> bitfinexOrderHist = new CopyOnWriteArrayList<>();
    /**
     * bitfinex活跃订单
     **/
    public static CopyOnWriteArrayList<PendingObj> bitfinexActiveHist = new CopyOnWriteArrayList<>();
    /**
     * bitfinex充值提现
     **/
    public static CopyOnWriteArrayList<PendingObj> bitfinDWH = new CopyOnWriteArrayList<>();

    /**
     * huobi订单
     **/
    public static Set<PendingObj> huobiOrderHist = new HashSet<>();

    /**
     * huobi订单
     **/
    public static Set<PendingObj> huobiMatchHist = new HashSet<>();

    /**
     * huobi订单转Map
     */
    public static Map<Long, PendingObj> huobiPendingObjMap = new HashMap<>();

    /**
     * huobi充值提现
     **/
    public static Set<PendingObj> huobiDWH = new HashSet<>();

    /**
     * okex历史订单
     **/
    public static Set<PendingObj> okexOrderHist = new HashSet<>();
    /**
     * okex活跃订单
     **/
    public static Set<PendingObj> okexActiveHist = new HashSet<>();
    /**
     * okex充值提现
     **/
    public static Set<PendingObj> okexDWH = new HashSet<>();

    /**
     * binance订单
     **/
    public static Set<PendingObj> binanceOrderHist = new HashSet<>();
    /**
     * binance充值提现
     **/
    public static Set<PendingObj> binanceDWH = new HashSet<>();

    /**
     * bibox历史订单
     **/
    public static Set<PendingObj> biboxOrderHist = new HashSet<>();
    /**
     * bibox活跃订单
     **/
    public static Set<PendingObj> biboxActiveHist = new HashSet<>();

}
