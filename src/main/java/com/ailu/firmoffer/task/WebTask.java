package com.ailu.firmoffer.task;

import com.ailu.firmoffer.dao.bean.FirmOfferKey;
import com.ailu.firmoffer.dao.bean.FirmOfferKeyExample;
import com.ailu.firmoffer.dao.bean.FirmOfferPushHist;
import com.ailu.firmoffer.dao.bean.UserFirmOffer;
import com.ailu.firmoffer.dao.mapper.FirmOfferKeyMapper;
import com.ailu.firmoffer.dao.mapper.ext.UserMapperExt;
import com.ailu.firmoffer.service.AccountService;
import com.ailu.firmoffer.service.FirmOfferOrderService;
import com.ailu.firmoffer.util.SendKafkaUtils;
import com.ailu.firmoffer.web.vo.UserDemoPersonalAccountVO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class WebTask {

    @Resource
    private AccountService accountService;

    @Resource
    FirmOfferOrderService offerOrderService;

    @Autowired
    private SendKafkaUtils sendKafkaUtils;

    @Resource
    private FirmOfferKeyMapper firmOfferKeyMapper;

    @Resource
    private UserMapperExt userMapperExt;

    @Resource
    TradeHistoryPushTask tradeHistoryPushTask;

    /*private final static List<Long> userIds = new ArrayList<>();*/


    public void getUserAccountInfo() {
        List<Long> userIds = getUserIds();
        for (Long userId : userIds) {
            UserDemoPersonalAccountVO userAccountInfo = null;
            try {
                userAccountInfo = accountService.getUserAccountInfo(userId);
            } catch (Exception e) {
                log.error("用户:"+ userId.toString()+ "获取用户账户信息出错，错误信息", e);
            }
            if (userAccountInfo != null) {
                sendKafkaUtils.sendFirmOfferMeta(JSONObject.toJSONString(userAccountInfo));
            }
        }
    }

    public void getOperatingHistory() {
        List<Long> userIds = getUserIds();
        for (Long userId : userIds) {
            int showUserId = tradeHistoryPushTask.getshowUserMap(userId.intValue());
            if (showUserId != 1) {
                List<FirmOfferPushHist> list = offerOrderService.getOrderOperatingHist(1, 500, userId);
                if (list != null && list.size() > 0) {
                    for (FirmOfferPushHist firmOfferPushHist : list) {
                        long time = firmOfferPushHist.getTime().getTime();
                        firmOfferPushHist.setMTime(time);
                    }
                    sendKafkaUtils.sendFirmOfferHistory(JSONObject.toJSONString(list));
                }
            }
        }
    }

    private List<Long> getUserIds() {
        FirmOfferKeyExample firmOfferKeyExample = new FirmOfferKeyExample();
        FirmOfferKeyExample.Criteria or = firmOfferKeyExample.or();
        or.andStatusEqualTo(1);
        List<FirmOfferKey> firmOfferKeys = firmOfferKeyMapper.selectByExample(firmOfferKeyExample);
        /* List<Long> userFirmOffers = userMapperExt.getShowUserId();*/
        List<Long> userIds = new ArrayList<>();
        for (FirmOfferKey firmOfferKey : firmOfferKeys) {
            /*  if (userFirmOffers.contains(firmOfferKey.getUserId())) {*/
            userIds.add(firmOfferKey.getUserId());
            /*  }*/
        }
        return userIds;
    }
}
