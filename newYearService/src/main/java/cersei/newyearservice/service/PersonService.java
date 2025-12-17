package cersei.newyearservice.service;

import cersei.newyearservice.dto.*;

import java.util.List;

public interface PersonService {
    List<PersonResponse> getAll();
    PersonResponse create(PersonRequest request);
    void delete(Long id);
    GiftResponse addGift(Long personId, GiftRequest request);
    void deleteGift(Long giftId);
    ClaimResponse claimGift(Long giftId, String username);
    GiftResponse getGiftDetails(Long giftId);
}

