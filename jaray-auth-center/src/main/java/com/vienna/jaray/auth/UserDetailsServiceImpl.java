package com.vienna.jaray.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jaray
 * @date 2020年09月10日 22:29
 * @description: 用户详情服务实现类
 */
@Slf4j
@Component
public class UserDetailsServiceImpl implements UserDetailsService {
	private static final String USERNAME_ADMIN = "admin";

	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("用户登录："+username);
		
		if (StringUtils.isEmpty(username)) {
			return null;
		}
		return new User(username, passwordEncoder.encode("123456"),
				true, true, true, true,
				AuthorityUtils.commaSeparatedStringToAuthorityList("admin,ROLE_USER"));
	}
}
