package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.dao.bean.FirmOfferLedgerHist;
import com.ailu.firmoffer.dao.bean.FirmOfferOrderHist;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description:
 * @author: mr.wang
 * @version: V1.0
 * @date: 2018年12月11日
 */
@Repository("firmOfferLedgerHistExt")
@Mapper
public interface FirmOfferLedgerHistExt {

    @Insert("<script>" +
            "<foreach collection=\"list\" item=\"item1\" index=\"index\" open=\"\" close='' separator=';'> " +
            "INSERT INTO firm_offer_ledger_hist" +
            "(ledger_id, exchange, currency, balance, amount, type_name, fee, `timestamp`,user_id,type) VALUES" +
            "(#{item1.ledgerId},#{item1.exchange},#{item1.currency},#{item1.balance},#{item1.amount}," +
            "#{item1.typeName},#{item1.fee},#{item1.timestamp},#{item1.userId},#{item1.type})  " +
            "ON DUPLICATE KEY UPDATE " +
            "balance = #{item1.balance},amount = #{item1.amount}," +
            "type_name = #{item1.typeName},fee= #{item1.fee},`timestamp` = #{item1.timestamp}" +
            "</foreach>" +
            "</script>")
    int insertDuplicateUpdate(@Param("list") List<FirmOfferLedgerHist> list);

    @Update("update `firmoffer`.`firm_offer_ledger_hist` set status = '01' where ledger_id in (${list})")
    void updateStatusInLedgerIds(@Param("list") String collect);

    @Select("SELECT * FROM firm_offer_ledger_hist folh LEFT JOIN user_firm_offer ufo ON folh.user_id = ufo.id where ufo.trans_time < folh.timestamp and folh.type_name in (${typeNames}) and status = '${status}'")
    List<FirmOfferLedgerHist> selectByLedgers(@Param("typeNames") String typeNames,@Param("status") String status);
}
