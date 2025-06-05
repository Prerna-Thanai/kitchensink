package com.kitchensink.repository;

import java.util.Optional;

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
    Optional<Member> findByEmail(String email);

    /**
     * Find by phone number.
     *
     * @param phoneNumber
     *            the phone number
     * @return the member
     */
    Optional<Member> findByPhoneNumber(String phoneNumber);

    /**
     * Find by active true
     *
     * @param pageable
     *            the pageable
     * @return member
     */
    Page<Member> findByActiveTrue(Pageable pageable);

    /**
     * Find by email and active true
     *
     * @param email
     *            the email
     * @return optional member
     */
    Optional<Member> findByEmailAndActiveTrue(String email);

}
