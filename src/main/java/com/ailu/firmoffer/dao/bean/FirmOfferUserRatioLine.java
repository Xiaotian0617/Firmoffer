package com.ailu.firmoffer.dao.bean;

import java.math.BigDecimal;
import java.util.Date;

public class FirmOfferUserRatioLine {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column firm_offer_user_ratio_line.id
     *
     * @mbg.generated
     */
    private Long id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column firm_offer_user_ratio_line.user_id
     *
     * @mbg.generated
     */
    private Long userId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column firm_offer_user_ratio_line.ratio
     *
     * @mbg.generated
     */
    private BigDecimal ratio;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column firm_offer_user_ratio_line.rate_time
     *
     * @mbg.generated
     */
    private Date rateTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column firm_offer_user_ratio_line.id
     *
     * @return the value of firm_offer_user_ratio_line.id
     * @mbg.generated
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column firm_offer_user_ratio_line.id
     *
     * @param id the value for firm_offer_user_ratio_line.id
     * @mbg.generated
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column firm_offer_user_ratio_line.user_id
     *
     * @return the value of firm_offer_user_ratio_line.user_id
     * @mbg.generated
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column firm_offer_user_ratio_line.user_id
     *
     * @param userId the value for firm_offer_user_ratio_line.user_id
     * @mbg.generated
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column firm_offer_user_ratio_line.ratio
     *
     * @return the value of firm_offer_user_ratio_line.ratio
     * @mbg.generated
     */
    public BigDecimal getRatio() {
        return ratio;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column firm_offer_user_ratio_line.ratio
     *
     * @param ratio the value for firm_offer_user_ratio_line.ratio
     * @mbg.generated
     */
    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column firm_offer_user_ratio_line.rate_time
     *
     * @return the value of firm_offer_user_ratio_line.rate_time
     * @mbg.generated
     */
    public Date getRateTime() {
        return rateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column firm_offer_user_ratio_line.rate_time
     *
     * @param rateTime the value for firm_offer_user_ratio_line.rate_time
     * @mbg.generated
     */
    public void setRateTime(Date rateTime) {
        this.rateTime = rateTime;
    }
}