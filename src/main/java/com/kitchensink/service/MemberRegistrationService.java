package com.kitchensink.service;

import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.entity.Member;

/**
 * The Interface MemberRegistrationService.
 */
public interface MemberRegistrationService {
	
	/**
     * Register memebers.
     *
     * @param member
     *            the member
	 * @return Member
     * @throws Exception
     *             the Exception
     */
	public Member register(RegisterMemberDto member) throws Exception;

}
