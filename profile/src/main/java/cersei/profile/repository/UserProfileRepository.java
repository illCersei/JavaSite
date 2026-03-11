package cersei.profile.repository;

import cersei.profile.model.UserProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfileModel, UUID> {
    @Override
    Optional<UserProfileModel> findById(UUID userId);

    @Override
    UserProfileModel save(UserProfileModel userProfileModel);

    @Override
    void deleteById(UUID userId);
}
