package com.ailu.firmoffer.dao.mapper;

import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalance;
import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalanceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FirmOfferExchangeBalanceMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    long countByExample(FirmOfferExchangeBalanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    int deleteByExample(FirmOfferExchangeBalanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    int insert(FirmOfferExchangeBalance record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    int insertSelective(FirmOfferExchangeBalance record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    List<FirmOfferExchangeBalance> selectByExample(FirmOfferExchangeBalanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    FirmOfferExchangeBalance selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") FirmOfferExchangeBalance record, @Param("example") FirmOfferExchangeBalanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") FirmOfferExchangeBalance record, @Param("example") FirmOfferExchangeBalanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(FirmOfferExchangeBalance record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_exchange_balance
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(FirmOfferExchangeBalance record);

    List<FirmOfferExchangeBalance> selectDistinctUserId();


    List<FirmOfferExchangeBalance> selectByUserId(Long UserId);

}