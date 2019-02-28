package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalance;
import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalanceExample;

import java.util.List;

/**
 * @Description:
 * @author: wang chong
 * @version: V1.0
 * @date: 2018/4/27 16:25\
 **/
public interface FirmOfferExchangeBalanceService {

    List<FirmOfferExchangeBalance> findByExample(FirmOfferExchangeBalanceExample example);

}
