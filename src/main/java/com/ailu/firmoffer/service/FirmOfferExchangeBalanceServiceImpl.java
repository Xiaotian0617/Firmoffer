package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalance;
import com.ailu.firmoffer.dao.bean.FirmOfferExchangeBalanceExample;
import com.ailu.firmoffer.dao.mapper.FirmOfferExchangeBalanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class FirmOfferExchangeBalanceServiceImpl implements FirmOfferExchangeBalanceService {
    @Resource
    FirmOfferExchangeBalanceMapper firmOfferExchangeBalanceMapper;

    @Override
    public List<FirmOfferExchangeBalance> findByExample(FirmOfferExchangeBalanceExample example) {
        List<FirmOfferExchangeBalance> firmOfferExchangeBalance = firmOfferExchangeBalanceMapper.selectByExample(example);
        return firmOfferExchangeBalance;
    }
}
