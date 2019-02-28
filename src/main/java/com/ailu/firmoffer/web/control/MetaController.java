package com.ailu.firmoffer.web.control;

import com.ailu.firmoffer.service.AccountService;
import com.ailu.firmoffer.service.UserService;
import com.ailu.firmoffer.web.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 实盘交易系统请求接口
 */
@RestController
@Slf4j
@RequestMapping(path = "/meta", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MetaController {

    @Resource
    private AccountService accountService;

    @Resource
    private UserService userService;


    @RequestMapping(value = "getUserAccountInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResult getUserAccountInfo(Long userId) {
        return WebResult.okResult(accountService.getUserAccountInfo(userId));
    }

    /*@GetMapping(path = "/getAllFirmUsers")
    private WebResult getAllFirmUsers(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "20") int pageSize) {
        return WebResult.okResult(userService.getFirmUsers(pageNum, pageSize));
    }*/

}
