package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.dao.bean.FirmOfferPushHist;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("FirmOfferPushHistExt")
@Mapper
public interface FirmOfferPushHistExt {

    @Insert("<script>" +
            "<foreach collection=\"list\" item=\"item1\" index=\"index\" open=\"\" close='' separator=';'> " +
            "INSERT INTO firm_offer_push_hist " +
            "(exchange,price,type,symbol,amount,time,utime,user_id,order_type,order_id,order_status,show_status) VALUES " +
            "(#{item1.exchange},#{item1.price},#{item1.type},#{item1.symbol},#{item1.amount}," +
            "#{item1.time},#{item1.utime},#{item1.userId},#{item1.orderType},#{item1.orderId},#{item1.orderStatus},#{item1.showStatus})" +
            "</foreach>" +
            "</script>")
    int insertFirmOfferPushHist(@Param("list") List<FirmOfferPushHist> list);

    /*@Select("select * from firm_offer_push_hist where userId=#{userId} order by time desc")
    List<FirmOfferPushHist> selectAll(@Param("userId") Long userId);*/
}
