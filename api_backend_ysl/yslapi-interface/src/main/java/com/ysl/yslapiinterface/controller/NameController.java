package com.ysl.yslapiinterface.controller;

import com.ysl.yslapiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;

/**
 * 名称API
 *
 * @author ysl
 */

@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getNameByGet(String name) {
        return "GET 你的名字是" + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user) { return  "POST 用户名字是" + user.getUsername(); }

}
