package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.*;
import com.ailu.firmoffer.dao.mapper.InitUpdateMapper;
import com.ailu.firmoffer.dao.mapper.UserFirmOfferMapper;
import com.ailu.firmoffer.dao.mapper.UserInitMapper;
import com.ailu.firmoffer.dao.mapper.UserMapper;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferLedgerHistExt;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferUserInitExt;
import com.ailu.firmoffer.dao.mapper.ext.UserMapperExt;
import com.ailu.firmoffer.task.FirmOfferExchangeBalanceTask;
import com.ailu.firmoffer.util.OnlyKeysUtil;
import com.ailu.firmoffer.web.vo.UserAccountVo;
import com.ailu.firmoffer.web.vo.UserRateVo;
import com.ailu.firmoffer.web.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ailu.firmoffer.service.MetaService.restartMap;
import static com.ailu.firmoffer.service.MetaService.userByExchanges;

/**
 * @author mr.wang
 * @version V1.0
 * @Description NOTE:
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserFirmOfferMapper userFirmOfferMapper;

    @Resource
    private UserMapperExt userMapperExt;

    @Resource
    private FirmOfferExchangeBalanceSnapService firmOfferExchangeBalanceSnapService;

    @Resource
    private RateService rateService;

    @Resource
    private UserInitMapper userInitMapper;

    @Resource
    private FirmOfferUserInitExt firmOfferUserInitExt;

    @Resource
    private FirmOfferExchangeBalanceTask firmOfferExchangeBalanceTask;

    @Resource
    private InitUpdateMapper initUpdateMapper;

    @Resource
    private FirmOfferLedgerHistExt firmOfferLedgerHistExt;

    @Override
    public UserFirmOffer getUserByUserId(Long userId) {
        return userFirmOfferMapper.selectByPrimaryKey(userId);
    }

    @Override
    public List<UserRateVo> getUserByRateRanking() {
        return userMapperExt.getProfitAll();
    }

    @Resource
    SymbolRateManager symbolRateManager;

    @Value("${user_ids}")
    private String userIds;

    /**
     * 根据用户的今日收益排名 查询用户
     *
     * @return
     */
    @Override
    public List<UserRateVo> getUserByTodayRateRanking() {
        return userMapperExt.getProfitToday();
    }

    /**
     * 根据用户的昨日收益排名 查询用户
     *
     * @return
     */
    @Override
    public List<UserRateVo> getUserByYesterdayRateRanking() {
        return userMapperExt.getProfitYesterday();
    }

    /**
     * 根据用户的周收益排名 查询用户
     *
     * @return
     */
    @Override
    public List<UserRateVo> getUserByWeekRateRanking() {
        return userMapperExt.getProfitLastWeek();
    }

    /**
     * 根据用户的月收益排名 查询用户
     *
     * @return
     */
    @Override
    public List<UserRateVo> getUserByMonthRateRanking() {
        return userMapperExt.getProfitLastMonth();
    }

    @Override
    public List<UserVo> getFirmUsers(int pageNum, int pageSize) {
        int skipNum = (pageNum - 1) * pageSize;
        int limitNum = pageSize;
        return userMapperExt.getFirmUsers(userIds).stream().map(userVo -> {
            FirmOfferExchangeBalanceSnap totalByUserId = firmOfferExchangeBalanceSnapService.findTotalByUserId(userVo.getUserId());
            userVo.setBalance(totalByUserId.getTotal());
            UserRateVo userRateVo = rateService.getAllMaps().get(userVo.getUserId());
            userVo.setRatio(userRateVo == null ? BigDecimal.ZERO : userRateVo.getRatio().multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN));
            return userVo;
        }).skip(skipNum).limit(limitNum).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInit(Long userId, BigDecimal price) {

    }

    /**
     * 用户初始资金为C
     * 定时【抓取充提记录】和【计算总资金】
     * 假如，发现用户【充值或者提现】金额换算为USD为b
     * 同时抓到总资金换算为USD为a
     * C=a*C/（a-b）
     * @param firmOfferLedgerHist
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInit(FirmOfferLedgerHist firmOfferLedgerHist) {
        UserInitExample userInitExample = new UserInitExample();
        userInitExample.or().andUserIdEqualTo(firmOfferLedgerHist.getUserId());
        List<UserInit> userInits = userInitMapper.selectByExample(userInitExample);
        if (userInits==null||userInits.isEmpty()){
            return;
        }
        UserInit userInit = userInits.get(0);
        String onlyKey = firmOfferLedgerHist.getExchange()+"_"+firmOfferLedgerHist.getCurrency().toUpperCase()+"_"+"USD";
        FirmOfferKey firmOfferKey = userByExchanges.get(firmOfferLedgerHist.getUserId());
        if (firmOfferKey==null){
            log.error("userId {} 未找到，暂不更改初始化操作！",firmOfferLedgerHist.getUserId());
            return;
        }
        UserAccountVo userAccount = firmOfferExchangeBalanceTask.getUserAccount(firmOfferLedgerHist.getUserId(), firmOfferKey);
        if (userAccount==null){
            log.warn("用户 {} 账户未找到，不进行更改初始化操作",firmOfferLedgerHist.getUserId());
            return;
        }
        BigDecimal price = getPriceByOnlyKey(symbolRateManager, onlyKey, firmOfferLedgerHist.getAmount());
        BigDecimal finalPrice;
        if (BigDecimal.ZERO.compareTo(firmOfferLedgerHist.getAmount())<0||"充值".equals(firmOfferLedgerHist.getTypeName())||"点对点账户转出".equals(firmOfferLedgerHist.getTypeName())){
            price = price.abs();
        }
        if (BigDecimal.ZERO.compareTo(firmOfferLedgerHist.getAmount())>0||"提现".equals(firmOfferLedgerHist.getTypeName())||"转入点对点账户".equals(firmOfferLedgerHist.getTypeName())){
            price = price.abs().multiply(new BigDecimal("-1"));
        }
        BigDecimal total = userAccount.getTotal();
        finalPrice = total.multiply(userInit.getBalance()).divide((total.subtract(price)), 8, BigDecimal.ROUND_HALF_DOWN);
        firmOfferUserInitExt.updateUserInit(finalPrice,firmOfferLedgerHist.getUserId());
        log.info("userId {} init balance update : {} ,old price : {}",firmOfferLedgerHist.getUserId(),finalPrice,userInit.getBalance());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInit(Long userId, List<FirmOfferLedgerHist> firmOfferLedgerHists) {
        if (firmOfferLedgerHists.isEmpty()){
            return;
        }
        String collect = firmOfferLedgerHists.stream().map(FirmOfferLedgerHist::getLedgerId).collect(Collectors.joining(","));
        UserInitExample userInitExample = new UserInitExample();
        userInitExample.or().andUserIdEqualTo(userId);
        FirmOfferKey firmOfferKey = userByExchanges.get(userId);
        List<UserInit> userInits = userInitMapper.selectByExample(userInitExample);
        if (userInits==null||userInits.isEmpty()){
            return;
        }
        UserInit userInit = userInits.get(0);
        if (firmOfferKey==null){
            log.error("userId {} 未找到，暂不更改初始化操作！",userId);
            return;
        }
        UserAccountVo userAccount = firmOfferExchangeBalanceTask.getUserAccount(userId, firmOfferKey);
        if (userAccount==null){
            log.warn("用户 {} 账户未找到，不进行更改初始化操作",userId);
            return;
        }
        BigDecimal transferPrice = BigDecimal.ZERO;
        for (int i = 0; i < firmOfferLedgerHists.size(); i++) {
            String onlyKey = firmOfferLedgerHists.get(i).getExchange()+"_"+firmOfferLedgerHists.get(i).getCurrency().toUpperCase()+"_"+"USD";
            BigDecimal priceByOnlyKey = getPriceByOnlyKey(symbolRateManager, onlyKey, firmOfferLedgerHists.get(i).getAmount());
            if (BigDecimal.ZERO.compareTo(firmOfferLedgerHists.get(i).getAmount())<0||"充值".equals(firmOfferLedgerHists.get(i).getTypeName())||"点对点账户转出".equals(firmOfferLedgerHists.get(i).getTypeName())){
                priceByOnlyKey = priceByOnlyKey.abs();
            }
            if (BigDecimal.ZERO.compareTo(firmOfferLedgerHists.get(i).getAmount())>0||"提现".equals(firmOfferLedgerHists.get(i).getTypeName())||"转入点对点账户".equals(firmOfferLedgerHists.get(i).getTypeName())){
                priceByOnlyKey = priceByOnlyKey.abs().multiply(new BigDecimal("-1"));
            }
            transferPrice = transferPrice.add(priceByOnlyKey);
        }
        BigDecimal finalPrice;
        BigDecimal total = userAccount.getTotal();
        finalPrice = total.multiply(userInit.getBalance()).divide((total.subtract(transferPrice)), 8, BigDecimal.ROUND_HALF_DOWN);
        firmOfferUserInitExt.updateUserInit(finalPrice,userId);
        firmOfferLedgerHistExt.updateStatusInLedgerIds(collect);
        InitUpdate initUpdate = new InitUpdate();
        initUpdate.setInit(userInit.getBalance());
        initUpdate.setTotal(total);
        initUpdate.setTransferPrice(transferPrice);
        initUpdate.setUserId(userId.intValue());
        initUpdate.setFinalInit(finalPrice);
        initUpdate.setCtime(new Date());
        initUpdateMapper.insertSelective(initUpdate);
        log.info("userId {} init balance update : {} ,old price : {}",userId,finalPrice,userInit.getBalance());
    }

    /**
     * TODO 本方法需要在本Model中注入其他Service 否则会报空指针
     *
     * @return
     */
    public BigDecimal getPriceByOnlyKey(SymbolRateManager symbolRateManager,String onlyKey,BigDecimal amount) {
        BigDecimal usdTemp = BigDecimal.ZERO;
        BigDecimal btcTemp = BigDecimal.ZERO;
        if (onlyKey == null) {
            return BigDecimal.ZERO;
        }
        String[] keys = onlyKey.split("_");
        if (Objects.equals(keys[1], keys[2])) {
            //如果单位和市场相同，返回其量为价格即可
            log.debug(" OnlyKey 为{}的计算数量为{},计算结果为：{},当前兑换BTC汇率为：{}，兑换USDT汇率为：{}", onlyKey, amount, amount, btcTemp, usdTemp);
            return amount;
        }
        usdTemp = getRatePrice(symbolRateManager, usdTemp, onlyKey,keys[0]).multiply(amount);
        if (usdTemp.compareTo(BigDecimal.ZERO) != 0) {
            return usdTemp;
        }
        onlyKey = keys[0] + "_" + keys[1] + "_" + "USDT";
        if ("USDT".equalsIgnoreCase(keys[1])){
            return amount;
        }
        usdTemp = getRatePrice(symbolRateManager, usdTemp, onlyKey,keys[0]).multiply(amount);
        if (usdTemp.compareTo(BigDecimal.ZERO) != 0) {
            return usdTemp;
        }
        onlyKey = keys[0] + "_" + keys[1] + "_" + "BTC";
        if ("BTC".equals(keys[1])) {
            if (amount != null) {
                btcTemp = amount;
            }
            usdTemp = btcTemp.multiply(getRatePrice(symbolRateManager, usdTemp, getOnlykey("BTC",keys[0]),keys[0]));
        } else {
            btcTemp = getRatePrice(symbolRateManager, btcTemp, onlyKey,keys[0]);
            if (btcTemp == null) {
                //如果此币种连BTC都没有换算，就没有换算必要了
                return BigDecimal.ZERO;
            } else {
                usdTemp = btcTemp.multiply(getRatePrice(symbolRateManager, usdTemp, getOnlykey("BTC",keys[0]),keys[0])).multiply(amount);
            }
        }
        BigDecimal multiply = usdTemp;
        if (multiply.compareTo(BigDecimal.ZERO) != 0) {
            log.debug(" OnlyKey 为{}的计算数量为{},计算结果为：{},当前兑换BTC汇率为：{}，兑换USDT汇率为：{}", onlyKey, amount, multiply, btcTemp, usdTemp);
        }
        return multiply;
    }

    private BigDecimal getRatePrice(SymbolRateManager symbolRateManager, BigDecimal temp, String onlyKey,String exchange) {
        return OnlyKeysUtil.getRatePrice(symbolRateManager,temp,exchange,onlyKey);
    }

    private String getOnlykey(String symbol,String exchange) {
        try {
            return OnlyKeysUtil.getOnlyKeyByExchange(exchange,symbol);
        } catch (Throwable e) {
            log.warn("CoinContrastManager 未准备就绪或未找到此币种,{},{}", symbol, exchange);
        }
        return "";
    }
}
