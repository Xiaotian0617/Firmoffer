package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.util.DateUtil;
import com.ailu.firmoffer.web.vo.IncomeModelVO;
import com.ailu.firmoffer.web.vo.UserRateVo;
import com.ailu.firmoffer.web.vo.UserVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author mr.wang
 * @version V1.0
 * @Description NOTE:
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface UserMapperExt {


    @Select("CALL proc_firm_topn_yield_ratio(#{start},#{end})")
    List<UserRateVo> getProfit(@Param("start") Date start, @Param("end") Date end);

    default List<UserRateVo> getProfitYesterday() {
        return getProfit(DateUtil.getYesterdayStart(), DateUtil.getYesterdayLast());
    }

    default List<UserRateVo> getProfitLastWeek() {
        return getProfit(DateUtil.getWeekStart(), DateUtil.getWeekLast());
    }

    default List<UserRateVo> getProfitLastMonth() {
        return getProfit(DateUtil.getMonthStart(), DateUtil.getMonthLast());
    }

    default List<UserRateVo> getProfitToday() {
        return getProfit(DateUtil.getTodayStart(), DateUtil.getTodayLast());
    }

    default List<UserRateVo> getProfitAll() {
        return getProfit(DateUtil.getFirst(), DateUtil.getLast());
    }

    @Select("select ratio as income,rate_time as ctime from firm_offer_user_ratio_line where user_id = #{userId} order by rate_time asc")
    List<IncomeModelVO> getUserIncomeLine(@Param("userId") Long userId);

    @Select("SELECT id as userId,nick_name as nickName,avatar as userPic from user where id in (${userIds})")
    List<UserVo> getFirmUsers(@Param("userIds") String userIds);

    @Select("select user_id as userId,ratio as ratio from firm_offer_user_ratio_line where rate_time>=#{strTime} and rate_time<=#{endTime} order by rate_time desc")
    List<UserRateVo> getRatioLine(@Param("strTime") Date strTime, @Param("endTime") Date endTime);

    @Select("select id,show_history from user_firm_offer")
    List<Map<String, Integer>> getShowUserId();

    @Select("select total as income,ctime as ctime from firm_offer_exchange_balance_hour where user_id = #{userId} order by ctime asc")
    List<IncomeModelVO> getUserBalance(@Param("userId") Long userId);

    @Select("select total as income,ctime as ctime from firm_offer_exchange_benifit_hour where user_id = #{userId} order by ctime asc")
    List<IncomeModelVO> getUserBenifit(@Param("userId") Long userId);

}
