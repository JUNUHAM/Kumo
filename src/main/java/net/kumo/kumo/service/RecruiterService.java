package net.kumo.kumo.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecruiterService {

    private final UserRepository userRepository;

    /**
     * 유저 정보 불러오기
     * 
     * @param email
     * @return
     */
    public UserEntity getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 유저의 프로필 이미지 경로를 업데이트합니다.
     * 
     * @param email     유저 식별용 이메일
     * @param imagePath 저장된 이미지의 웹 접근 경로
     */
    public void updateProfileImage(String email, String imagePath) {
        // 1. 이메일로 유저 정보를 가져옵니다.
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일을 가진 유저를 찾을 수 없습니다: " + email));

        // 2. 새로운 이미지 경로를 세팅합니다. (엔티티의 setter 사용)
        user.setProfileImage(imagePath);

        // 3. 변경 사항을 저장합니다.
        // @Transactional이 붙어있으면 사실 save를 안 호출해도 감지되어 업데이트되지만,
        // 명시적으로 적어주는 것이 가독성에 좋습니다.
        userRepository.save(user);
    }

}
