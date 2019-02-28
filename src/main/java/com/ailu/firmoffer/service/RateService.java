package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.mapper.FirmOfferUserRatioLineMapper;
import com.ailu.firmoffer.web.vo.UserRateVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mr.wang
 * @version V1.0
 * @Description NOTE: 收益率计算
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Service
public class RateService {

    public static Map<Long, UserRateVo> todayMaps = new LinkedHashMap<>();

    public static Map<Long, UserRateVo> yesterdayMaps = new LinkedHashMap<>();

    public static Map<Long, UserRateVo> weekMaps = new LinkedHashMap<>();

    public static Map<Long, UserRateVo> monthMaps = new LinkedHashMap<>();

    public static Map<Long, UserRateVo> allMaps = new LinkedHashMap<>();

    public static List<UserRateVo> todayLists = new ArrayList<>();

    public static List<UserRateVo> yesterdayLists = new ArrayList<>();

    public static List<UserRateVo> weekLists = new ArrayList<>();

    public static List<UserRateVo> monthLists = new ArrayList<>();

    public static List<UserRateVo> allLists = new ArrayList<>();

    public List<UserRateVo> getTodayLists() {
        return todayLists;
    }

    public void setTodayLists(List<UserRateVo> todayLists) {
        RateService.todayLists = todayLists;
    }

    public List<UserRateVo> getYesterdayLists() {
        return yesterdayLists;
    }

    public void setYesterdayLists(List<UserRateVo> yesterdayLists) {
        RateService.yesterdayLists = yesterdayLists;
    }

    public List<UserRateVo> getWeekLists() {
        return weekLists;
    }

    public void setWeekLists(List<UserRateVo> weekLists) {
        RateService.weekLists = weekLists;
    }

    public List<UserRateVo> getMonthLists() {
        return monthLists;
    }

    public void setMonthLists(List<UserRateVo> monthLists) {
        RateService.monthLists = monthLists;
    }

    public List<UserRateVo> getAllLists() {
        return allLists;
    }

    public static void setAllLists(List<UserRateVo> allLists) {
        RateService.allLists = allLists;
    }

    public Map<Long, UserRateVo> getTodayMaps() {
        return todayMaps;
    }

    public Map<Long, UserRateVo> getYesterdayMaps() {
        return yesterdayMaps;
    }

    public Map<Long, UserRateVo> getWeekMaps() {
        return weekMaps;
    }

    public Map<Long, UserRateVo> getMonthMaps() {
        return monthMaps;
    }

    public Map<Long, UserRateVo> getAllMaps() {
        return allMaps;
    }

    public void setTodayMaps(Map<Long, UserRateVo> todayMaps) {
        RateService.todayMaps = todayMaps;
    }

    public void setYesterdayMaps(Map<Long, UserRateVo> yesterdayMaps) {
        RateService.yesterdayMaps = yesterdayMaps;
    }

    public void setWeekMaps(Map<Long, UserRateVo> weekMaps) {
        RateService.weekMaps = weekMaps;
    }

    public void setMonthMaps(Map<Long, UserRateVo> monthMaps) {
        RateService.monthMaps = monthMaps;
    }

    public void setAllMaps(Map<Long, UserRateVo> allMaps) {
        RateService.allMaps = allMaps;
    }

    @Resource
    private FirmOfferUserRatioLineMapper firmOfferUserRatioLineMapper;

    /**
     * 保存收益率至收益走势表
     */
    //@Scheduled(cron = "0 0 1 * * *")
    public void addRateLine() {
        try {
            List<UserRateVo> list = getAllLists();
            if (list.isEmpty()) {
                log.error("收益集合为空，此次未保存！");
                return;
            }
            firmOfferUserRatioLineMapper.insertRates(list.stream().map(map -> {
                UserRadioLineVo userVo = new UserRadioLineVo();
                userVo.setRateTime(new Date());
                userVo.setRatio(map.getRatio().multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN));
                userVo.setUserId(map.getUserId());
                return userVo;
            }).collect(Collectors.toList()));
        } catch (Throwable e) {
            log.error("收益集合保存失败！", e);
        }
    }

    @Data
    public class UserRadioLineVo {

        private Date rateTime;

        private BigDecimal ratio;

        private Long userId;

    }

}
