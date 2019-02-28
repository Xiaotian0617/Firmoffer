package com.ailu.firmoffer.dao.mapper;

import com.ailu.firmoffer.dao.bean.FirmOfferPosition;
import com.ailu.firmoffer.dao.bean.FirmOfferPositionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FirmOfferPositionMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    long countByExample(FirmOfferPositionExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    int deleteByExample(FirmOfferPositionExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    int insert(FirmOfferPosition record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    int insertSelective(FirmOfferPosition record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    List<FirmOfferPosition> selectByExample(FirmOfferPositionExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    FirmOfferPosition selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") FirmOfferPosition record, @Param("example") FirmOfferPositionExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") FirmOfferPosition record, @Param("example") FirmOfferPositionExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(FirmOfferPosition record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table firm_offer_position
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(FirmOfferPosition record);
}