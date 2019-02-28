package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.*;
import com.ailu.firmoffer.dao.mapper.*;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferLedgerHistExt;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferUserInitExt;
import com.ailu.firmoffer.dao.mapper.ext.UserMapperExt;
import com.ailu.firmoffer.domain.RateModel;
import com.ailu.firmoffer.task.FirmOfferExchangeBalanceTask;
import com.ailu.firmoffer.task.WebTask;
import com.ailu.firmoffer.util.DateUtils;
import com.ailu.firmoffer.web.vo.UserAssetRatioVo;
import com.ailu.firmoffer.web.vo.UserRateVo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/22 11:59
 */
@Slf4j
@Service
public class MetaService {

    @Resource
    FirmOfferKeyMapper keyMapper;

    @Resource
    FirmOfferCoinContrastMapper contrastMapper;

    @Resource
    SymbolRateManager symbolRateManager;

    @Resource
    RateService rateService;

    @Resource
    FirmOfferExchangeBalanceTask firmOfferExchangeBalanceTask;

    @Resource
    UserService userService;

    @Resource
    UserMapperExt userMapperExt;

    @Resource
    WebTask webTask;

    @Resource
    CoinContrastManager coinContrastManager;

    @Resource
    UserFirmOfferMapper userFirmOfferMapper;

    @Resource
    FirmOfferLedgerHistMapper firmOfferLedgerHistMapper;

    @Resource
    FirmOfferLedgerHistExt firmOfferLedgerHistExt;

    /**
     * 项目重启时初始化
     */
    public static Map<String, String> restartMap = new ConcurrentHashMap<>();
    /**
     * 所有用户信息
     */
    public static Map<Long, UserFirmOffer> userMap = new ConcurrentHashMap<>();

    /**
     * 用户所属的交易所
     * key:用户id
     * values:用户的Key信息
     */
    public static Map<Long,FirmOfferKey> userByExchanges = new ConcurrentHashMap<>();

    public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    public static final String EX_CHANGE_BITFINEX = "Bitfinex";
    public static final String EX_CHANGE_HUOBI = "Huobi";
    public static final String EX_CHANGE_BIBOX = "Bibox";
    public static final String EX_CHANGE_BINANCE = "Binance";
    public static final String EX_CHANGE_OKEX = "Okex";

    public UserFirmOffer getUserMap(Long id) {
        return userMap.get(id);
    }

    public String getRestartMap(String key) {
        return restartMap.get(key);
    }

    public void putRestartMap(String key, String val) {
        restartMap.put(key, val);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    private void task() {
        getUserByExchangeMap();
        coinContrastManager.init();
        log.info("开始定时更新币种汇率 ");
        try {
            symbolRateManager.init();
            firmOfferExchangeBalanceTask.init();
        } catch (Throwable e) {
            log.error("定时更新币种或快照用户总资产出错！", e);
        }
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                symbolRateManager.init();
            } catch (Throwable e) {
                log.error("定时更新币种汇率出错", e);
            }
        }, 30, 30, TimeUnit.SECONDS);

        log.info("开始定时获取用户账户信息！");
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                webTask.getUserAccountInfo();
            } catch (Throwable e) {
                log.error("获取用户账户信息！", e);
            }
        }, 10, 10, TimeUnit.SECONDS);

        log.info("开始定时获取用户账户信息！");
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                webTask.getOperatingHistory();
            } catch (Throwable e) {
                log.error("获取用户操作历史！", e);
            }
        }, 10, 10, TimeUnit.SECONDS);

        log.info("开始定时快照用戶总资产 ");
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                firmOfferExchangeBalanceTask.init();
            } catch (Throwable e) {
                log.error("定时快照用戶总资产出错", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                getUserByExchangeMap();
                calcAllRate();
            } catch (Throwable e) {
                log.error("初始化收益计算出错", e);
            }
        }, 0, 1, TimeUnit.MINUTES);

        log.info("开始每小时保存用户收益！");
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                rateService.addRateLine();
            } catch (Throwable e) {
                log.error("每小时保存用户收益快照出错！", e);
            }
        }, 1, 1, TimeUnit.HOURS);
