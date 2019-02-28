package com.ailu.firmoffer.task;


import com.ailu.firmoffer.dao.bean.*;
import com.ailu.firmoffer.dao.mapper.*;
import com.ailu.firmoffer.domain.RateModel;
import com.ailu.firmoffer.service.MetaService;
import com.ailu.firmoffer.util.Dic;
import com.ailu.firmoffer.web.vo.UserAccountVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.ailu.firmoffer.service.MetaService.userByExchanges;

@Slf4j
@Component
public class FirmOfferExchangeBalanceTask {

    @Resource
    FirmOfferExchangeBalanceMapper firmOfferExchangeBalanceMapper;

    @Resource
    MetaService metaService;

    @Resource
    FirmOfferExchangeBalanceSnapMapper firmOfferExchangeBalanceSnapMapper;

    @Resource
    FirmOfferExchangeBalanceHourMapper firmOfferExchangeBalanceHourMapper;

    @Resource
    FirmOfferExchangeBenifitHourMapper firmOfferExchangeBenifitHourMapper;

    @Resource
    UserInitMapper userInitMapper;

    @Resource
    InitUpdateMapper initUpdateMapper;

    /**
     * 数据快照
     */
    public void init() {
        saveFirmOfferExchangeBalanceSnap();
    }

    public void saveFirmOfferExchangeBalanceSnap() {
        List<FirmOfferExchangeBalance> distinctFirmOfferExchangeBalanceList = firmOfferExchangeBalanceMapper.selectDistinctUserId();
        List<FirmOfferExchangeBalanceSnap> insertList = new ArrayList<>();
        for (int i = 0; i < distinctFirmOfferExchangeBalanceList.size(); i++) {
            FirmOfferExchangeBalanceSnap firmOfferExchangeBalanceSnap = new FirmOfferExchangeBalanceSnap();
            Long userId = distinctFirmOfferExchangeBalanceList.get(i).getUserId();
            firmOfferExchangeBalanceSnap.setUserId(userId);
            FirmOfferKey firmOfferKey = userByExchanges.get(userId);
            if (firmOfferKey == null) {
                log.error("userId 未找到，暂不SNAP操作！");
                continue;
            }
            UserAccountVo userAccount;
            try {
                userAccount = getUserAccount(userId, firmOfferKey);
            } catch (Throwable e) {
                log.error("calc user balance error", e);
                return;
            }
            firmOfferExchangeBalanceSnap.setTotal(userAccount.getTotal());
            firmOfferExchangeBalanceSnap.setFuture(userAccount.getFuture());
            firmOfferExchangeBalanceSnap.setWallet(userAccount.getWallet());
            firmOfferExchangeBalanceSnap.setStock(userAccount.getStock());
            firmOfferExchangeBalanceSnap.setMargin(userAccount.getMargin());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateTime = df.format(System.currentTimeMillis());
            try {
                Date dates = df.parse(dateTime);
                firmOfferExchangeBalanceSnap.setCtime(dates);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            firmOfferExchangeBalanceSnap.setTimeNumber(Long.valueOf(dateFormat.format(new Date())));
            insertList.add(firmOfferExchangeBalanceSnap);
        }
        if (insertList.size() == 0) {
            log.warn("快照用户总资产时，数量为0，已跳过本次保存！");
            return;
        }
        firmOfferExchangeBalanceSnapMapper.insertAccounts(insertList);

        List<FirmOfferExchangeBalanceHour> insertListHour = new ArrayList<>();
        List<FirmOfferExchangeBenifitHour> insertListBenifitHour = new ArrayList<>();
        for (FirmOfferExchangeBalanceSnap snap : insertList) {

            insertListHour.add(new FirmOfferExchangeBalanceHour(snap));

            FirmOfferExchangeBenifitHour firmOfferExchangeBenifitHour = getUserBenifitHour(snap);
            if (null == firmOfferExchangeBenifitHour) {
                continue;
            }

            insertListBenifitHour.add(firmOfferExchangeBenifitHour);
        }
        firmOfferExchangeBalanceHourMapper.insertAccounts(insertListHour);
        firmOfferExchangeBenifitHourMapper.insertAccounts(insertListBenifitHour);
    }

    /**
     * 获取 用户每小时收益
     *
     * @param snap
     * @return
     */
    private FirmOfferExchangeBenifitHour getUserBenifitHour(FirmOfferExchangeBalanceSnap snap) {

        //获取用户初始资产 信息
        UserInitExample userInitExample = new UserInitExample();
        userInitExample.or().andUserIdEqualTo(snap.getUserId()).andBenifitTimeLessThan(snap.getCtime());
        List<UserInit> userInits = userInitMapper.selectByExample(userInitExample);
        if (userInits == null || userInits.size() < 1) {
            log.error("用户每小时收益计算：{} 用户不存在或入驻时间不符合计算条件", snap.getUserId());
            return null;
        }
        Date benifitTime = userInits.get(0).getBenifitTime();//入驻时间
        BigDecimal initOrigin = userInits.get(0).getInitOrigin();//初始资产

        //获取用户 充提资产 记录
        InitUpdateExample initUpdateExample = new InitUpdateExample();
        initUpdateExample.or().andCtimeBetween(benifitTime, snap.getCtime()).andUserIdEqualTo(snap.getUserId().intValue());
        List<InitUpdate> initUpdates = initUpdateMapper.selectByExample(initUpdateExample);
        BigDecimal sumTransger = sumTransfer(initUpdates);//充提资产流动 总和

        if (null == initOrigin) {
            log.error("用户每小时收益计算：{} 用户初始资产为空", snap.getUserId());
            return null;
        }
        //构建 用户收益 对象
        BigDecimal benifit = snap.getTotal().subtract(initOrigin).subtract(sumTransger);
        FirmOfferExchangeBenifitHour firmOfferExchangeBenifitHour = new FirmOfferExchangeBenifitHour();
        firmOfferExchangeBenifitHour.setUserId(snap.getUserId());
        firmOfferExchangeBenifitHour.setTotal(benifit);
        firmOfferExchangeBenifitHour.setCtime(snap.getCtime());
        SimpleDateFormat dateFormat = new SimpleDateFormat(FirmOfferExchangeBalanceHour.TIMENUM_TYPE);
        firmOfferExchangeBenifitHour.setTimeNumber(Long.valueOf(dateFormat.format(snap.getCtime())));
        return firmOfferExchangeBenifitHour;
    }

    /**
     * 获取用户 充提资产流动 总和
     *
     * @param initUpdates
     * @return
     */
    private BigDecimal sumTransfer(List<InitUpdate> initUpdates) {
        BigDecimal sum = new BigDecimal("0");
        for (InitUpdate initUpdate : initUpdates) {
            sum = sum.add(initUpdate.getTransferPrice());
        }
        return sum;
    }

    public UserAccountVo getUserAccount(Long userId, FirmOfferKey firmOfferKey) {
        FirmOfferExchangeBalanceExample firmOfferExchangeBalanceExample = new FirmOfferExchangeBalanceExample();
        firmOfferExchangeBalanceExample.or().andUserIdEqualTo(userId).andExChangeEqualTo(firmOfferKey.getExChange());
        List<FirmOfferExchangeBalance> firmOfferExchangeBalance = firmOfferExchangeBalanceMapper.selectByExample(firmOfferExchangeBalanceExample);
        List<FirmOfferExchangeBalance> stockList = firmOfferExchangeBalance.stream().filter(balance -> balance.getType().equals(Dic.STOCK)).collect(Collectors.toList());
        List<FirmOfferExchangeBalance> futureList = firmOfferExchangeBalance.stream().filter(balance -> balance.getType().equals(Dic.FUTURE)).collect(Collectors.toList());
        List<FirmOfferExchangeBalance> walletList = firmOfferExchangeBalance.stream().filter(balance -> balance.getType().equals(Dic.WALLET)).collect(Collectors.toList());
        List<FirmOfferExchangeBalance> swapList = firmOfferExchangeBalance.stream().filter(balance -> balance.getType().equals(Dic.SWAP)).collect(Collectors.toList());
        List<FirmOfferExchangeBalance> marginList = firmOfferExchangeBalance.stream().filter(balance -> balance.getType().equals(Dic.MARGIN)).collect(Collectors.toList());
        BigDecimal total = getTotalsByType(firmOfferExchangeBalance, firmOfferKey.getExChange());
        BigDecimal stock = getTotalsByType(stockList, firmOfferKey.getExChange());
        BigDecimal future = getTotalsByType(futureList, firmOfferKey.getExChange());
        BigDecimal wallet = getTotalsByType(walletList, firmOfferKey.getExChange());
        BigDecimal swap = getTotalsByType(swapList, firmOfferKey.getExChange());
        BigDecimal margin = getTotalsByType(marginList, firmOfferKey.getExChange());
        log.info("{} 用户的总资产计算后结果为：{},期货资产计算后结果为：{},现货资产计算后结果为：{},钱包资产计算后结果为：{},永续资产计算后结果为：{},币币杠杆资产计算后结果为：{}", userId, total, future, stock, wallet, swap, margin);
        return new UserAccountVo(total, stock, future, wallet, swap, margin);
    }

    private BigDecimal getTotalsByType(List<FirmOfferExchangeBalance> firmOfferExchangeBalance, String exchange) {
        List<RateModel> rateModelList = new ArrayList<>();
        for (int totals = 0; totals < firmOfferExchangeBalance.size(); totals++) {
            RateModel rateModel = new RateModel();
            rateModel.setSymbol(firmOfferExchangeBalance.get(totals).getSymbol());
            rateModel.setExchangeName(firmOfferExchangeBalance.get(totals).getExChange());
            rateModel.setAmount(firmOfferExchangeBalance.get(totals).getAmount().subtract(firmOfferExchangeBalance.get(totals).getLoan()).subtract(firmOfferExchangeBalance.get(totals).getFee()));
            rateModel.setCoinId(firmOfferExchangeBalance.get(totals).getCoin());
            rateModelList.add(rateModel);
        }
        return metaService.getBalanceBySymbol(rateModelList, exchange);
    }

}
