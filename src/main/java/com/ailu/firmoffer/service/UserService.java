package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferLedgerHist;
import com.ailu.firmoffer.dao.bean.UserFirmOffer;
import com.ailu.firmoffer.web.vo.UserRateVo;
import com.ailu.firmoffer.web.vo.UserVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author mr.wang
 * @version V1.0
 * @Description NOTE:
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface UserService {

    UserFirmOffer getUserByUserId(Long userId);

    /**
     * 根据用户的累积收益排名 查询用户
     *
     * @return
     */
    List<UserRateVo> getUserByRateRanking();

    /**
     * 根据用户的今日收益排名 查询用户
     *
     * @return
     */
    List<UserRateVo> getUserByTodayRateRanking();

    /**
     * 根据用户的昨日收益排名 查询用户
     *
     * @return
     */
    List<UserRateVo> getUserByYesterdayRateRanking();

    /**
     * 根据用户的周收益排名 查询用户
     *
     * @return
     */
    List<UserRateVo> getUserByWeekRateRanking();

    /**
     * 根据用户的月收益排名 查询用户
     *
     * @return
     */
    List<UserRateVo> getUserByMonthRateRanking();


    List<UserVo> getFirmUsers(int pageNum, int pageSize);

    void updateUserInit(Long userId, BigDecimal price);

    void updateUserInit(FirmOfferLedgerHist firmOfferLedgerHist);

    void updateUserInit(Long userId, List<FirmOfferLedgerHist> price);
}
