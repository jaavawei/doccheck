package com.iwei.rule.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 测试controller
 *
 * @auther: Wei
 */

@RestController
@RequestMapping("/rule/")
@Slf4j
public class testController {
    @RequestMapping("test")
    public String test() {
        return "success";
    }
}
