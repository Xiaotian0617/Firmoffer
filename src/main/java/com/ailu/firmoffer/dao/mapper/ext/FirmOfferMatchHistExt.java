package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.dao.bean.FirmOfferMatchHist;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/25 13:54
 */
@Repository("FirmOfferMatchHistExt")
@Mapper
public interface FirmOfferMatchHistExt {

    @Insert("<script>" +
            "<foreach collection=\"list\" item=\"item1\" index=\"index\" open=\"\" close='' separator=';'> " +
            "INSERT INTO firm_offer_match_hist" +
            "(id,user_id,order_id,match_id,ex_change,symbol,type,price,source,field_fees,field_amount,match_date,match_type,field_price) VALUES" +
            "(#{item1.id},#{item1.userId},#{item1.orderId},#{item1.matchId},#{item1.exChange},#{item1.symbol},#{item1.type}," +
            "#{item1.price},#{item1.source},#{item1.fieldFees},#{item1.fieldAmount},#{item1.matchDate},#{item1.matchType},#{item1.fieldPrice} ) " +
            "ON DUPLICATE KEY UPDATE " +
            "price = #{item1.price},source = #{item1.source},match_id = #{item1.matchId}," +
            "field_fees = #{item1.fieldFees},field_amount = #{item1.fieldAmount},type = #{item1.type}, field_price = #{item1.fieldPrice},match_date = #{item1.matchDate},order_id = #{item1.orderId}" +
            "</foreach>" +
            "</script>")
    int insertDuplicateUpdate(@Param("list") List<FirmOfferMatchHist> list);

    @Select(" select id,user_id as userId,order_id as orderId,match_id as matchId,ex_change exChange,symbol,type,price,source,field_fees as fieIdFees,field_amount as fieldAmount,match_date as matchDate from  firm_offer_match_hist where id in (${matchsId}) order by utime desc limit #{limitNum}")
    List<FirmOfferMatchHist> selectMatchsByMatchId(@Param("matchsId") String matchsId, @Param("limitNum") int limitNum);

    @Select("select DISTINCT(user_id) as userId,ex_change as exChange from firm_offer_match_hist;")
    List<FirmOfferMatchHist> getAllUserId();
}
