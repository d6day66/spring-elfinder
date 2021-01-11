package cn.kong.web.controller;

import cn.kong.web.entity.Result;
import cn.kong.web.entity.SysUser;
import cn.kong.web.service.SysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: gh
 * @date: 2021/03/24/0024 19:58
 * @description:
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    private static Logger log = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    SysUserService sysUserService;
    @RequestMapping("/add")
    public Result addUser(SysUser sysUser) {
        sysUserService.insertAdmin(sysUser);
        log.info("插入成功");
        return Result.success(null);
    }
}
