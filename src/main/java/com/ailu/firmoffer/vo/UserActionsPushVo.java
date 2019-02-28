package com.ailu.firmoffer.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserActionsPushVo {

    private long userId;//用户ID

    private String leaderName;//用户昵称

    private BigDecimal amount;//量

    private String exchange;//交易所

    private BigDecimal price;//价格

    private String symbol;//币种

    private Date time;//时间

    private String type;//类型

    private Date utime;//更新时间

    private int orderStatus;//是否成交

    private String orderId;//订单id

    private int isUpdate;//0入库，1更新

}
