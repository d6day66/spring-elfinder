package cn.kong.web.service;

import cn.kong.web.entity.AuthToken;
import cn.kong.web.entity.SysUser;
import org.springframework.stereotype.Service;

/**
 * @author: gh
 * @date: 2021/03/23/0023 15:37
 * @description:
 */
public interface SysUserService {
    public void insertAdmin(SysUser sysUser);

    public AuthToken login(SysUser sysUser, String  clientId, String clientSecret);
}
