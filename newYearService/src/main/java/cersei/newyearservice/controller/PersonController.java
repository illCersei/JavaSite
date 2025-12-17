package cersei.newyearservice.controller;

import cersei.newyearservice.dto.*;
import cersei.newyearservice.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
@Tag(name = "Новый год")
public class PersonController {
    private final PersonService personService;

    @Operation(summary = "Получить всех людей с подарками")
    @GetMapping
    public ResponseEntity<List<PersonResponse>> getAll() {
        return ResponseEntity.ok(personService.getAll());
    }

    @Operation(summary = "Создать человека")
    @PostMapping
    public ResponseEntity<PersonResponse> create(@RequestBody @Valid PersonRequest request) {
        return ResponseEntity.ok(personService.create(request));
    }

    @Operation(summary = "Удалить человека")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить подарок человеку")
    @PostMapping("/{personId}/gifts")
    public ResponseEntity<GiftResponse> addGift(@PathVariable Long personId,
                                                 @RequestBody @Valid GiftRequest request) {
        return ResponseEntity.ok(personService.addGift(personId, request));
    }

    @Operation(summary = "Удалить подарок")
    @DeleteMapping("/gifts/{giftId}")
    public ResponseEntity<Void> deleteGift(@PathVariable Long giftId) {
        personService.deleteGift(giftId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Забронировать подарок")
    @PostMapping("/gifts/{giftId}/claim")
    public ResponseEntity<ClaimResponse> claimGift(@PathVariable Long giftId,
                                                   Authentication authentication) {
        String username = authentication.getName();
        ClaimResponse response = personService.claimGift(giftId, username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить детали подарка (показывает кто забронировал)")
    @GetMapping("/gifts/{giftId}")
    public ResponseEntity<GiftResponse> getGiftDetails(@PathVariable Long giftId) {
        return ResponseEntity.ok(personService.getGiftDetails(giftId));
    }
}

