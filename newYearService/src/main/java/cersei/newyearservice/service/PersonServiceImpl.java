package cersei.newyearservice.service;

import cersei.newyearservice.dto.*;
import cersei.newyearservice.model.Gift;
import cersei.newyearservice.model.Person;
import cersei.newyearservice.repository.GiftRepository;
import cersei.newyearservice.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;
    private final GiftRepository giftRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PersonResponse> getAll() {
        return personRepository.findAllWithGifts().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PersonResponse create(PersonRequest request) {
        Person person = new Person();
        person.setName(request.getName());
        person = personRepository.save(person);
        return toResponse(person);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!personRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Человек не найден");
        }
        personRepository.deleteById(id);
    }

    @Override
    @Transactional
    public GiftResponse addGift(Long personId, GiftRequest request) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Человек не найден"));

        Gift gift = new Gift();
        gift.setDescription(request.getDescription());
        gift.setLink(request.getLink());
        gift.setPerson(person);
        gift = giftRepository.save(gift);

        return toGiftResponse(gift);
    }

    @Override
    @Transactional
    public void deleteGift(Long giftId) {
        if (!giftRepository.existsById(giftId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Подарок не найден");
        }
        giftRepository.deleteById(giftId);
    }

    @Override
    @Transactional
    public ClaimResponse claimGift(Long giftId, String username) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Подарок не найден"));

        if (gift.getClaimedBy() != null) {
            return new ClaimResponse(false, "Подарок уже забронирован", gift.getClaimedBy());
        }

        gift.setClaimedBy(username);
        gift.setClaimedAt(LocalDateTime.now());
        giftRepository.save(gift);

        return new ClaimResponse(true, "Подарок успешно забронирован", username);
    }

    @Override
    public GiftResponse getGiftDetails(Long giftId) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Подарок не найден"));
        return toGiftResponse(gift);
    }

    private PersonResponse toResponse(Person person) {
        PersonResponse response = new PersonResponse();
        response.setId(person.getId());
        response.setName(person.getName());
        response.setGifts(person.getGifts().stream()
                .map(this::toGiftResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private GiftResponse toGiftResponse(Gift gift) {
        GiftResponse response = new GiftResponse();
        response.setId(gift.getId());
        response.setDescription(gift.getDescription());
        response.setIsClaimed(gift.getClaimedBy() != null);
        response.setClaimedBy(gift.getClaimedBy());
        response.setLink(gift.getLink());
        return response;
    }
}