//        scheduledExecutorService.scheduleAtFixedRate(() -> {
//            try {
//                log.info("开始重置用户初始化资产");
//                checkTotalBalance();
//                log.info("结束重置用户初始化资产");
//            } catch (Throwable e) {
//                log.error("每3分钟检查用户是否有充值记录出错！", e);
//            }
//        }, 0, 3, TimeUnit.MINUTES);
    }

    /**
     * 检查总资产 如果变动需要更改其初始资产
     */
    private void checkTotalBalance() {
//        FirmOfferLedgerHistExample firmOfferLedgerHistExample = new FirmOfferLedgerHistExample();
//        List<String> typeNames = Arrays.asList("transfer","rebate","充值","提现","转入点对点账户","点对点账户转出");
//        firmOfferLedgerHistExample.or().andTypeNameIn(typeNames)
//                .andStatusEqualTo("00");
//        List<FirmOfferLedgerHist> firmOfferLedgerHists = firmOfferLedgerHistMapper.selectByExample(firmOfferLedgerHistExample);
        String typeNames = "'transfer','rebate','充值','提现','转入点对点账户','点对点账户转出'";
        String status = "00";
        List<FirmOfferLedgerHist> firmOfferLedgerHists = firmOfferLedgerHistExt.selectByLedgers(typeNames,status);
        if (firmOfferLedgerHists==null||firmOfferLedgerHists.isEmpty()){
            return;
        }
        Map<Long, List<FirmOfferLedgerHist>> collect = firmOfferLedgerHists.stream().collect(Collectors.groupingBy(FirmOfferLedgerHist::getUserId, Collectors.toList()));
        collect.entrySet().forEach(map -> {
            try {
                userService.updateUserInit(map.getKey(),map.getValue());
            }catch (Throwable e){
                log.error("userId {} update init price error",e);
            }
        });
    }

    private void getUserByExchangeMap() {
        FirmOfferKeyExample firmOfferKeyExample = new FirmOfferKeyExample();
        List<FirmOfferKey> firmOfferKeys = keyMapper.selectByExample(firmOfferKeyExample);
        if (firmOfferKeys==null||firmOfferKeys.isEmpty()){
            log.warn("未配置的用户key信息");
        }
        Map<Long, FirmOfferKey> collect = firmOfferKeys.stream().collect(Collectors.toMap(FirmOfferKey::getUserId, Function.identity()));
        userByExchanges.putAll(collect);
    }

    /**
     * 由于目前确定Huobi的换算单位为USDT,Bitfinex的换算单位为USD
     * NOTE:
     * 1.首先根据交易所筛选，并且拼接OnlyKey
     * 2.从定时获取的汇率Map中获得最新汇率
     * 3.根据业务返回其汇率后的价格
     *
     * @param rateModels 汇率换算model
     * @param exchange 汇率换算需要的交易所
     * @return
     */
    public BigDecimal getBalanceBySymbol(List<RateModel> rateModels, String exchange) {
        BigDecimal collect = BigDecimal.ZERO;
        collect = collect.add(getAllBalanceByExchange(rateModels, exchange));
        log.debug("总资产为{}", collect);
        return collect.setScale(2, BigDecimal.ROUND_DOWN);
    }

    private BigDecimal getAllBalanceByExchange(List<RateModel> rateModels, String exchangeName) {
        BigDecimal collect = rateModels.stream().map(rateModel -> rateModel.getPriceByOnlyKey(symbolRateManager)).collect(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));
        return collect;
    }

    /**
     * NOTE:
     * 将传入的信息，每个对象赋予最新价格然后返回即可
     *
     * @param userAssetRatioVos 汇率换算model
     * @return
     */
    public List<UserAssetRatioVo> getUserAssetRatioVosForPrice(List<UserAssetRatioVo> userAssetRatioVos) {
        return userAssetRatioVos.stream().map(userAssetRatioVo -> {
            try {
                userAssetRatioVo.setPriceByCurrPrice(symbolRateManager);
            }catch (Throwable e){
                log.warn("汇率计算出错,出错币种{},错误信息{}", JSON.toJSONString(userAssetRatioVo),e);
                userAssetRatioVo.setPrice(BigDecimal.ZERO);
            }
            return userAssetRatioVo;
        }).collect(Collectors.toList());
    }

    /**
     * 计算用户的累积收益率
     */
    private void calcAllRate() {
        try {
            log.info("开始计算用户的累积收益率！");
            List<UserRateVo> allList = userService.getUserByRateRanking();
            rateService.setAllLists(allList);
            rateService.setAllMaps(allList.stream().collect(Collectors.toMap(UserRateVo::getUserId, Function.identity(), (o1, o2) -> o1)));
            log.info("开始计算用户的今日收益率！");
            List<UserRateVo> todayList = userMapperExt.getRatioLine(DateUtils.getZeroTime(), getDates(System.currentTimeMillis()));
            rateService.setTodayLists(computerRatio(todayList));
            rateService.setTodayMaps(computerRatio(todayList).stream().collect(Collectors.toMap(UserRateVo::getUserId, Function.identity(), (o1, o2) -> o1)));
            log.info("开始计算用户的昨日收益率！");
            List<UserRateVo> yesterdayList = userMapperExt.getRatioLine(DateUtils.getYesterdayDate(), DateUtils.getZeroTime());
            rateService.setYesterdayLists(computerRatio(yesterdayList));
            rateService.setYesterdayMaps(computerRatio(yesterdayList).stream().collect(Collectors.toMap(UserRateVo::getUserId, Function.identity(), (o1, o2) -> o1)));
            log.info("开始计算用户的周收益率！");
            List<UserRateVo> weekList = userMapperExt.getRatioLine(getDates(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000),
                    getDates(System.currentTimeMillis()));
            rateService.setWeekLists(computerRatio(weekList));
            rateService.setWeekMaps(computerRatio(weekList).stream().collect(Collectors.toMap(UserRateVo::getUserId, Function.identity(), (o1, o2) -> o1)));
            log.info("开始计算用户的月收益率！");
            List<UserRateVo> monthList = userMapperExt.getRatioLine(getDates((System.currentTimeMillis()) - (Long.valueOf(30L * 24L * 60L * 60L * 1000L))), getDates(System.currentTimeMillis()));
            rateService.setMonthLists(computerRatio(monthList));
            rateService.setMonthMaps(computerRatio(monthList).stream().collect(Collectors.toMap(UserRateVo::getUserId, Function.identity(), (o1, o2) -> o1)));
        } catch (Throwable e) {
            log.error("计算收益失败", e);
        }
    }

    private List<UserRateVo> computerRatio(List<UserRateVo> userRateVos) {
        List<UserRateVo> userRateVoList = new ArrayList<>();
        //.divide(userRateVos.get(userRateVos.size() - 1).getRatio().add(new BigDecimal(1)), 2, BigDecimal.ROUND_HALF_UP
        Map<Long, List<UserRateVo>> collect = userRateVos.stream().collect(Collectors.groupingBy(UserRateVo::getUserId, Collectors.toList()));
        for (Map.Entry<Long, List<UserRateVo>> userRateMap : collect.entrySet()) {
            UserRateVo userRateVo = new UserRateVo();
            Long userId = userRateMap.getKey();
            List<UserRateVo> value = userRateMap.getValue();
            if (value.isEmpty()) {
                log.warn("用户{}的收益集合为空，无法计算收益率.", userId);
                continue;
            }
            BigDecimal divide = value.get(0).getRatio().subtract(value.get(value.size() - 1).getRatio());
            userRateVo.setRatio(divide);
            userRateVo.setUserId(userId);
            userRateVoList.add(userRateVo);
        }
        return userRateVoList;
    }

    private Date getDates(Long times) {
        Date date = new Date();
        date.setTime(times);
        return date;
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void getAllUserMessage() {
        UserFirmOfferExample userFirmOfferExample = new UserFirmOfferExample();
        List<UserFirmOffer> userFirmOffers = userFirmOfferMapper.selectByExample(userFirmOfferExample);
        for (UserFirmOffer userFirmOffer : userFirmOffers) {
            userMap.put(userFirmOffer.getId(), userFirmOffer);
        }
    }

}
