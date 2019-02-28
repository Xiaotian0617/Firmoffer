package com.ailu.firmoffer.task;

import com.ailu.firmoffer.dao.bean.*;
import com.ailu.firmoffer.dao.mapper.*;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferMatchHistExt;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferOrderHistExt;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferPushHistExt;
import com.ailu.firmoffer.dao.mapper.ext.UserMapperExt;
import com.ailu.firmoffer.service.MetaService;
import com.ailu.firmoffer.util.ContractUtil;
import com.ailu.firmoffer.util.Dic;
import com.ailu.firmoffer.util.SendKafkaUtils;
import com.ailu.firmoffer.vo.UserActionsPushVo;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 实盘历史操作推送.
 *
 * @author ch.wang
 */
@Slf4j
@Component
public class TradeHistoryPushTask {

    @Resource
    private FirmOfferPushHistExt firmOfferPushHistExt;

    @Resource
    private FirmOfferMatchHistMapper firmOfferMatchHistMapper;

    @Resource
    private FirmOfferOrderHistMapper firmOfferOrderHistMapper;

    @Autowired
    private SendKafkaUtils sendKafkaUtils;

    @Autowired
    private MetaService metaService;

    @Autowired
    private ContractUtil contractUtil;

    @Resource
    FirmOfferMatchHistExt firmOfferMatchHistExt;

    @Resource
    FirmOfferOrderHistExt firmOfferOrderHistExt;

    @Resource
    FirmOfferKeyMapper firmOfferKeyMapper;

    @Resource
    FirmOfferPushHistMapper firmOfferPushHistMapper;

    @Resource
    UserFirmOfferMapper userFirmOfferMapper;

    @Resource
    UserMapperExt userMapperExt;

    private final static List<Map<String, Integer>> showUserMap = new CopyOnWriteArrayList<>();

    @Scheduled(cron = "0/10 * * * * ? ")
    private void lodShowUserMap() {
        List<Map<String, Integer>> showUserId = userMapperExt.getShowUserId();
        showUserMap.clear();
        showUserMap.addAll(showUserId);
    }

    public Integer getshowUserMap(Integer key) {
        Integer res = 0;
        for (Map<String, Integer> map : showUserMap) {
            if (BigInteger.valueOf(key.longValue()).equals(map.get("id"))) {
                res = map.get("show_history");
                break;
            }
        }
        return res;
    }


