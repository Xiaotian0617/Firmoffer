package com.ailu.firmoffer.dao.mapper;

import com.ailu.firmoffer.dao.bean.UserFirmOffer;
import com.ailu.firmoffer.dao.bean.UserFirmOfferExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserFirmOfferMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    long countByExample(UserFirmOfferExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    int deleteByExample(UserFirmOfferExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    int insert(UserFirmOffer record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    int insertSelective(UserFirmOffer record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    List<UserFirmOffer> selectByExample(UserFirmOfferExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    UserFirmOffer selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") UserFirmOffer record, @Param("example") UserFirmOfferExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") UserFirmOffer record, @Param("example") UserFirmOfferExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(UserFirmOffer record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_firm_offer
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(UserFirmOffer record);
}