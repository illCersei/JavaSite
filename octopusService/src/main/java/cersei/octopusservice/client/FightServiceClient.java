package cersei.octopusservice.client;

import cersei.octopusservice.dto.fight.FightResultResponse;
import cersei.octopusservice.dto.fight.FightStartRequest;
import cersei.octopusservice.dto.fight.FightStartResponse;
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

    public FightStartResponse startFight(String accessToken, FightStartRequest request) {
        log.info(
                "FightService start battleId={} source={} dungeonRunId={} roomId={}",
                request.battleId(),
                request.context().source(),
                request.context().dungeonRunId(),
                request.context().dungeonRoomId()
        );
        try {
            FightStartResponse response = fightRestClient.post()
                    .uri("/fight/start")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(FightStartResponse.class);
            if (response == null) {
                throw new FightServiceException("Fight start returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new FightServiceException(ex.getStatusCode().value(), "Fight start failed: HTTP " + ex.getStatusCode().value());
        }
    }

    public FightResultResponse getFightResult(String accessToken, String battleId) {
        log.info("FightService get result battleId={}", battleId);
        try {
            FightResultResponse response = fightRestClient.get()
                    .uri("/fight/result/{battleId}", battleId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(FightResultResponse.class);
            if (response == null) {
                throw new FightServiceException("Fight result returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new FightServiceException(ex.getStatusCode().value(), "Fight result failed: HTTP " + ex.getStatusCode().value());
        }
    }
}