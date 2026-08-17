package octopusService.unit.CatalogService;

import cersei.octopusservice.dto.OctopusSummaryDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.Octopus;
import cersei.octopusservice.model.utils.ElementType;
import cersei.octopusservice.repository.OctopusCatalogRepository;
import cersei.octopusservice.service.OctopusCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CatalogServiceTest {

    @Mock
    private OctopusCatalogRepository octopusRepository;

    @InjectMocks
    OctopusCatalogService octopusCatalogService;

    private Octopus blue;
    private Octopus poison;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                octopusCatalogService,
                "spriteUrlTemplate",
                "image-%d.png"
        );

        blue = new Octopus();
        blue.setId(1);
        blue.setName("Blue Octopus");
        blue.setElementType(ElementType.STORM);
        blue.setTier(1);
        blue.setAttackStat(10);
        blue.setMagicPowerStat(20);
        blue.setArmorStat(30);
        blue.setMagicResistStat(40);
        blue.setSpeedStat(50);

        poison = new Octopus();
        poison.setId(2);
        poison.setName("Poison Octopus");
        poison.setElementType(ElementType.POISON);
        poison.setTier(2);
        poison.setAttackStat(11);
        poison.setMagicPowerStat(20);
        poison.setArmorStat(30);
        poison.setMagicResistStat(40);
        poison.setSpeedStat(50);
    }

    @Test
    void when_GetAll_ReturnAllOctopusCatalogs() {
        when(octopusRepository.findAll()).thenReturn(List.of(blue,poison));

        List<OctopusSummaryDto> result = octopusCatalogService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(blue.getId(), result.get(0).id());
        assertEquals(poison.getId(), result.get(1).id());
        assertEquals(blue.getName(), result.get(0).name());
        assertEquals(poison.getName(), result.get(1).name());
    }

    @Test
    void when_GetByValidId_ReturnOctopus() {
        when(octopusRepository.findById(1)).thenReturn(Optional.of(blue));

        OctopusSummaryDto resultOctopus = octopusCatalogService.getById(1);

        assertNotNull(resultOctopus);
        assertEquals(blue.getId(), resultOctopus.id());
        assertEquals(blue.getName(), resultOctopus.name());
    }

    @Test
    void when_GetByInvalidId_ThrowsOctopusNotFoundException() {
        int notValidId = 3;
        when(octopusRepository.findById(notValidId)).thenReturn(Optional.empty());

        OctopusNotFoundException octopusNotFoundException = assertThrows(
                OctopusNotFoundException.class,
                () -> octopusCatalogService.getById(notValidId)
        );

        assertEquals(OctopusNotFoundException.class, octopusNotFoundException.getClass());
        assertEquals("Octopus not found: " + notValidId, octopusNotFoundException.getMessage());
    }

    @Test
    void when_ToSummary_UsesQuantityAndSpriteTemplate() {
        OctopusSummaryDto result = octopusCatalogService.toSummary(blue, 5);

        assertEquals(5, result.quantity());
        assertEquals("image-1.png", result.imageUrl());
        assertEquals(blue.getAttackStat(), result.attack());
        assertEquals(blue.getElementType().name(), result.elementType());
    }
}

