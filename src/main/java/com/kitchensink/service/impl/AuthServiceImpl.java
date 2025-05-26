package com.kitchensink.service.impl;

import com.kitchensink.entity.Member;
import com.kitchensink.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements UserDetailsService{

    private final MemberRepository memberRepository;

    public AuthServiceImpl(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        Member member = memberRepository.findByEmail(email);
        if(member == null){
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }
        return User.withUsername(email)
                   .password(member.getPassword())
                   .authorities(member.getRoles().stream()
                                      .map(SimpleGrantedAuthority::new)
                                      .toList())
                   .disabled(!member.isActive()).build();
    }
}
