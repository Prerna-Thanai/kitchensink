package com.kitchensink.service.impl;

import com.kitchensink.entity.Member;
import com.kitchensink.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * The Class AuthServiceImpl.
 *
 * @author prerna
 */
@Service
public class AuthServiceImpl implements UserDetailsService {

    /** The member repository */
    private final MemberRepository memberRepository;

    /**
     * AuthServiceImpl constructor
     *
     * @param memberRepository
     *            the member repository
     */
    public AuthServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Load user by username
     *
     * @param email
     *            the email
     * @return user details
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Member> memberOptional = memberRepository.findByEmailAndActiveTrue(email);
        if (memberOptional.isEmpty()) {
            throw new UsernameNotFoundException("Member with email " + email + " not found");
        }
        Member member = memberOptional.get();
        if (member.isBlocked()) {
            throw new UsernameNotFoundException("Member with email " + email + " is blocked");
        }
        return User.withUsername(email).password(member.getPassword())
                   .authorities(member.getRoles().stream().map(role -> "ROLE_" + role) //spring requires ROLE_ prefix
                   .map(SimpleGrantedAuthority::new).toList()).disabled(!member.isActive()).build();
    }
}
