package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalanceSnap;
import com.ailu.firmoffer.dao.mapper.FirmOfferExchangeBalanceSnapMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class FirmOfferExchangeBalanceSnapServiceImpl implements FirmOfferExchangeBalanceSnapService {
    @Resource
    FirmOfferExchangeBalanceSnapMapper firmOfferExchangeBalanceSnapMapper;

    @Override
    public FirmOfferExchangeBalanceSnap findTotalByUserId(Long userId) {
        FirmOfferExchangeBalanceSnap firmOfferExchangeBalanceSnap = new FirmOfferExchangeBalanceSnap();
        firmOfferExchangeBalanceSnap.setUserId(userId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        firmOfferExchangeBalanceSnap.setTimeNumber(Long.valueOf(dateFormat.format(new Date())));
        FirmOfferExchangeBalanceSnap resultData = firmOfferExchangeBalanceSnapMapper.selectByUserId(firmOfferExchangeBalanceSnap);
        return resultData;
    }
}
