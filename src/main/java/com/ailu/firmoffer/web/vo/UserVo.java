package com.ailu.firmoffer.web.vo;

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
public class UserVo {

    private Long userId;

    private String nickName;

    private String userPic;

    private BigDecimal balance;

    private BigDecimal ratio;

}
