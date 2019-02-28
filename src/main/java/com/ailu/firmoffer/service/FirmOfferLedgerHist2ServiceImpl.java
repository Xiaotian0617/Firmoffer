package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferLedgerHist;
import com.ailu.firmoffer.dao.mapper.ext.FirmOfferLedgerHistExt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FirmOfferLedgerHist2ServiceImpl implements FirmOfferLedgerHist2Service {
    @Resource
    FirmOfferLedgerHistExt firmOfferLedgerHistExt;

    @Override
    public int insertFirmOfferLedgerHist(FirmOfferLedgerHist firmOfferLedgerHist) {
        List<FirmOfferLedgerHist> firmOfferLedgerHist2s = new ArrayList<>();
        firmOfferLedgerHist2s.add(firmOfferLedgerHist);
        int i = firmOfferLedgerHistExt.insertDuplicateUpdate(firmOfferLedgerHist2s);
        return i;
    }
}
