package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.*;
import com.ailu.firmoffer.dao.mapper.FirmOfferKeyMapper;
import com.ailu.firmoffer.dao.mapper.FirmOfferPositionMapper;
import com.ailu.firmoffer.dao.mapper.UserFirmOfferMapper;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferOrderHistExt;
import com.ailu.firmoffer.dao.mapper.ext.UserMapperExt;
import com.ailu.firmoffer.exchange.conversion.BitmexConversion;
import com.ailu.firmoffer.exchange.conversion.OkexV3Conversion;
import com.ailu.firmoffer.util.Dic;
import com.ailu.firmoffer.web.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {

    @Resource
    private RateService rateService;

    @Resource
    private UserMapperExt userMapperExt;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserService userService;

    @Resource
    private UserFirmOfferMapper userFirmOfferMapper;

    @Resource
    private FirmOfferKeyMapper firmOfferKeyMapper;

    @Resource
    private FirmOfferPositionMapper firmOfferPositionMapper;

    @Resource
    private OkexV3Conversion okexV3Conversion;

    @Resource
    private BitmexConversion bitmexConversion;

    @Resource
    private FirmOfferExchangeBalanceSnapService firmOfferExchangeBalanceSnapService;

    @Autowired
    SymbolRateManager symbolRateManager;

    @Resource
    FirmOfferOrderHistExt firmOfferOrderHistExt;

    //今日收益
    final int TODAY_INCOME = 1;

    //昨日收益
    final int YESTERDAY_INCOME = 2;

    // 周收益
    final int WEEK_INCOME = 3;

    //月收益
    final int MONTH_INCOME = 4;

    //总收益
    final int ALL_INCOME = 9;

    public UserDemoPersonalAccountVO getUserAccountInfo(Long userId) {
        //获取用户的收益走势
        List<IncomeModelVO> incomeModelVOS = getUserIncomeLine(userId);
        //获取用户资产走势
        List<IncomeModelVO> balanceModelVOS = getUserBalanceLine(userId);
        //获取用户的每小时收益走势
        List<IncomeModelVO> benifitModelVOS = getUserBenifitLine(userId);
        //根据UserID获取用户信息
        UserFirmOffer user = userService.getUserByUserId(userId);
        //初始化用户账户和前端交互实体
        UserDemoPersonalAccountVO userDemoPersonalAccountVO = new UserDemoPersonalAccountVO();
        //判断用户信息是否为空
        if (user == null) {
            return userDemoPersonalAccountVO;
        }
        //用户名称
        userDemoPersonalAccountVO.setNickName(user.getNickName());
        //用户 ID
        userDemoPersonalAccountVO.setUserId(userId);
        //个人简介
        userDemoPersonalAccountVO.setBrief(user.getBrief());
        //用户头像
        userDemoPersonalAccountVO.setUserPic(user.getAvatar());
        //用户初始资产
        userDemoPersonalAccountVO.setInitBalance(userAccountService.getInitUserBalanceByUserId(userId));
        //个人总资产
        FirmOfferExchangeBalanceSnap snap = firmOfferExchangeBalanceSnapService.findTotalByUserId(userId);
        if (snap == null) {
            log.warn("userId{}暂未入库其账户资产，稍后再试！", user.getId());
            return userDemoPersonalAccountVO;
        }
        userDemoPersonalAccountVO.setBalance(snap.getTotal() == null ? BigDecimal.ZERO : snap.getTotal());
        userDemoPersonalAccountVO.setFuture(snap.getFuture() == null ? BigDecimal.ZERO : snap.getFuture());
        userDemoPersonalAccountVO.setStock(snap.getStock() == null ? BigDecimal.ZERO : snap.getStock());
        userDemoPersonalAccountVO.setWallet(snap.getWallet() == null ? BigDecimal.ZERO : snap.getWallet());
        userDemoPersonalAccountVO.setProfit(userDemoPersonalAccountVO.getBalance()
                .subtract(userAccountService.getInitUserBalanceByUserId(userId)));

        userDemoPersonalAccountVO.setSlogen(user.getSlogen());

        //入驻天数
        Integer userDaysNumber = getUserDaysNumber(userId);
        userDemoPersonalAccountVO.setNumberOfDays(userDaysNumber);
        //操作币种
        userDemoPersonalAccountVO.setCoins(user.getHascoins());
        //累计收益
        userDemoPersonalAccountVO.setAllIncome(getUserIncome(userId, ALL_INCOME));
        //今日累计收益
        userDemoPersonalAccountVO.setTodayIncome(getUserIncome(userId, TODAY_INCOME));
        //昨日累计收益
        userDemoPersonalAccountVO.setYesterdayIncome(getUserIncome(userId, YESTERDAY_INCOME));
        //月累计收益
        userDemoPersonalAccountVO.setMonthIncome(getUserIncome(userId, MONTH_INCOME));
        //周累计收益
        userDemoPersonalAccountVO.setWeekIncome(getUserIncome(userId, WEEK_INCOME));
        //用户收益走势图
        userDemoPersonalAccountVO.setIncomeModels(incomeModelVOS);
        //用户收益走势图
        userDemoPersonalAccountVO.setBalanceModels(balanceModelVOS);
        //用户每小时收益走势图
        userDemoPersonalAccountVO.setBenifitModels(benifitModelVOS);
        //用户所持币种比例
        userDemoPersonalAccountVO.setRatioMap(getRatioByUserId(userId));
        //用户现货仓位比例
        userDemoPersonalAccountVO.setSpotRatio(getSpotRatioByUserId(userId));
        //用户期货持仓信息
        List<FirmOfferPositionVo> futurePositionVos = getFuturePositionVo(userId);
        //日均操作次数
        Integer countOrder;
        Integer orderscount = firmOfferOrderHistExt.countOrdersByUserId(userId);
        if (orderscount == null || orderscount == 0) {
            Integer integer = firmOfferOrderHistExt.countMatchsByUserId(userId);
            if (integer == null || integer == 0) {
                countOrder = 0;
            } else {
                countOrder = integer;
            }
        } else {
            countOrder = orderscount;
        }
        if (countOrder == 0) {
            userDemoPersonalAccountVO.setAvgOperation(0);
        } else {
            userDemoPersonalAccountVO.setAvgOperation(countOrder / userDaysNumber);
        }
        userDemoPersonalAccountVO.setFuturePosition(futurePositionVos);
        if (futurePositionVos != null && !futurePositionVos.isEmpty()) {
            //如果用户有期货持仓信息的话，就获取其期货持仓比例
            setUserAccountRatioByType(futurePositionVos, userDemoPersonalAccountVO);
        }
        return userDemoPersonalAccountVO;
    }

    /**
     * 期货仓位：
     * 1.假如全仓20倍梭多，则多20倍，空0倍；
     * 2.假如半仓20倍梭多，半仓20倍梭空，则多10倍，空10倍。
     * 3.假如四分之一仓10倍梭多，则多2.5倍
     * <p>
     * 算法：
     * 多X倍就是拿所有多单仓位（张数），先乘以10或100，得到多单金额，再除以账户总资产。
     * 空X倍同理。
     * <p>
     * Bitmex:
     * 多空分开统计：
     * CurrentCost/10^8 * XBTUSD / 总资产
     * currentcost是用户持仓换算为XBt的数量；除以10^8是以XBT为单位
     * XBTUSD是XBT以USD为单位的价格
     * 总资产以USD为单位
     * 注：
     * 1.多空分别累加；
     * 2.CurrentCost可能为负，注意取绝对值；
     *
     * @param futurePositionVos
     * @param userDemoPersonalAccountVO
     */
    private void setUserAccountRatioByType(List<FirmOfferPositionVo> futurePositionVos, UserDemoPersonalAccountVO userDemoPersonalAccountVO) {
        BigDecimal balance = userDemoPersonalAccountVO.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        BigDecimal longAmount;

        BigDecimal shortAmount;
        if ("Bitmex".equals(futurePositionVos.get(0).getExChange())) {
            longAmount = getBitmexAllPositionsQtyByType(futurePositionVos, Dic.LONG);
            shortAmount = getBitmexAllPositionsQtyByType(futurePositionVos, Dic.SHORT);
            userDemoPersonalAccountVO.setFutureLongAmount(longAmount.divide(balance, 6, BigDecimal.ROUND_HALF_DOWN));
            userDemoPersonalAccountVO.setFutureShortAmount(shortAmount.divide(balance, 6, BigDecimal.ROUND_HALF_DOWN));
        } else {
            longAmount = getOkexAllPositionsQtyByType(futurePositionVos, Dic.LONG);
            shortAmount = getOkexAllPositionsQtyByType(futurePositionVos, Dic.SHORT);
            userDemoPersonalAccountVO.setFutureLongAmount(longAmount.divide(balance, 6, BigDecimal.ROUND_HALF_DOWN));
            userDemoPersonalAccountVO.setFutureShortAmount(shortAmount.divide(balance, 6, BigDecimal.ROUND_HALF_DOWN));
        }
    }

    private BigDecimal getBitmexAllPositionsQtyByType(List<FirmOfferPositionVo> futurePositionVos, String type) {
        return futurePositionVos.stream()
                .filter(firmOfferPositionVo -> type.equals(firmOfferPositionVo.getType()))
                .map(firmOfferPositionVo -> {
                    if (firmOfferPositionVo.getCurrentCost() == null) {
                        return BigDecimal.ZERO;
                    }
                    BigDecimal qty = firmOfferPositionVo.getCurrentCost().abs()
                            .divide(new BigDecimal(100000000), 8, BigDecimal.ROUND_HALF_DOWN)
                            .multiply(bitmexConversion.getNowPriceByInstrumentId("XBT"));
                    return qty;
                })
                .collect(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal getOkexAllPositionsQtyByType(List<FirmOfferPositionVo> futurePositionVos, String type) {
        return futurePositionVos.stream()
                .filter(firmOfferPositionVo -> type.equals(firmOfferPositionVo.getType()))
                .map(firmOfferPositionVo -> {
                    BigDecimal qty = firmOfferPositionVo.getQty();
                    if (firmOfferPositionVo.getTradingOn().contains("BTC-USD")) {
                        qty = qty.multiply(new BigDecimal(100));
                    } else {
                        qty = qty.multiply(new BigDecimal(10));
                    }
                    return qty;
                })
                .collect(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));
    }

    private List<FirmOfferPositionVo> getFuturePositionVo(Long userId) {
        FirmOfferPositionExample firmOfferPositionExample = new FirmOfferPositionExample();
        firmOfferPositionExample.or().andUserIdEqualTo(userId).andQtyNotEqualTo(BigDecimal.ZERO);
        List<FirmOfferPosition> positions = firmOfferPositionMapper.selectByExample(firmOfferPositionExample);
        if (positions == null || positions.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return positions.stream().map(firmOfferPosition -> {
            FirmOfferPositionVo firmOfferPositionVo = new FirmOfferPositionVo(okexV3Conversion, bitmexConversion);
            firmOfferPositionVo.setTradingOn(firmOfferPosition.getTradingOn());
            BeanUtils.copyProperties(firmOfferPosition, firmOfferPositionVo);
            if ("Bitmex".equals(firmOfferPositionVo.getExChange())) {
                if ("XBT".equals(firmOfferPositionVo.getInstrumentId()) || "ETHUSD".equals(firmOfferPositionVo.getTradingOn())) {
                    firmOfferPositionVo.setBitMexQtyUnit("USD");
                } else {
                    firmOfferPositionVo.setBitMexQtyUnit(firmOfferPositionVo.getInstrumentId());
                }
                firmOfferPositionVo.setAmount(firmOfferPositionVo.getCurrentCost().abs().divide(new BigDecimal(100000000), 6, BigDecimal.ROUND_HALF_DOWN));
                String substring = firmOfferPositionVo.getTradingOn().substring(firmOfferPositionVo.getTradingOn().length() - 3);
                String s = "";
                if ("USD".equals(substring)) {
                    s = "永续";
                }
                if ("long".equals(firmOfferPositionVo.getType())) {
                    firmOfferPositionVo.setInstrumentId(firmOfferPositionVo.getInstrumentId() + s + "多");
                } else {
                    firmOfferPositionVo.setInstrumentId(firmOfferPositionVo.getInstrumentId() + s + "空");
                }
                //【临时】 BITMEX的收益计算：margin*收益率
                firmOfferPositionVo.setRealizedPnl(firmOfferPositionVo.getMargin()
                        .multiply(firmOfferPositionVo.getPnlRatio()).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN)
                        .setScale(4, BigDecimal.ROUND_HALF_DOWN));
                if (firmOfferPositionVo.getTradingOn().contains("XBT")) {
                    BigDecimal bitmex_xbt_usd = symbolRateManager.getBitmexSymbolRate("Bitmex_XBT_USD");
                    //收益A 收益率/leverage*CurrentQty/AvgCostPrice/（1+收益率/leverage）
                    BigDecimal add = firmOfferPositionVo.getPnlRatio().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN)
                            .divide(new BigDecimal(firmOfferPositionVo.getLeverage()), 4, BigDecimal.ROUND_HALF_DOWN).add(BigDecimal.ONE);
                    BigDecimal multiply = firmOfferPositionVo.getPnlRatio().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN)
                            .divide(new BigDecimal(firmOfferPositionVo.getLeverage()), 4, BigDecimal.ROUND_HALF_DOWN)
                            .multiply(firmOfferPositionVo.getQty()).divide(firmOfferPositionVo.getAvgCost(), 4, BigDecimal.ROUND_HALF_DOWN)
                            .divide(add, 4, BigDecimal.ROUND_HALF_DOWN);
                    firmOfferPositionVo.setRealizedPnl(multiply.setScale(4, BigDecimal.ROUND_HALF_DOWN));
                    firmOfferPositionVo.setValue(firmOfferPositionVo.getQty().divide(bitmex_xbt_usd, 4, BigDecimal.ROUND_HALF_DOWN));
                } else if ("ETHUSD".equals(firmOfferPositionVo.getTradingOn())) {
                    //币coin实盘，操作页面，ETH永续，价值用数量/10000，单位是XBT
                    firmOfferPositionVo.setValue(firmOfferPositionVo.getQty().divide(new BigDecimal(10000)));
                    //币coin实盘，操作页面，ETH永续，收益算法，用“(MarkPrice-AvgCostPrice) * CurrentQty / 10 000”单位是XBT
                    //收益C (MarkPrice-AvgCostPrice)/AvgCostPrice*CurrentQty/10000
                    BigDecimal divide = firmOfferPositionVo.getPnlRatio().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN)
                            .divide(new BigDecimal(firmOfferPositionVo.getLeverage()), 4, BigDecimal.ROUND_HALF_DOWN)
                            .multiply(firmOfferPositionVo.getQty()).divide(new BigDecimal(10000), 4, BigDecimal.ROUND_HALF_DOWN);
                    firmOfferPositionVo.setRealizedPnl(divide.setScale(4, BigDecimal.ROUND_HALF_DOWN));
                } else {
                    //收益B 收益率/leverage*CurrentQty*AvgCostPrice*（1+收益率/leverage）
                    BigDecimal add = firmOfferPositionVo.getPnlRatio().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN)
                            .divide(new BigDecimal(firmOfferPositionVo.getLeverage()), 4, BigDecimal.ROUND_HALF_DOWN).add(BigDecimal.ONE);
                    BigDecimal multiply = firmOfferPositionVo.getPnlRatio().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN)
                            .divide(new BigDecimal(firmOfferPositionVo.getLeverage()), 4, BigDecimal.ROUND_HALF_DOWN)
                            .multiply(firmOfferPositionVo.getQty()).multiply(firmOfferPositionVo.getAvgCost()).multiply(add);
                    firmOfferPositionVo.setRealizedPnl(multiply.setScale(4, BigDecimal.ROUND_HALF_DOWN));
                    firmOfferPositionVo.setValue(firmOfferPositionVo.getQty());
                }
            }
            if ("Bybit".equals(firmOfferPositionVo.getExChange())) {
                firmOfferPositionVo.setAmount(firmOfferPositionVo.getCurrentCost());
            }
            if ("Okex".equals(firmOfferPositionVo.getExChange())) {
                BigDecimal leverage = new BigDecimal(firmOfferPositionVo.getLeverage());
                BigDecimal symbolRate = getSymbolRate(firmOfferPositionVo);
                if (firmOfferPositionVo.getTradingOn().contains("BTC")) {
                    //价值
                    firmOfferPositionVo.setValue(firmOfferPositionVo.getQty().multiply(new BigDecimal(100)).divide(symbolRate, 4, BigDecimal.ROUND_HALF_DOWN));
                    //保障金
                    firmOfferPositionVo.setMargin(firmOfferPositionVo.getQty()
                            .divide(leverage, 4, BigDecimal.ROUND_HALF_DOWN)
                            .multiply(new BigDecimal(100)).divide(symbolRate, 4, BigDecimal.ROUND_HALF_DOWN));
                    //收益
                    firmOfferPositionVo.setRealizedPnl(firmOfferPositionVo.getPnlRatio()
                            .multiply(firmOfferPositionVo.getQty()).multiply(new BigDecimal(100))
                            .divide(symbolRate, 4, BigDecimal.ROUND_HALF_DOWN).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN)
                            .divide(new BigDecimal(firmOfferPositionVo.getLeverage()), 4, BigDecimal.ROUND_HALF_DOWN));
                } else {
                    //价值
                    firmOfferPositionVo.setValue(firmOfferPositionVo.getQty().multiply(new BigDecimal(10)).divide(symbolRate, 4, BigDecimal.ROUND_HALF_DOWN));
                    //保证金
                    firmOfferPositionVo.setMargin(firmOfferPositionVo.getQty()
                            .divide(leverage, 4, BigDecimal.ROUND_HALF_DOWN)
                            .multiply(new BigDecimal(10)).divide(symbolRate, 4, BigDecimal.ROUND_HALF_DOWN));
                    //收益
                    firmOfferPositionVo.setRealizedPnl(firmOfferPositionVo.getPnlRatio()
                            .multiply(firmOfferPositionVo.getQty()).multiply(new BigDecimal(10))
                            .divide(symbolRate, 4, BigDecimal.ROUND_HALF_DOWN).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN)
                            .divide(new BigDecimal(firmOfferPositionVo.getLeverage()), 4, BigDecimal.ROUND_HALF_DOWN));
                }
            }
            if (firmOfferPositionVo.getTradingOn().contains("SWAP")) {
                BigDecimal symbolRate = getSymbolRate(firmOfferPositionVo);
                if (firmOfferPositionVo.getTradingOn().contains("BTC")) {
                    firmOfferPositionVo.setAmount(firmOfferPositionVo.getQty().multiply(new BigDecimal(100)).divide(symbolRate, 6, BigDecimal.ROUND_HALF_DOWN));
                } else {
                    firmOfferPositionVo.setAmount(firmOfferPositionVo.getQty().multiply(new BigDecimal(10)).divide(symbolRate, 6, BigDecimal.ROUND_HALF_DOWN));
                }
                if ("Okex".equals(firmOfferPositionVo.getExChange())) {
                    firmOfferPositionVo.setPnlRatio(firmOfferPositionVo.getPnlRatio().multiply(new BigDecimal(100)));
                }
            }
            if (firmOfferPositionVo.getLiquiPrice() == null) {
                if (firmOfferPositionVo.getLiquidationPrice() == null) {
                    firmOfferPositionVo.setLiquiPrice(BigDecimal.ZERO);
                } else {
                    firmOfferPositionVo.setLiquiPrice(firmOfferPositionVo.getLiquidationPrice());
                }
            }
            return firmOfferPositionVo;
        }).collect(Collectors.toList());
    }


    private BigDecimal getSymbolRate(FirmOfferPositionVo firmOfferPositionVo) {
        BigDecimal symbolRate;
        if (firmOfferPositionVo.getTradingOn().contains("BTC")) {
            symbolRate = symbolRateManager.getBitmexSymbolRate("Bitmex_XBT_USD");
        } else if (firmOfferPositionVo.getTradingOn().contains("ETH")) {
            symbolRate = symbolRateManager.getBitmexSymbolRate("Bitmex_ETH_USD");
        } else if (firmOfferPositionVo.getTradingOn().contains("TRX")) {
            symbolRate = symbolRateManager.getOkexSymbolRate("Okex_TRX_USDT");
        } else {
            String substring = firmOfferPositionVo.getTradingOn().substring(0, firmOfferPositionVo.getTradingOn().indexOf("-"));
            symbolRate = symbolRateManager.getOkexSymbolRate("Okex_" + substring + "QUARTER_USD");
        }
        if (symbolRate == null) {
            String substring = firmOfferPositionVo.getTradingOn().substring(0, firmOfferPositionVo.getTradingOn().indexOf("-"));
            symbolRate = symbolRateManager.getOkexSymbolRate("Okex_" + substring + "_USDT");
        }
        return symbolRate;
    }

    private Integer getUserDaysNumber(Long id) {
        FirmOfferKeyExample firmOfferKeyExample = new FirmOfferKeyExample();
        FirmOfferKeyExample.Criteria or = firmOfferKeyExample.or();
        or.andUserIdEqualTo(id);
        List<FirmOfferKey> firmOfferKeys = firmOfferKeyMapper.selectByExample(firmOfferKeyExample);
        if (firmOfferKeys != null && firmOfferKeys.size() > 0) {
            Integer Numberofdays = firmOfferKeys.get(0).getNumberofdays();
            return Numberofdays;
        }
        return 0;
    }

    /**
     * 计算用户所持币种比例
     *
     * @param userId
     * @return
     */
    private List<UserDemoCoinInfoVo> getRatioByUserId(Long userId) {
        List<UserAssetRatioVo> userAccoutRatio = userAccountService.getUserAccoutRatio(userId);
        List<UserDemoCoinInfoVo> collect = userAccoutRatio.stream().filter(userAssetRatioVo -> userAssetRatioVo.getPrice().compareTo(BigDecimal.ZERO) != 0)
                .map(userAssetRatioVo -> getUserDemoCoinInfoVo(userId, userAssetRatioVo)).collect(Collectors.toList());
        return collect;
    }

    /**
     * 计算用户现货仓位
     * 拿所有不是USDT（或者USD、TUSD、PAX）的虚拟币持仓，换算成USD，除以总资产。
     *
     * @return
     */
    private BigDecimal getSpotRatioByUserId(Long userId) {
        List<UserAssetRatioVo> userAccoutRatio = userAccountService.getUserAccoutRatio(userId, Dic.STOCK);
        BigDecimal ratio = userAccoutRatio.stream()
                .filter(userAssetRatioVo -> userAssetRatioVo.getPrice().compareTo(BigDecimal.ZERO) != 0)
                .filter(userAssetRatioVo -> !"USDT,USD,TUSD,PAX".contains(userAssetRatioVo.getSymbol().toUpperCase()))
                .map(userAssetRatioVo -> getUserDemoCoinInfoVo(userId, userAssetRatioVo))
                .map(userDemoCoinInfoVo -> userDemoCoinInfoVo.getRatio())
                .collect(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));
        return ratio;
    }

    private UserDemoCoinInfoVo getUserDemoCoinInfoVo(Long userId, UserAssetRatioVo userAssetRatioVo) {
        UserDemoCoinInfoVo userDemoCoinInfoVo = new UserDemoCoinInfoVo();
        userDemoCoinInfoVo.setSymbol(userAssetRatioVo.getSymbol().toUpperCase());
        userDemoCoinInfoVo.setCoinId(userAssetRatioVo.getCoin().longValue());
        userDemoCoinInfoVo.setRatio(userAssetRatioVo.getRatio());
        userDemoCoinInfoVo.setUserId(userId);
        return userDemoCoinInfoVo;
    }

    /**
     * 计算用户收益走势
     *
     * @param userId
     * @return
     */
    private List<IncomeModelVO> getUserIncomeLine(Long userId) {
        return userMapperExt.getUserIncomeLine(userId);
    }

    /**
     * 计算用户资产走势
     *
     * @param userId
     * @return
     */
    private List<IncomeModelVO> getUserBalanceLine(Long userId) {
        return userMapperExt.getUserBalance(userId);
    }

    /**
     * 计算用户小时收益走势
     *
     * @param userId
     * @return
     */
    private List<IncomeModelVO> getUserBenifitLine(Long userId) {
        return userMapperExt.getUserBenifit(userId);
    }

    /**
     * 计算用户累计收益
     *
     * @param userId
     * @param incomeFrom
     * @return
     */
    private BigDecimal getUserIncome(Long userId, int incomeFrom) {
        UserRateVo userVo;
        switch (incomeFrom) {
            case TODAY_INCOME:
                userVo = rateService.getTodayMaps().get(userId);
                return userVo != null ? userVo.getRatio().setScale(2, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO;
            case YESTERDAY_INCOME:
                userVo = rateService.getYesterdayMaps().get(userId);
                return userVo != null ? userVo.getRatio().setScale(2, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO;
            case WEEK_INCOME:
                userVo = rateService.getWeekMaps().get(userId);
                return userVo != null ? userVo.getRatio().setScale(2, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO;
            case MONTH_INCOME:
                userVo = rateService.getMonthMaps().get(userId);
                return userVo != null ? userVo.getRatio().setScale(2, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO;
            case ALL_INCOME:
                userVo = rateService.getAllMaps().get(userId);
                return userVo != null ? userVo.getRatio().multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }
}
