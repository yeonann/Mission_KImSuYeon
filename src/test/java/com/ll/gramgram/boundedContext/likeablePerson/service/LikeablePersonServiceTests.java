package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class LikeablePersonServiceTests {
    @Autowired
    private LikeablePersonService likeablePersonService;

    @Test
    @DisplayName("test001")
    void t001() throws Exception {
        // 2번 호감 정보를 가져온다.
        /*
        SELECT *
        FROM likeable_person
        WHERE id = 2;
         */
        LikeablePerson likeablePersonId2 = likeablePersonService.findById(2L).get();

        // 2번 호감을 표시한 인스타 회원을 가져온다.
        // 그 회원의 인스타 아이디는 insta_user3 이다.
        /*
        SELECT *
        FROM insta_member
        WHERE id = 2;
         */
        InstaMember instaMemberInstaUser3 = likeablePersonId2.getFromInstaMember();
        assertThat(instaMemberInstaUser3.getUsername()).isEqualTo("insta_user3");

        // 인스타 아이디가 insta_user3 인 사람이 호감을 표시한 목록
        // 호감은 2가지로 구성되어 있다. : from(호감표시자), to(호감받은자)
        /*
        SELECT *
        FROM likeable_person
        WHERE from_insta_member_id = 2;
         */
        List<LikeablePerson> fromLikeablePeople = instaMemberInstaUser3.getFromLikeablePeople();

        // 특정 회원이 표시한 호감을 반복한다.
        for (LikeablePerson likeablePerson : fromLikeablePeople) {
            // 특정회원(인스타 아이디 insta_user3)은 호감표시자와 같은 사람이다.
            assertThat(instaMemberInstaUser3.getUsername()).isEqualTo(likeablePerson.getFromInstaMember().getUsername());
        }
    }
}
