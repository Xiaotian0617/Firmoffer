package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.dao.bean.UserAccountBalance;
import com.ailu.firmoffer.web.vo.UserAssetRatioVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/24 15:07
 */
@Repository("userAccountExt")
@Mapper
public interface UserAccountExt {

    @Select("SELECT SUM(b.available) available,SUM(b.freeze) freeze,SUM(b.amount) amount,c.symbol " +
            "FROM " +
            "firm_offer_exchange_balance b " +
            "LEFT JOIN firm_offer_coin_contrast c ON b.coin = c.coin " +
            "WHERE " +
            "b.user_id = #{userId} GROUP BY b.coin ORDER BY b.amount DESC , b.coin ASC ")
    List<UserAccountBalance> getUserAccountBalance(@Param("userId") Long userId);

    @Select("SELECT SUM(b.available) available,SUM(b.freeze) freeze,SUM(b.amount) amount,c.symbol " +
            "FROM " +
            "firm_offer_exchange_balance b " +
            "LEFT JOIN firm_offer_coin_contrast c ON b.coin = c.coin " +
            "WHERE " +
            "b.user_id = #{userId} and b.ex_change = #{exchange} GROUP BY b.coin ORDER BY b.amount DESC , b.coin ASC ")
    List<UserAccountBalance> getUserAccountExchangeBalance(@Param("userId") Long userId, @Param("exchange") String exchange);

    @Select("SELECT b.coin,b.symbol,b.amount,b.ex_change as exchange " +
            "FROM " +
            "firm_offer_exchange_balance b " +
            "WHERE " +
            "b.user_id = #{userId}")
    List<UserAssetRatioVo> getUserCoinAmount(@Param("userId") Long userId);

    @Select("SELECT balance from user_init where user_id = #{userId}")
    BigDecimal getInitUserBalanceByUserId(@Param("userId") Long userId);

    @Select("SELECT b.coin,b.symbol,b.amount,b.ex_change as exchange " +
            "FROM " +
            "firm_offer_exchange_balance b " +
            "WHERE " +
            "b.user_id = #{userId} and b.type = #{type}")
    List<UserAssetRatioVo> getUserCoinAmountByUserIdAndType(@Param("userId") Long userId,@Param("type")  String type);
}
