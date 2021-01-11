package cn.kong.web.service;

import cn.kong.web.dao.SysUserDao;
import cn.kong.web.entity.SysRole;
import cn.kong.web.entity.SysUser;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author: gh
 * @date: 2021/03/22/0022 18:17
 * @description:
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private SysUserDao sysUserDao;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        String lowerCaseLogin = username.toLowerCase();
        SysUser sysUser = new SysUser();
        sysUser.setUsername(lowerCaseLogin);
        SysUser sysUserEntity = sysUserDao.selectUser(sysUser);

        if (sysUserEntity == null) {
            throw new UsernameNotFoundException("User" + lowerCaseLogin + "was not found in the database");
        }
        //角色（权限）
//        String list = "admin,user,guest";
//        List<GrantedAuthority> grantedAuthorities1 = AuthorityUtils.commaSeparatedStringToAuthorityList(list);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (SysRole sysRole : sysUserEntity.getSysRoleEntities()) {
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(sysRole.getName());
            grantedAuthorities.add(grantedAuthority);
        }
        return new User(sysUserEntity.getUsername(), sysUserEntity.getPassword(), grantedAuthorities);
    }
}