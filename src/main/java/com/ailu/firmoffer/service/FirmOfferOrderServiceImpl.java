package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferPushHist;
import com.ailu.firmoffer.dao.bean.FirmOfferPushHistExample;
import com.ailu.firmoffer.dao.mapper.FirmOfferPushHistMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/27 18:47
 */
@Slf4j
@Service
public class FirmOfferOrderServiceImpl implements FirmOfferOrderService {

    @Resource
    FirmOfferPushHistMapper firmOfferPushHistMapper;

    @Override
    public List<FirmOfferPushHist> getOrderOperatingHist(int pageNum, int pageSize, Long userId) {
        PageHelper.startPage(pageNum, pageSize);
        FirmOfferPushHistExample firmOfferPushHistExample = new FirmOfferPushHistExample();
        FirmOfferPushHistExample.Criteria or = firmOfferPushHistExample.or();
        or.andUserIdEqualTo(Integer.parseInt(userId.toString()));
        or.andShowStatusEqualTo(0);
        firmOfferPushHistExample.setOrderByClause("time desc");
        return firmOfferPushHistMapper.selectByExample(firmOfferPushHistExample);
    }


}
