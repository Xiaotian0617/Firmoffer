package com.ailu.firmoffer.web.control;

import com.ailu.firmoffer.dao.bean.FirmOfferPushHist;
import com.ailu.firmoffer.service.FirmOfferOrderService;
import com.ailu.firmoffer.web.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/28 10:16
 */
@Slf4j
@RestController
@RequestMapping(path = "/order", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OrderController {

    @Resource
    FirmOfferOrderService offerOrderService;

    @RequestMapping(value = "/operating/history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResult getOperatingHistory(Long userId, @RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "500") int pageSize) {
        List<FirmOfferPushHist> list = offerOrderService.getOrderOperatingHist(pageNum, pageSize, userId);
        if (list != null && list.size() > 0) {
            for (FirmOfferPushHist firmOfferPushHist : list) {
                long time = firmOfferPushHist.getTime().getTime();
                firmOfferPushHist.setMTime(time);
            }
            return WebResult.okResult(list);
        }
        return WebResult.failResult(9999);
    }

}
