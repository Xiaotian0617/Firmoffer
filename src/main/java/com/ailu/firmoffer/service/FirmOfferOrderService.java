package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferPushHist;

import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/27 18:47
 */
public interface FirmOfferOrderService {

    List<FirmOfferPushHist> getOrderOperatingHist(int pageNum, int pageSize, Long userId);

}
