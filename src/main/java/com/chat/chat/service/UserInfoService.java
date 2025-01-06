package com.chat.chat.service;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.dto.response.MemberResponse;
import com.chat.chat.entity.Member;
import com.chat.chat.repository.CustomMemberRepository;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.redis.RedisMemberRepository;
import com.chat.chat.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.chat.chat.service.MemberService.hashPassword;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    private final MemberRepository memberRepo;
    private final RedisRepository redisRepo;
    private final CustomMemberRepository customMemberRepo;

    /**
     * redis 에 있는지 확인 , 없으면 데이터 베이스 , 데이터 베이스에도 없으면 에러
     * redis 에 user created at 추가하기
     */
    public Mono<MemberResponse> getUserInfo(String memberId) {
        return redisRepo.exists(memberId)
                .flatMap(exists -> {
                    if (exists) {
                        return redisRepo.findMemberById(memberId)
                                .flatMap(memberData -> {
                                    MemberResponse response = new MemberResponse();
                                    response.setMemberId(memberData.getMemberId());
                                    response.setCreateTime(memberData.getCreatedDate());
                                    return Mono.just(response);
                                });
                    } else {
                        return memberRepo.findByMemberId(memberId)
                                .flatMap(memberData -> {
                                    MemberResponse response = new MemberResponse();
                                    response.setMemberId(memberData.getMemberId());
                                    response.setCreateTime(memberData.getCreatedDate());
                                    return Mono.just(response);
                                }).switchIfEmpty(Mono.error(new CustomException("Member not found")));
                    }
                });
    }

    /**
     * 레디스에 id 있는가? -> 없다 -> 데이터 베이스 탐색 -> 비밀 번호는 맞는가? -> 변경 -> 데이터 베이스 변경후 -> 레디스 업데이트
     * - 있다
     * 비밀 번호는 맞는가? ->틀림 -> 에러
     * -맞다 -> 레디스 변경 -> 데이터베이스 변경
     */
    public Mono<Member> updateUserInfo(String memberId, String memberPw, String memberNewPw) {
        String hashNewPw = hashPassword(memberNewPw);
        return redisRepo.exists(memberId)
                .flatMap(exist -> {
                    if (exist) {
                        // 현재 레디스에 아이디가 있음
                        // 원래 비밀번호랑 비교 -> 다르면 (error)
                        return redisRepo.findPasswordByMemberId(memberId)
                                .flatMap(redisPw -> {
                                    if (BCrypt.checkpw(memberPw, redisPw)) {
                                        // 일치 -> 새로운 비번을 레디스와 데이터베이스에 넣기
                                        return redisRepo.updateField(memberId, memberPw, hashNewPw)
                                                .then(customMemberRepo.updateMemberPassword(memberId, hashNewPw));
                                    } else {
                                        log.error("Password Incorrect");
                                        return Mono.error(new CustomException("Password Incorrect"));
                                    }
                                });
                    } else {
                        // 레디스에 아직 반영이 안된 경우
                        return memberRepo.existsByMemberId(memberId)
                                .flatMap(exist2 -> {
                                    if (exist2) {
                                        return memberRepo.findByMemberId(memberId)
                                                .flatMap(dataPw -> {
                                                    if (BCrypt.checkpw(memberPw, dataPw.getMemberPassword())) {
                                                        return customMemberRepo.updateMemberPassword(memberId, hashNewPw);
                                                    } else {
                                                        return Mono.error(new CustomException("Password Incorrect"));
                                                    }
                                                });
                                    } else {
                                        return Mono.error(new CustomException("User not found"));
                                    }
                                });
                    }
                });
    }

    /**
     * 어차피 데이터 베이스를 거쳐야함 다른 컬렉션 때문에 먼저 레디스를 탐색하는것은 무의미 하다고 생각
     * 추후에 지워지면 수동으로 마지막에 지우는게 좋을 듯 하다
     */

    public Mono<Void> deleteUserInfo(String memberId, String memberPassword) {
        return memberRepo.findByMemberId(memberId)
                .switchIfEmpty(Mono.error(new CustomException("User not found")))
                .flatMap(member -> {

                    if (!BCrypt.checkpw(memberPassword, member.getMemberPassword())) {
                        return Mono.error(new CustomException("Invalid password"));
                    }

                    return memberRepo.delete(member)
                            .then(customMemberRepo.updateMessageForDeleteUser(memberId))
                            .then(customMemberRepo.updateRoomForDeleteUser(memberId));
                }).then(deleteInRedis(memberId));
    }

    private Mono<Void> deleteInRedis(String memberId) {
        return redisRepo.exists(memberId)
                .flatMap(exists -> {
                    if (exists) {
                        return redisRepo.deleteMember(memberId)
                                .flatMap(deleted -> {
                                    if (deleted) {
                                        return Mono.empty();
                                    } else {
                                        return Mono.error(new CustomException("Failed to delete member in Redis"));
                                    }
                                });
                    } else {
                        return Mono.empty();
                    }
                });
    }

}
