package com.ailu.firmoffer.dao.mapper;

import com.ailu.firmoffer.dao.bean.FirmOfferKey;
import com.ailu.firmoffer.dao.bean.FirmOfferKeyExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FirmOfferKeyMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    long countByExample(FirmOfferKeyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    int deleteByExample(FirmOfferKeyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    int insert(FirmOfferKey record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    int insertSelective(FirmOfferKey record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    List<FirmOfferKey> selectByExample(FirmOfferKeyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    FirmOfferKey selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") FirmOfferKey record, @Param("example") FirmOfferKeyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") FirmOfferKey record, @Param("example") FirmOfferKeyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(FirmOfferKey record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_key
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(FirmOfferKey record);
}