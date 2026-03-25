package cersei.profile.messaging;

import cersei.profile.service.UserProfileImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaListenerService {

    private final UserProfileImpl profileService;

    @KafkaListener(topics = "user-registration", groupId = "profile-service-group")
    public void listenUserRegistrationEvent(String message) {

        profileService.createEmptyProfile(UUID.fromString(message));
    }
}