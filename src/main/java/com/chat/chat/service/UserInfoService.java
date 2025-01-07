package com.chat.chat.service;

import com.chat.chat.common.exception.CustomException;
import com.chat.chat.common.responseEnums.ErrorTypes;
import com.chat.chat.dto.response.MemberResponse;
import com.chat.chat.entity.Member;
import com.chat.chat.repository.CustomRepository;
import com.chat.chat.repository.MemberRepository;
import com.chat.chat.repository.RepositorySelector;
import com.chat.chat.repository.redis.RedisMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    private final MemberRepository memberRepo;
    private final RedisMemberRepository redisMemberRepo;
    private final RepositorySelector repoSelector;
    private final CustomRepository customMemberRepo;

    /**
     * redis 에 있는지 확인 , 없으면 데이터 베이스 , 데이터 베이스에도 없으면 에러
     */
    public Mono<MemberResponse> getUserInfo(String memberId) {
        return repoSelector.selectRepo(memberId)
                .flatMap(repo->repo.findMemberById(memberId))
                .flatMap(member -> {
                    MemberResponse memberResponse = new MemberResponse();
                    memberResponse.setMemberId(member.getMemberId());
                    memberResponse.setCreateTime(member.getCreatedDate());
                    return Mono.just(memberResponse);
                });
    }


    /**
     * 레디스에 id 있는가? -> 없다 -> 데이터 베이스 탐색 -> 비밀 번호는 맞는가? -> 변경 -> 데이터 베이스 변경후 -> 레디스 업데이트
     * -> 없데이트 안됌 삭제하고 다시 저장하기 선택함
     * - 있다
     * 비밀 번호는 맞는가? ->틀림 -> 에러
     * -맞다 -> 레디스 변경 -> 데이터베이스 변경
     */
    public Mono<Member> updateUserInfo(String memberId, String password, String newPassword) {
        String hashNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        return repoSelector.selectRepo(memberId)
                .flatMap(repo -> repo.findMemberById(memberId))
                .flatMap(member -> {

                    if (BCrypt.checkpw(password, member.getMemberPassword())) {
                        log.info("비밀번호가 일치");


                        return customMemberRepo.updateMemberPassword(member.getMemberId(), hashNewPassword)
                                .flatMap(updateResult -> {
                                    return redisMemberRepo.deleteMember(memberId)
                                            .flatMap(deleteResult -> {
                                                if (deleteResult) {
                                                    log.info("Redis 삭제 성공: memberId {}", memberId);
                                                    member.setMemberPassword(hashNewPassword);
                                                    return redisMemberRepo.saveMember(member)
                                                            .flatMap(saveResult -> {
                                                                if (saveResult) {
                                                                    log.info("Redis 저장 성공: memberId {}", memberId);
                                                                    return Mono.just(member);
                                                                } else {
                                                                    log.error("Redis 저장 실패: memberId {}", memberId);
                                                                    return Mono.error(new CustomException(ErrorTypes.REDIS_SAVE_FAILED.errorMessage));
                                                                }
                                                            });
                                                } else {
                                                    log.error("Redis 삭제 실패: memberId {}", memberId);
                                                    return Mono.error(new CustomException(ErrorTypes.REDIS_DELETE_FAILED.errorMessage));
                                                }
                                            });
                                });
                    } else {
                        log.error("현재 비밀번호가 일치하지 않습니다");
                        return Mono.error(new CustomException(ErrorTypes.NOT_VALID_MEMBER_PASSWORD.errorMessage));
                    }
                });
    }


    /**
     * 어차피 데이터 베이스를 거쳐야함 다른 컬렉션 때문에 먼저 레디스를 탐색하는것은 무의미 하다고 생각
     * 추후에 지워지면 수동으로 마지막에 지우는게 좋을 듯 하다
     */

    public Mono<Void> deleteUserInfo(String memberId) {
        return memberRepo.findByMemberId(memberId)
                .switchIfEmpty(Mono.error(new CustomException(ErrorTypes.NOT_EXIST_MEMBER.errorMessage)))
                .flatMap(member -> {

                    return memberRepo.delete(member)
                            .then(customMemberRepo.updateMessageForDeleteUser(memberId))
                            .then(customMemberRepo.updateRoomForDeleteUser(memberId));
                }).then(deleteInRedis(memberId));
    }

    private Mono<Void> deleteInRedis(String memberId) {
        return redisMemberRepo.existByMemberId(memberId)
                .flatMap(exists -> {
                    if (exists) {
                        return redisMemberRepo.deleteMember(memberId)
                                .flatMap(deleted -> {
                                    if (deleted) {
                                        return Mono.empty();
                                    } else {
                                        return Mono.error(new CustomException(ErrorTypes.FAILED_TO_DELETE_REDIS.errorMessage));
                                    }
                                });
                    } else {
                        return Mono.empty();
                    }
                });
    }

}
