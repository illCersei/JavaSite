package cersei.profile.service;

import cersei.profile.dto.UserProfileChangeDto;
import cersei.profile.dto.UserProfileCreateDto;
import cersei.profile.dto.UserProfileDto;
import cersei.profile.model.UserProfileModel;
import cersei.profile.repository.UserProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserProfileImpl implements UserProfile{
    private final UserProfileRepository userProfileRepository;
    /**
     * Метод получения профиля через GET. Предположим, что пустого профиля быть не может, так как мы его создаем при регистрации
     * @param userId
     * @return
     */
    @Override
    public UserProfileDto getProfile(UUID userId) {
        //А нужен ли ексцептион
        UserProfileModel userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Профиль не найдет"));

        return new UserProfileDto(
                userProfile.getUserId(),
                userProfile.getNickname(),
                userProfile.getAvatarUrl(),
                userProfile.getCreatedAt()
        );
    }

    /**
     * Смена данных PATCH
     * @param userId
     * @param dto
     * @return
     */
    @Override
    public UserProfileDto changeProfile(UUID userId, UserProfileChangeDto dto) {
        UserProfileModel userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Профиль не найден"));

        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            userProfile.setNickname(dto.getNickname());
        }
        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isEmpty()) {
            userProfile.setAvatarUrl(dto.getAvatarUrl());
        }

        UserProfileModel saved = userProfileRepository.save(userProfile);

        return new UserProfileDto(
                saved.getUserId(),
                saved.getNickname(),
                saved.getAvatarUrl(),
                saved.getCreatedAt()
        );
    }

    /**
     * Создаем профиль, нужно связать с регистрацией
     * @param userId
     * @param dto
     * @return
     */
    @Override
    public UserProfileDto createProfile(UUID userId, UserProfileCreateDto dto) {
        UserProfileModel userProfileModel = new UserProfileModel();

        userProfileModel.setUserId(userId);
        userProfileModel.setNickname(dto.getNickname());
        userProfileModel.setAvatarUrl(dto.getAvatarUrl());
        userProfileModel.setCreatedAt(LocalDateTime.now());

        UserProfileModel saved = userProfileRepository.save(userProfileModel);

        return new UserProfileDto(
                saved.getUserId(),
                saved.getNickname(),
                saved.getAvatarUrl(),
                saved.getCreatedAt()
        );
    }


    /**
     * Создаем пустой профиль, который нам передает кафка при регситрации
     * @param userId приходит в сообщении кафки
     */
    @Override
    public void createEmptyProfile(UUID userId) {
        UserProfileModel userProfileModel = new UserProfileModel();

        userProfileModel.setUserId(userId);
        userProfileModel.setNickname("tempname");
        userProfileModel.setAvatarUrl(null);
        userProfileModel.setCreatedAt(LocalDateTime.now());

        userProfileRepository.save(userProfileModel);
    }

    /**
     * Метод удаления профиля
     * @param userId
     */
    @Override
    public void deleteProfile(UUID userId) {
        UserProfileModel userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Профиль не найдет"));

        userProfileRepository.deleteById(userId);
    }
}
