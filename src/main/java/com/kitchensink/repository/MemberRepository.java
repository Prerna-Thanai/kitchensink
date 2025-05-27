package com.kitchensink.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.kitchensink.entity.Member;

/**
 * The Interface MemberRepository.
 *
 * @author prerna
 */
@Repository
public interface MemberRepository extends MongoRepository<Member, String> {

    /**
     * Find by email.
     *
     * @param email
     *            the email
     * @return the member
     */
    Member findByEmail(String email);

    /**
     * Find by phone number.
     *
     * @param phoneNumber
     *            the phone number
     * @return the member
     */
    Member findByPhoneNumber(String phoneNumber);

    Page<Member> findByIsActiveTrue(Pageable pageable);

}
