package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalanceSnap;

/**
 * @Description:
 * @author: wang chong
 * @version: V1.0
 * @date: 2018/4/27 16:25\
 **/
public interface FirmOfferExchangeBalanceSnapService {

    FirmOfferExchangeBalanceSnap findTotalByUserId(Long userId);

}
