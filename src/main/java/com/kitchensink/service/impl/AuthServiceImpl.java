package com.kitchensink.service.impl;

import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kitchensink.entity.Member;
import com.kitchensink.repository.MemberRepository;

@Service
public class AuthServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    public AuthServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        if (memberOptional.isEmpty()) {
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }
        Member member = memberOptional.get();
        return User.withUsername(email).password(member.getPassword()).authorities(member.getRoles().stream().map(
            SimpleGrantedAuthority::new).toList()).disabled(!member.isActive()).build();
    }
}
