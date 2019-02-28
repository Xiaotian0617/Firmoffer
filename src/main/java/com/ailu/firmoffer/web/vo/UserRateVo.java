package com.ailu.firmoffer.web.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserRateVo {

    private Long userId;

    private BigDecimal ratio;

}
