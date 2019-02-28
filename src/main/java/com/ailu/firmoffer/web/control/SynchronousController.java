package com.ailu.firmoffer.web.control;


import com.ailu.firmoffer.dao.bean.FirmOfferKey;
import com.ailu.firmoffer.dao.bean.FirmOfferKeyExample;
import com.ailu.firmoffer.dao.bean.UserFirmOffer;
import com.ailu.firmoffer.dao.bean.UserFirmOfferExample;
import com.ailu.firmoffer.dao.mapper.FirmOfferKeyMapper;
import com.ailu.firmoffer.dao.mapper.UserFirmOfferMapper;
import com.ailu.firmoffer.vo.InformFirmOfferVo;
import com.ailu.firmoffer.web.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 实盘交易系统请求接口
 */
@RestController
@Slf4j
@RequestMapping(path = "/synchronous", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SynchronousController {

    @Resource
    UserFirmOfferMapper userFirmOfferMapper;
    @Resource
    FirmOfferKeyMapper firmOfferKeyMapper;

    @GetMapping("/synchronousUser")
    public WebResult synchronousUser() {
        Map<Long, String> firmOfferExchange = getFirmOfferExchange();
        UserFirmOfferExample userFirmOfferExample = new UserFirmOfferExample();
        List<UserFirmOffer> userFirmOffers = userFirmOfferMapper.selectByExample(userFirmOfferExample);
        List<InformFirmOfferVo> informFirmOfferVos = new ArrayList<>();
        for (UserFirmOffer userFirmOffer : userFirmOffers) {
            InformFirmOfferVo informFirmOfferVo = new InformFirmOfferVo();
            informFirmOfferVo.setImg(userFirmOffer.getAvatar());
            informFirmOfferVo.setLeaderId(userFirmOffer.getId().toString());
            informFirmOfferVo.setLeaderName(userFirmOffer.getNickName());
            informFirmOfferVo.setExchange(firmOfferExchange.get(userFirmOffer.getId()));
            informFirmOfferVo.setLeaderInfo(userFirmOffer.getBrief());
            informFirmOfferVo.setSlogen(userFirmOffer.getSlogen());
            if ("Huobi".equals(firmOfferExchange.get(userFirmOffer.getId()))) {
                informFirmOfferVo.setLeaderType("现货");
            } else {
                informFirmOfferVo.setLeaderType("期货");
            }
            informFirmOfferVos.add(informFirmOfferVo);
        }
        return WebResult.okResult(informFirmOfferVos);
    }

    private Map<Long, String> getFirmOfferExchange() {
        FirmOfferKeyExample firmOfferKeyExample = new FirmOfferKeyExample();
        List<FirmOfferKey> firmOfferKeys = firmOfferKeyMapper.selectByExample(firmOfferKeyExample);
        Map<Long, String> map = new HashedMap();
        for (FirmOfferKey firmOfferKey : firmOfferKeys) {
            map.put(firmOfferKey.getUserId(), firmOfferKey.getExChange());
        }
        return map;
    }


}
