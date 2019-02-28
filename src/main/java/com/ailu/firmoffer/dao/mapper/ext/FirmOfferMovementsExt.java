package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.dao.bean.FirmOfferMovements;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/26 22:59
 */
@Repository("firmOfferMovementsExt")
@Mapper
public interface FirmOfferMovementsExt {

    @Insert("<script>" +
            "<foreach collection=\"list\" item=\"item1\" index=\"index\" open=\"\" close='' separator=';'> " +
            "INSERT INTO firm_offer_movements" +
            "(id,type,currency,amount,address,fee,state,created_time,updated_time,txid,user_id,ex_change) VALUES" +
            "(#{item1.id},#{item1.type},#{item1.currency},#{item1.amount},#{item1.address},#{item1.fee}," +
            "#{item1.state},#{item1.createdTime},#{item1.updatedTime},#{item1.txid},#{item1.userId},#{item1.exChange}) " +
            "ON DUPLICATE KEY UPDATE " +
            "state = #{item1.state},updated_time = #{item1.updatedTime}," +
            "type = #{item1.type},address = #{item1.address},fee = #{item1.fee},txid = #{item1.txid}    " +
            "</foreach>" +
            "</script>")
    int insertDuplicateUpdate(@Param("list") List<FirmOfferMovements> list);

}
