package com.ailu.firmoffer.web.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author mr.wang
 * @version V1.0
 * @Description NOTE:
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Data
public class UserDemoCoinInfoVo {

    private Long userId;//用户ID
    private Long coinId; // 币种ID
    private String symbol; // 币种名称
    private BigDecimal count; // 币数量
    private BigDecimal priceUsd; // 当前单价
    private BigDecimal amount; // 币市值
    private BigDecimal ratio; // 百分比
    private String picUrl; // 币种图标

    @JSONField(serialize = false)
    private Long positionId;

    private Integer positionType;

}
