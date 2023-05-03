package com.ll.gramgram.boundedContext.member.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 전체적으로 트랜잭션 환경을 만들어주기 위해 클래스단에 읽기 전용으로 적용
public class MemberService {
    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    @Transactional // DB 데이터의 생성·변경·삭제는 메서드에 붙여준다. (readOnly = false)
    // 일반 회원가입(소셜 로그인을 통한 회원가입이 아님)
    public RsData<Member> join(String username, String password) {
        // "GRAMGRAM" 해당 회원이 일반회원가입으로 인해 생성되었다는걸 나타내기 위해서
        return join("GRAMGRAM", username, password);
    }

    // 내부 처리함수, 일반회원가입, 소셜로그인을 통한 회원가입(최초 로그인 시 한번만 발생)에서 이 함수를 사용함
    private RsData<Member> join(String providerTypeCode, String username, String password) {
        if (findByUsername(username).isPresent()) {
            return RsData.of("F-1", "해당 아이디(%s)는 이미 사용중입니다.".formatted(username));
        }

        // 소셜 로그인을 통한 회원가입에서는 비번이 없다.
        if (StringUtils.hasText(password)) password = passwordEncoder.encode(password);

        Member member = Member
                .builder()
                .providerTypeCode(providerTypeCode)
                .username(username)
                .password(password)
                .build();

        memberRepository.save(member);

        return RsData.of("S-1", "회원가입이 완료되었습니다.", member);
    }

    // 해당 회원에게 인스타 계정을 연결시킨다.
    // 1:1 관계
    @Transactional
    public void updateInstaMember(Member member, InstaMember instaMember) {
        member.setInstaMember(instaMember);
        memberRepository.save(member); // 여기서 실제로 UPDATE 쿼리 발생
    }

    // 소셜 로그인(카카오, 구글, 네이버) 로그인이 될 때 마다 실행되는 함수
    @Transactional
    public RsData<Member> whenSocialLogin(String providerTypeCode, String username) {
        Optional<Member> opMember = findByUsername(username); // username 예시 : KAKAO__1312319038130912, NAVER__1230812300

        if (opMember.isPresent()) return RsData.of("S-2", "로그인 되었습니다.", opMember.get());

        // 소셜 로그인를 통한 가입시 비번은 없다.
        return join(providerTypeCode, username, ""); // 최초 로그인 시 딱 한번 실행
    }
}
