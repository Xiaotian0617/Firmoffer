package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.dao.bean.FirmOfferOrderHist;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/25 13:54
 */
@Repository("firmOfferOrderHistExt")
@Mapper
public interface FirmOfferOrderHistExt {

    @Insert("<script>" +
            "<foreach collection=\"list\" item=\"item1\" index=\"index\" open=\"\" close='' separator=';'> " +
            "INSERT INTO firm_offer_order_hist" +
            "(id,user_id,ex_change,trading_on,type,price,side,amount,field_amount,field_price,order_date,order_status,contract_name,fee,unit_amount,lever_rate) VALUES" +
            "(#{item1.id},#{item1.userId},#{item1.exChange},#{item1.tradingOn},#{item1.type}," +
            "#{item1.price},#{item1.side},#{item1.amount},#{item1.fieldAmount},#{item1.fieldPrice}," +
            "#{item1.orderDate},#{item1.orderStatus},#{item1.contractName},#{item1.fee},#{item1.unitAmount},#{item1.leverRate})  " +
            "ON DUPLICATE KEY UPDATE " +
            "order_status = #{item1.orderStatus},field_amount = #{item1.fieldAmount}," +
            "field_price = #{item1.fieldPrice},type= #{item1.type},price = #{item1.price},side = #{item1.side},order_date = #{item1.orderDate},fee = #{item1.fee},unit_amount = #{item1.unitAmount},lever_rate = #{item1.leverRate}" +
            "</foreach>" +
            "</script>")
    int insertDuplicateUpdate(@Param("list") List<FirmOfferOrderHist> list);

    @Select("select * from firm_offer_match_hist where userId=#{userId} order by time desc")
    List<FirmOfferOrderHist> orderHist(@Param("userId") long userId);

    @Select("select id,user_id as userId,order_status as orderStatus,ex_change as exChange,trading_on as tradingOn,type,price,side,amount,field_amount as fieldAmount,field_price as fieldPrice,order_date as orderDate,contract_name as contractName,fee," +
            "unit_amount as unitAmount,lever_rate as leverRate,utime from firm_offer_order_hist where id in (${ordersId}) order by utime desc limit #{limitNum}")
    List<FirmOfferOrderHist> selectOrdersByOrdersId(@Param("ordersId") String ordersId, @Param("limitNum") Integer limitNum);

    @Select("select DISTINCT(user_id) from firm_offer_order_hist;")
    List<Long> getAllUserId();

    @Select("select create_time from user_firm_offer where id=#{id}")
    Date getUpDateByUserId(@Param("id") Long id);

    @Select("select count(*) from firm_offer_order_hist where user_id=#{userId};")
    Integer countOrdersByUserId(@Param("userId") Long userId);

    @Select("select count(*) from firm_offer_match_hist where user_id=#{userId};")
    Integer countMatchsByUserId(@Param("userId") Long userId);
}
