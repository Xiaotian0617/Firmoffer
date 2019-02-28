package com.ailu.firmoffer.dao.mapper.ext;

import com.ailu.firmoffer.domain.CoinContrasts;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/5/8 9:44
 */
@Repository("firmOfferUserInitExt")
@Mapper
public interface FirmOfferUserInitExt {

    @Update("update user_init set balance = ${num} where user_id = ${userId}")
   void updateUserInit(@Param("num") BigDecimal num,@Param("userId") Long userId);


}
