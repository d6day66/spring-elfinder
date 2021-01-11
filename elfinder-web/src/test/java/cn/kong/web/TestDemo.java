package cn.kong.web;

import cn.kong.web.dao.SysUserDao;
import cn.kong.web.entity.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author: gh
 * @date: 2021/03/22/0022 21:10
 * @description:
 */
@SpringBootTest
public class TestDemo {
    @Autowired
    SysUserDao sysUserDao;
    @Test
    public void test1() {
        SysUser sysUser = new SysUser();
        String gensalt = BCrypt.gensalt();
        String hashpw = BCrypt.hashpw("12345", gensalt);
        sysUser.setUsername("gh");
        sysUser.setPassword(hashpw);
        sysUser.setId("2");
        sysUserDao.insert(sysUser);
        System.out.println("完成");
    }

    @Test
    public void test2() {
        String gensalt = BCrypt.gensalt();
        String hashpw = BCrypt.hashpw("12345", gensalt);
        System.out.println(gensalt);
        System.out.println(hashpw);
    }


    @Test
    public void test3() {
        SysUser sysUser = new SysUser();
        sysUser.setId("1");
        sysUserDao.selectOne(new QueryWrapper<SysUser>().lambda().eq(StringUtils.isNotBlank(sysUser.getId()),SysUser::getId,sysUser.getId()));
    }

    @Test
    public void test4() {
        SysUser sysUser = new SysUser();
        sysUser.setUsername("admin");
        SysUser sysUser1 = sysUserDao.selectUser(sysUser);
        System.out.println(sysUser1);
    }
    @Test
    public void test5() {
        try {
            String hostName = InetAddress.getLocalHost().getHostAddress();
            System.out.println(hostName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
