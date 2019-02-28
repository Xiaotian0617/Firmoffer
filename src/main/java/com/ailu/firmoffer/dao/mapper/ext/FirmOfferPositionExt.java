package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.dao.bean.FirmOfferPosition;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/5/2 10:26
 */
public interface FirmOfferPositionExt {

    @Insert("<script>" +
            "<foreach collection=\"list\" item=\"item1\" index=\"index\" open=\"\" close='' separator=';'> " +
            " insert into firm_offer_position (user_id, ex_change, instrument_id,trading_on, \n" +
            "      type, margin_mode, position_date, \n" +
            "      liquidation_price, qty, avail_qty, \n" +
            "      avg_cost, settlement_price, liqui_price, \n" +
            "      pnl_ratio, margin, realized_pnl, \n" +
            "      leverage, utime,current_cost)\n" +
            "    values (#{item1.userId,jdbcType=BIGINT}, #{item1.exChange,jdbcType=VARCHAR}, #{item1.instrumentId,jdbcType=VARCHAR},#{item1.tradingOn,jdbcType=VARCHAR}, \n" +
            "      #{item1.type,jdbcType=VARCHAR}, #{item1.marginMode,jdbcType=VARCHAR}, #{item1.positionDate,jdbcType=TIMESTAMP}, \n" +
            "      #{item1.liquidationPrice,jdbcType=DECIMAL}, #{item1.qty,jdbcType=DECIMAL}, #{item1.availQty,jdbcType=DECIMAL}, \n" +
            "      #{item1.avgCost,jdbcType=DECIMAL}, #{item1.settlementPrice,jdbcType=DECIMAL}, #{item1.liquiPrice,jdbcType=DECIMAL}, \n" +
            "      #{item1.pnlRatio,jdbcType=DECIMAL}, #{item1.margin,jdbcType=DECIMAL}, #{item1.realizedPnl,jdbcType=DECIMAL}, \n" +
            "      #{item1.leverage,jdbcType=VARCHAR}, #{item1.utime,jdbcType=TIMESTAMP}, #{item1.currentCost,jdbcType=TIMESTAMP}) " +
            "ON DUPLICATE KEY UPDATE " +
            " position_date = #{item1.positionDate,jdbcType=TIMESTAMP},\n" +
            "      liquidation_price = #{item1.liquidationPrice,jdbcType=DECIMAL},trading_on = #{item1.tradingOn,jdbcType=VARCHAR},\n" +
            "      qty = #{item1.qty,jdbcType=DECIMAL},\n" +
            "      avail_qty = #{item1.availQty,jdbcType=DECIMAL},\n" +
            "      avg_cost = #{item1.avgCost,jdbcType=DECIMAL},\n" +
            "      settlement_price = #{item1.settlementPrice,jdbcType=DECIMAL},\n" +
            "      liqui_price = #{item1.liquiPrice,jdbcType=DECIMAL},\n" +
            "      pnl_ratio = #{item1.pnlRatio,jdbcType=DECIMAL},\n" +
            "      margin = #{item1.margin,jdbcType=DECIMAL},\n" +
            "      realized_pnl = #{item1.realizedPnl,jdbcType=DECIMAL},\n" +
            "      leverage = #{item1.leverage,jdbcType=VARCHAR},\n" +
            "      utime = #{item1.utime,jdbcType=TIMESTAMP} ,\n" +
            "      current_cost = #{item1.currentCost,jdbcType=TIMESTAMP} " +
            "</foreach>" +
            "</script>")
    int insertFirmOfferPositionUpdate(@Param("list") List<FirmOfferPosition> list);

    @Delete("delete from firm_offer_position where id in (${ids})")
    int delById(@Param("ids") String ids);


}
