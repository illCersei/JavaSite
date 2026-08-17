package cersei.octopusservice.client;

import cersei.octopusservice.dto.fight.FightStartRequest;
import cersei.octopusservice.dto.fight.FightStateDto;
import cersei.octopusservice.exception.FightServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class FightServiceClient {

    private final RestClient fightRestClient;

    public FightStateDto startFight(String accessToken, FightStartRequest request) {
        log.info(
                "FightService start battleId={} source={} dungeonRunId={} roomId={}",
                request.battleId(),
                request.context().source(),
                request.context().dungeonRunId(),
                request.context().dungeonRoomId()
        );
        try {
            FightStateDto response = fightRestClient.post()
                    .uri("/fight/start")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(FightStateDto.class);
            if (response == null) {
                throw new FightServiceException("Fight start returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new FightServiceException(ex.getStatusCode().value(), "Fight start failed: HTTP " + ex.getStatusCode().value());
        }
    }

    // Java only reads state to confirm the final result (finished/result) once a battle is
    // over - the live per-turn /fight/{battleId}/action loop is called directly by the
    // frontend against fightServiceUrl, not proxied through here.
    public FightStateDto getState(String accessToken, String battleId) {
        log.info("FightService get state battleId={}", battleId);
        try {
            FightStateDto response = fightRestClient.get()
                    .uri("/fight/{battleId}/state", battleId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(FightStateDto.class);
            if (response == null) {
                throw new FightServiceException("Fight state returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new FightServiceException(ex.getStatusCode().value(), "Fight state failed: HTTP " + ex.getStatusCode().value());
        }
    }
}