    @Scheduled(cron = "0/30 * * * * ? ")
    public void ComputerTradeHistory() {
        List<FirmOfferMatchHist> allUserId = firmOfferMatchHistExt.getAllUserId();
        for (FirmOfferMatchHist firmOfferMatchHist : allUserId) {
            if (getshowUserMap(firmOfferMatchHist.getUserId().intValue()) == 1) {
                continue;
            }
            if ("Huobi".equals(firmOfferMatchHist.getExChange()) || "Binance".equals(firmOfferMatchHist.getExChange())) {
                List<FirmOfferMatchHist> firmOfferMatchHistList = getFirmOfferMatchHists(firmOfferMatchHist.getUserId());
                if (firmOfferMatchHistList == null || firmOfferMatchHistList.size() == 0) {
                    log.info("未获取到Huobi实盘现货五分钟内的操作数据！");
                    continue;
                }
                if (metaService.getRestartMap("huobiMatch") != null && !"".equals(metaService.getRestartMap("huobiMatch"))) {
                    computerFirmOfferPushHist(firmOfferMatchHistList, firmOfferMatchHist.getUserId(), metaService.getUserMap(firmOfferMatchHist.getUserId()));
                } else {
                    for (FirmOfferMatchHist offerMatchHist : firmOfferMatchHistList) {
                        offerMatchHist.setPushStates(1);
                        firmOfferMatchHistMapper.updateByPrimaryKeySelective(offerMatchHist);
                    }
                }
            }
        }
        metaService.putRestartMap("huobiMatch", "1");
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void ComputerTradeHistoryForOkex() {
        List<FirmOfferMatchHist> allUserId = firmOfferMatchHistExt.getAllUserId();
        for (FirmOfferMatchHist firmOfferMatchHist : allUserId) {
            if (getshowUserMap(firmOfferMatchHist.getUserId().intValue()) == 1) {
                continue;
            }
            if ("Okex".equals(firmOfferMatchHist.getExChange())) {
                List<FirmOfferMatchHist> firmOfferMatchHistList = getFirmOfferMatchHistsForOkex(firmOfferMatchHist.getUserId());
                if (firmOfferMatchHistList == null || firmOfferMatchHistList.size() == 0) {
                    log.info("未获取到OKEX实盘现货三分钟内的操作数据！");
                    continue;
                }
                if (metaService.getRestartMap("okexMatch") != null && !"".equals(metaService.getRestartMap("okexMatch"))) {
                    computerFirmOfferPushHistForOkex(firmOfferMatchHistList, firmOfferMatchHist.getUserId(), metaService.getUserMap(firmOfferMatchHist.getUserId()));
                } else {
                    for (FirmOfferMatchHist offerMatchHist : firmOfferMatchHistList) {
                        offerMatchHist.setPushStates(1);
                        firmOfferMatchHistMapper.updateByPrimaryKeySelective(offerMatchHist);
                    }
                }
            }
            metaService.putRestartMap("okexMatch", "1");
        }
    }

    @Scheduled(cron = "0/5 * * * * ? ")
    public void ComputerFuturesFirm() {
        List<Long> allUserId = firmOfferOrderHistExt.getAllUserId();
        for (Long userId : allUserId) {
            if (getshowUserMap(userId.intValue()) == 1) {
                continue;
            }
            List<FirmOfferOrderHist> futuresFirm = getFuturesFirm(userId);
            if (futuresFirm == null || futuresFirm.size() == 0) {
                log.info("未获取到实盘期货五分钟内的操作数据！");
                continue;
            }
            if (metaService.getRestartMap("okexOrder") != null && !"".equals(metaService.getRestartMap("okexOrder"))) {
                computerFuturesFirm(futuresFirm, userId, metaService.getUserMap(userId));
            } else {
                for (FirmOfferOrderHist offerOrderHist : futuresFirm) {
                    offerOrderHist.setPushStates(1);
                    firmOfferOrderHistMapper.updateByPrimaryKeySelective(offerOrderHist);
                }
            }
        }
        metaService.putRestartMap("okexOrder", "1");
    }

    @Scheduled(cron = "0 0 0 * * ? ")
    public void updateUserNumberOfDays() {
        FirmOfferKeyExample firmOfferKeyExample = new FirmOfferKeyExample();
        List<FirmOfferKey> firmOfferKeys = firmOfferKeyMapper.selectByExample(firmOfferKeyExample);
        for (FirmOfferKey firmOfferKey : firmOfferKeys) {
            firmOfferKey.setNumberofdays(firmOfferKey.getNumberofdays() + 1);
            firmOfferKeyMapper.updateByPrimaryKey(firmOfferKey);
        }
    }

    /**
     * 定时更新逐笔成交订单
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void changeNoDealOrders() {
        //获取未完全成交的订单
        List<FirmOfferPushHist> noDealOrders = getNoDealOrders();
        if (noDealOrders == null || noDealOrders.size() < 1) {
            return;
        }
        List<String> ids = noDealOrders.stream().map(o -> o.getOrderId() + "").distinct().collect(Collectors.toList());
        FirmOfferOrderHistExample firmOfferOrderHistExample = new FirmOfferOrderHistExample();
        FirmOfferOrderHistExample.Criteria or = firmOfferOrderHistExample.or();
        or.andIdIn(ids);
        //or.andFieldAmountGreaterThan(new BigDecimal(0));
        List<FirmOfferOrderHist> firmOfferOrderHists = firmOfferOrderHistMapper.selectByExample(firmOfferOrderHistExample);
        if (firmOfferOrderHists == null || firmOfferOrderHists.size() < 1) {
            return;
        }
        List<FirmOfferOrderHist> lst = noDealOrders.stream().flatMap(
                o -> firmOfferOrderHists.stream().filter(x ->
                        o.getOrderId().equals(x.getId()) &&
                                o.getAmount().compareTo(x.getFieldAmount()) == -1 &&
                                o.getExchange().equals(x.getExChange())))
                .collect(Collectors.toList());
        List<FirmOfferPushHist> firmOfferPushHistList = new ArrayList<>();
        if (lst != null && lst.size() > 0) {
            for (FirmOfferOrderHist offerOrderHist : lst) {
                if ("".equals(offerOrderHist.getType()) || offerOrderHist.getType() == null) {
                    continue;
                }
                FirmOfferPushHist firmOfferPushHist = new FirmOfferPushHist();
                firmOfferPushHist.setAmount(offerOrderHist.getFieldAmount());
                firmOfferPushHist.setPrice(offerOrderHist.getFieldPrice());
                firmOfferPushHist.setOrderStatus(offerOrderHist.getOrderStatus());
                String s;
                if ("Bitmex".equals(offerOrderHist.getExChange())) {
                    s = "";
                } else {
                    s = contractUtil.judgeContract(offerOrderHist.getTradingOn().substring(offerOrderHist.getTradingOn().length() - 6));
                }
                firmOfferPushHist.setSymbol(StringUtils.substringBefore(offerOrderHist.getTradingOn(), "-") + s);
                Date date = new Date();
                date.setTime(System.currentTimeMillis());
                firmOfferPushHist.setUtime(date);
                firmOfferPushHist.setTime(offerOrderHist.getOrderDate());
                firmOfferPushHist.setExchange(offerOrderHist.getExChange());
                FirmOfferPushHist firmOfferPushHist1 = computerOrderType(offerOrderHist);
                if (firmOfferPushHist1 == null) {
                    continue;
                }
                firmOfferPushHist.setType(firmOfferPushHist1.getType());
                firmOfferPushHist.setOrderType(firmOfferPushHist1.getOrderType());
                firmOfferPushHist.setUserId(Integer.parseInt(offerOrderHist.getUserId().toString()));
                firmOfferPushHist.setShowStatus(0);
                firmOfferPushHist.setUserName(getUserNameByUserId(offerOrderHist.getUserId()).getNickName());
                firmOfferPushHist.setOrderId(offerOrderHist.getId());
                firmOfferPushHist.setOrderStatus(offerOrderHist.getOrderStatus());
                firmOfferPushHistList.add(firmOfferPushHist);
            }
        } else {
            return;
        }
        if (firmOfferPushHistList != null && firmOfferPushHistList.size() > 0) {
            pushFirmOffer(firmOfferPushHistList, 1);
        }
    }

    public UserFirmOffer getUserNameByUserId(Long userId) {
        return userFirmOfferMapper.selectByPrimaryKey(userId);
    }

    private void computerFirmOfferPushHistForOkex(List<FirmOfferMatchHist> futuresFirm, Long
            userId, UserFirmOffer userFirmOffer) {
        List<FirmOfferPushHist> histList = new ArrayList<>();
        for (FirmOfferMatchHist firmOfferMatchHist : futuresFirm) {
            FirmOfferPushHist hist = new FirmOfferPushHist();
            hist.setSymbol(firmOfferMatchHist.getSymbol());
            Date date = new Date();
            hist.setTime(firmOfferMatchHist.getMatchDate());
            date.setTime(System.currentTimeMillis());
            hist.setUtime(date);
            hist.setExchange(firmOfferMatchHist.getExChange());
            hist.setPrice(firmOfferMatchHist.getFieldPrice());
            hist.setAmount(firmOfferMatchHist.getFieldAmount());
            if ("buy-limit".equals(firmOfferMatchHist.getType())) {
                if (Dic.STOCK.equals(firmOfferMatchHist.getMatchType())) {
                    hist.setType("buy");
                } else {
                    hist.setType(Dic.MARGIN + "Buy");
                }
            } else {
                if (Dic.STOCK.equals(firmOfferMatchHist.getMatchType())) {
                    hist.setType("sell");
                } else {
                    hist.setType(Dic.MARGIN + "Sell");
                }
            }
            hist.setUserId(Integer.parseInt(userId.toString()));
            hist.setUserName(userFirmOffer.getNickName());
            hist.setShowStatus(0);
            hist.setOrderId(firmOfferMatchHist.getId().toString());
            FirmOfferMatchHist offerMatchHist = new FirmOfferMatchHist();
            offerMatchHist.setId(firmOfferMatchHist.getId());
            offerMatchHist.setPushStates(1);
            firmOfferMatchHistMapper.updateByPrimaryKeySelective(offerMatchHist);
            histList.add(hist);
        }
        if (histList != null && histList.size() > 0) {
            pushFirmOffer(histList, 0);
        }
    }

    private void computerFuturesFirm(List<FirmOfferOrderHist> futuresFirm, Long userId, UserFirmOffer
            userFirmOffer) {
        List<FirmOfferPushHist> histList = new ArrayList<>();
        for (FirmOfferOrderHist firmOfferOrderHist : futuresFirm) {
            try {
                if (firmOfferOrderHist.getFieldAmount().compareTo(new BigDecimal(0)) == 1) {
                    FirmOfferPushHist firmOfferPushHist = new FirmOfferPushHist();
                    if ("Okex".equals(firmOfferOrderHist.getExChange())) {
                        String substring = firmOfferOrderHist.getTradingOn().substring(firmOfferOrderHist.getTradingOn().lastIndexOf("-") + 1);
                        String s;
                        if ("SWAP".equals(substring)) {
                            s = "永续合约";
                        } else {
                            s = contractUtil.judgeContract(substring);
                        }
                        firmOfferPushHist.setSymbol(StringUtils.substringBefore(firmOfferOrderHist.getTradingOn(), "-") + s);
                    } else {
                        firmOfferPushHist.setSymbol(firmOfferOrderHist.getTradingOn());
                    }
                    Date date = new Date();
                    firmOfferPushHist.setTime(firmOfferOrderHist.getUtime());
                    date.setTime(System.currentTimeMillis());
                    firmOfferPushHist.setUtime(date);
                    firmOfferPushHist.setExchange(firmOfferOrderHist.getExChange());
                    firmOfferPushHist.setPrice(firmOfferOrderHist.getFieldPrice());
                    if (!"Bitmex".equals(firmOfferOrderHist.getExChange()) && !"Bybit".equals(firmOfferOrderHist.getExChange())) {
                        //订单类型 期货的话 1：开多 2：开空 3：平多 4： 平空
                        FirmOfferPushHist firmOfferPushHist1 = computerOrderType(firmOfferOrderHist);
                        if (firmOfferPushHist1 == null) {
                            continue;
                        }
                        firmOfferPushHist.setType(firmOfferPushHist1.getType());
                        firmOfferPushHist.setOrderType(firmOfferPushHist1.getOrderType());
                    } else {
                        firmOfferPushHist.setType(firmOfferOrderHist.getSide().toLowerCase());
                        firmOfferPushHist.setOrderType("6");
                    }
                    firmOfferPushHist.setUserId(Integer.parseInt(userId.toString()));
                    firmOfferPushHist.setUserName(userFirmOffer.getNickName());
                    firmOfferPushHist.setAmount(firmOfferOrderHist.getFieldAmount());
                    firmOfferPushHist.setOrderId(firmOfferOrderHist.getId());
                    firmOfferPushHist.setOrderStatus(firmOfferOrderHist.getOrderStatus());
                    firmOfferPushHist.setShowStatus(0);
                    FirmOfferOrderHist offerOrderHist = new FirmOfferOrderHist();
                    offerOrderHist.setPushStates(1);
                    offerOrderHist.setId(firmOfferOrderHist.getId());
                    firmOfferOrderHistMapper.updateByPrimaryKeySelective(offerOrderHist);
                    histList.add(firmOfferPushHist);
                }
            } catch (Exception e) {
                log.error("计算合约数据异常，请检查:", e);
                continue;
            }
        }
        if (histList != null && histList.size() > 0) {
            pushFirmOffer(histList, 0);
        }
    }

    private void computerFirmOfferPushHist(List<FirmOfferMatchHist> firmOfferMatchHistList, Long
            userId, UserFirmOffer userFirmOffer) {
        List<FirmOfferPushHist> histList = new ArrayList<>();
        Map<String, List<FirmOfferMatchHist>> collect = firmOfferMatchHistList.stream().
                collect(Collectors.groupingBy(FirmOfferMatchHist::getSymbol));
        for (List<FirmOfferMatchHist> firmOfferMatchHists : collect.values()) {
            BigDecimal AvgPriceForBuy = new BigDecimal(0);
            BigDecimal AvgAmountForBuy = new BigDecimal(0);
            BigDecimal AvgPriceForSell = new BigDecimal(0);
            BigDecimal AvgAmountForSell = new BigDecimal(0);
            List<Date> buyDate = new ArrayList<>();
            List<Date> sellDate = new ArrayList<>();
            for (int i = 0; i < firmOfferMatchHists.size(); i++) {
                if ("buy".equals(firmOfferMatchHists.get(i).getType().substring(0, firmOfferMatchHists.get(i).getType().indexOf("-")))) {
                    AvgPriceForBuy = AvgPriceForBuy.add(firmOfferMatchHists.get(i).getPrice().multiply(firmOfferMatchHists.get(i).getFieldAmount()));
                    AvgAmountForBuy = AvgAmountForBuy.add(firmOfferMatchHists.get(i).getFieldAmount());
                    buyDate.add(firmOfferMatchHists.get(i).getMatchDate());
                }
                if ("sell".equals(firmOfferMatchHists.get(i).getType().substring(0, firmOfferMatchHists.get(i).getType().indexOf("-")))) {
                    AvgPriceForSell = AvgPriceForSell.add(firmOfferMatchHists.get(i).getPrice().multiply(firmOfferMatchHists.get(i).getFieldAmount()));
                    AvgAmountForSell = AvgAmountForSell.add(firmOfferMatchHists.get(i).getFieldAmount());
                    sellDate.add(firmOfferMatchHists.get(i).getMatchDate());
                }
            }
            Collections.sort(buyDate);
            Collections.sort(sellDate);
            if (AvgPriceForBuy.compareTo(new BigDecimal(0)) == 1) {
                FirmOfferPushHist hist = new FirmOfferPushHist();
                hist.setSymbol(firmOfferMatchHists.get(0).getSymbol());
                Date date = new Date();
                hist.setTime(buyDate.get(buyDate.size() - 1));
                date.setTime(System.currentTimeMillis());
                hist.setUtime(date);
                hist.setExchange(firmOfferMatchHists.get(0).getExChange());
                hist.setAmount(AvgAmountForBuy);
                hist.setPrice(AvgPriceForBuy.divide(AvgAmountForBuy, 4, BigDecimal.ROUND_HALF_UP));
                if (Dic.STOCK.equals(firmOfferMatchHists.get(0).getMatchType())) {
                    hist.setType("buy");
                } else {
                    hist.setType(Dic.MARGIN + "Buy");
                }
                hist.setUserId(Integer.parseInt(userId.toString()));
                hist.setUserName(userFirmOffer.getNickName());
                hist.setShowStatus(0);
                hist.setOrderId(firmOfferMatchHists.get(0).getId().toString());
                histList.add(hist);
            }
            if (AvgAmountForSell.compareTo(new BigDecimal(0)) == 1) {
                FirmOfferPushHist hist = new FirmOfferPushHist();
                hist.setSymbol(firmOfferMatchHists.get(0).getSymbol());
                Date date = new Date();
                hist.setTime(sellDate.get(sellDate.size() - 1));
                date.setTime(System.currentTimeMillis());
                hist.setUtime(date);
                hist.setExchange(firmOfferMatchHists.get(0).getExChange());
                hist.setAmount(AvgAmountForSell);
                hist.setPrice(AvgPriceForSell.divide(AvgAmountForSell, 4, BigDecimal.ROUND_HALF_UP));
                if (Dic.STOCK.equals(firmOfferMatchHists.get(0).getMatchType())) {
                    hist.setType("sell");
                } else {
                    hist.setType(Dic.MARGIN + "Sell");
                }
                hist.setUserId(Integer.parseInt(userId.toString()));
                hist.setUserName(userFirmOffer.getNickName());
                hist.setShowStatus(0);
                hist.setOrderId(firmOfferMatchHists.get(0).getId().toString());
                histList.add(hist);
            }
        }
        if (histList != null && histList.size() > 0) {
            pushFirmOffer(histList, 0);
        }

    }

    private List<FirmOfferMatchHist> getFirmOfferMatchHists(Long userId) {
        Date startDate = new Date();
        Date endDate = new Date();
        FirmOfferPushHistExample firmOfferPushHistExample = new FirmOfferPushHistExample();
        FirmOfferPushHistExample.Criteria pushOr = firmOfferPushHistExample.or();
        pushOr.andUserIdEqualTo(Integer.parseInt(userId.toString()));
        firmOfferPushHistExample.setOrderByClause("time desc");
        List<FirmOfferPushHist> firmOfferPushHists = firmOfferPushHistMapper.selectByExample(firmOfferPushHistExample);
        FirmOfferMatchHistExample firmOfferMatchHistExample = new FirmOfferMatchHistExample();
        FirmOfferMatchHistExample.Criteria or = firmOfferMatchHistExample.or();
        if (firmOfferPushHists != null && firmOfferPushHists.size() > 0) {
            or.andMatchDateGreaterThan(firmOfferPushHists.get(0).getUtime());
        } else {
            startDate.setTime(System.currentTimeMillis() - (5 * 60 * 1000));
            or.andMatchDateGreaterThan(startDate);
        }
        endDate.setTime(System.currentTimeMillis());
        or.andMatchDateLessThanOrEqualTo(endDate);
        or.andUserIdEqualTo(userId);
        firmOfferMatchHistExample.setOrderByClause("match_date desc");
        return firmOfferMatchHistMapper.selectByExample(firmOfferMatchHistExample);
    }

    private List<FirmOfferMatchHist> getFirmOfferMatchHistsForOkex(Long userId) {
        Date startDate = new Date();
        Date endDate = new Date();
        FirmOfferMatchHistExample firmOfferMatchHistExample = new FirmOfferMatchHistExample();
        FirmOfferMatchHistExample.Criteria or = firmOfferMatchHistExample.or();
        startDate.setTime(System.currentTimeMillis() - (3 * 60 * 1000));
        or.andUtimeGreaterThan(startDate);
        endDate.setTime(System.currentTimeMillis());
        or.andUtimeLessThanOrEqualTo(endDate);
        or.andPushStatesEqualTo(0);
        or.andFieldAmountGreaterThan(new BigDecimal(0));
        or.andExChangeEqualTo("Okex");
        or.andUserIdEqualTo(userId);
        firmOfferMatchHistExample.setOrderByClause("match_date desc");
        return firmOfferMatchHistMapper.selectByExample(firmOfferMatchHistExample);
    }

    private List<FirmOfferOrderHist> getFuturesFirm(Long UserId) {
        FirmOfferOrderHistExample offerOrderHistExample = new FirmOfferOrderHistExample();
        FirmOfferOrderHistExample.Criteria or = offerOrderHistExample.or();
        Date upDateByUserId = firmOfferOrderHistExt.getUpDateByUserId(UserId);
        or.andOrderDateGreaterThan(upDateByUserId);
        or.andUserIdEqualTo(UserId);
        or.andPushStatesEqualTo(0);
        //or.andOrderStatusGreaterThanOrEqualTo(0);
        offerOrderHistExample.setOrderByClause("order_date desc");
        return firmOfferOrderHistMapper.selectByExample(offerOrderHistExample);
    }

    private List<FirmOfferPushHist> getNoDealOrders() {
        FirmOfferPushHistExample firmOfferPushHistExample = new FirmOfferPushHistExample();
        FirmOfferPushHistExample.Criteria pushOr = firmOfferPushHistExample.or();
        pushOr.andOrderStatusLessThanOrEqualTo(1);
        pushOr.andShowStatusEqualTo(0);
        pushOr.andOrderIdIsNotNull();
        pushOr.andOrderStatusIsNotNull();
        firmOfferPushHistExample.setOrderByClause("time desc");
        List<FirmOfferPushHist> firmOfferPushHists = firmOfferPushHistMapper.selectByExample(firmOfferPushHistExample);
        return firmOfferPushHists;
    }


    public void pushFirmOffer(List<FirmOfferPushHist> histList, int type) {
        List<UserActionsPushVo> userActionsPushVos = new ArrayList<>();
        for (FirmOfferPushHist firmOfferPushHist : histList) {
            UserActionsPushVo userActionsPushVo = new UserActionsPushVo();
            userActionsPushVo.setAmount(firmOfferPushHist.getAmount());
            userActionsPushVo.setExchange(firmOfferPushHist.getExchange());
            userActionsPushVo.setLeaderName(firmOfferPushHist.getUserName());
            userActionsPushVo.setPrice(firmOfferPushHist.getPrice());
            if ("Bybit".equals(firmOfferPushHist.getExchange())) {
                userActionsPushVo.setSymbol(firmOfferPushHist.getSymbol().substring(0, 3) + "永续");
            } else {
                userActionsPushVo.setSymbol(firmOfferPushHist.getSymbol());
            }
            userActionsPushVo.setTime(firmOfferPushHist.getTime());
            userActionsPushVo.setType(firmOfferPushHist.getType());
            userActionsPushVo.setUserId(firmOfferPushHist.getUserId());
            userActionsPushVo.setUtime(firmOfferPushHist.getUtime());
            userActionsPushVo.setIsUpdate(type);
            userActionsPushVo.setOrderId(firmOfferPushHist.getOrderId());
            if (firmOfferPushHist.getOrderStatus() != null) {
                userActionsPushVo.setOrderStatus(firmOfferPushHist.getOrderStatus());
            } else {
                userActionsPushVo.setOrderStatus(0);
            }
            userActionsPushVos.add(userActionsPushVo);
            if (type == 1) {
                FirmOfferPushHistExample firmOfferPushHistExample = new FirmOfferPushHistExample();
                FirmOfferPushHistExample.Criteria or1 = firmOfferPushHistExample.or();
                or1.andOrderIdEqualTo(firmOfferPushHist.getOrderId());
                or1.andExchangeEqualTo(firmOfferPushHist.getExchange());
                firmOfferPushHistMapper.updateByExampleSelective(firmOfferPushHist, firmOfferPushHistExample);
            }
        }
        if (type == 0) {
            firmOfferPushHistExt.insertFirmOfferPushHist(histList);
        }
        sendKafkaUtils.sendFirmOffer(JSONObject.toJSONString(userActionsPushVos));
    }

    private FirmOfferPushHist computerOrderType(FirmOfferOrderHist firmOfferOrderHist) {
        FirmOfferPushHist firmOfferPushHist = new FirmOfferPushHist();
        //订单类型 期货的话 1：开多 2：开空 3：平多 4： 平空
        if ("1".equals(firmOfferOrderHist.getType())) {
            firmOfferPushHist.setType("openMore");
            firmOfferPushHist.setOrderType("1");
        } else if ("2".equals(firmOfferOrderHist.getType())) {
            firmOfferPushHist.setType("openEmpty");
            firmOfferPushHist.setOrderType("2");
        } else if ("3".equals(firmOfferOrderHist.getType())) {
            firmOfferPushHist.setType("flatMore");
            firmOfferPushHist.setOrderType("3");
        } else if ("4".equals(firmOfferOrderHist.getType())) {
            firmOfferPushHist.setType("flatEmpty");
            firmOfferPushHist.setOrderType("4");
        } else {
            log.info("非期货订单，暂时不要");
            return null;
        }
        return firmOfferPushHist;
    }


}
