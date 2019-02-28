package com.ailu.firmoffer.web.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author mr.wang
 * @version V1.0
 * @Description NOTE:
 * 用户账户和前端交互 model
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Data
public class UserDemoPersonalAccountVO {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String userPic;

    /**
     * 累计收益
     */
    private BigDecimal allIncome;

    /**
     * 今日累计收益
     */
    private BigDecimal todayIncome;

    /**
     * 昨日累计收益
     */
    private BigDecimal yesterdayIncome;

    /**
     * 月累计收益
     */
    private BigDecimal monthIncome;

    /**
     * 周累计收益
     */
    private BigDecimal weekIncome;

    /**
     * 用户收益走势图
     */
    private List<IncomeModelVO> incomeModels;

    /**
     * 用户资产走势图
     */
    private List<IncomeModelVO> balanceModels;

    /**
     * 用户每小时收益走势图
     */
    private List<IncomeModelVO> benifitModels;

    /**
     *  期货持仓比例
     */
    private List<FirmOfferPositionVo> futurePosition;

    /**
     *  期货多仓占比
     */
    private BigDecimal futureLongAmount;

    /**
     *  期货空仓占比
     */
    private BigDecimal futureShortAmount;

    /**
     * 用户所持币种比例 Key 币种名称 value  持有比例
     * 期货的话  BTC多 29% BTC空 15%
     */
    private List<UserDemoCoinInfoVo> ratioMap;

    /**
     * 个人总资产
     */
    private BigDecimal balance;

    /**
     * 个人期货总资产
     */
    private BigDecimal future;

    /**
     * 个人钱包总资产
     */
    private BigDecimal wallet;

    /**
     * 个人现货总资产
     */
    private BigDecimal stock;

    /**
     * 总盈利 根据初始值和累计收益计算
     */
    private BigDecimal profit;

    /**
     * 个人简介
     */
    private String brief;

    //实盘高手选择币种的简称
    private String coins;

    //操盘天数
    private Integer numberOfDays;

    //现货仓位比例
    private BigDecimal spotRatio;

    /**
     * 用户初始资产
     */
    @JSONField(serialize = false)
    private BigDecimal initBalance = BigDecimal.ZERO;

    @JSONField(serialize = false)
    private Date date;

    private Long id;

    private String slogen;

    private Integer avgOperation;//日均操作


}
