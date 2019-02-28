package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalance;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/23 16:53
 */
@Repository("firmOfferExchangeBalanceExt")
@Mapper
public interface FirmOfferExchangeBalanceExt {

    @Insert("<script>" +
            "INSERT INTO firm_offer_exchange_balance(user_id, ex_change, available,freeze, coin, symbol,amount,type,loan) VALUES" +
            "<foreach collection=\"list\" item=\"item1\" index=\"index\"  separator=\",\">" +
            "(#{item1.userId},#{item1.exChange},#{item1.available},#{item1.freeze},#{item1.coin},#{item1.symbol},#{item1.amount},#{item1.type},#{item1.loan})" +
            "</foreach>" +
            "</script>")
    int exchangeBalanceInsert(@Param("list") List<FirmOfferExchangeBalance> balances);

    @Insert("<script>" +
            "<foreach collection=\"list\" item=\"item1\" index=\"index\" open=\"\" close='' separator=';'> " +
            "INSERT INTO firm_offer_exchange_balance" +
            "(user_id,ex_change,available,freeze,loan,coin,symbol,amount,type,u_time) VALUES" +
            "(#{item1.userId},#{item1.exChange},#{item1.available}," +
            "#{item1.freeze},#{item1.loan}," +
            "#{item1.coin},#{item1.symbol},#{item1.amount}, #{item1.type}, #{item1.uTime}" +
            ")  " +
            "ON DUPLICATE KEY UPDATE " +
            "available = #{item1.available},freeze = #{item1.freeze}," +
            "loan = #{item1.loan},amount = #{item1.amount},u_time = #{item1.uTime}" +
            "</foreach>" +
            "</script>")
    int exchangeBalanceUpdate(@Param("list") List<FirmOfferExchangeBalance> balances);

    @Delete("delete from firm_offer_exchange_balance where id in (${ids})")
    void deleteByIds(@Param("ids") String collect);
}
