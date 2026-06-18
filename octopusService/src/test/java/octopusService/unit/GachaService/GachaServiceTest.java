/**
 * Это все нужно переделать, сервис идемпотентности:
 * 1)Очень сложный
 * 2)Дает доп зависимости -> трудно тестировать
 * НУЖНО СДЕЛАТЬ НОВЫЙ ГАЧА СЕРВИС
 * **/

package octopusService.unit.GachaService;

import cersei.octopusservice.client.WalletClient;
import cersei.octopusservice.dto.*;
import cersei.octopusservice.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GachaServiceTest {

    @Mock
    private WalletClient walletClient;

    @Mock
    private OctopusCatalogService octopusCatalogService;

    @Mock
    private OctopusInventoryService octopusInventoryService;

    @Mock
    private IdempotencyService idempotencyService;

    //Не получается делать инджект мокс из-за параметров стоимости и id
    private GachaService gachaService;

    @BeforeEach
    void setUp() {
        long costMinor = 100L;
        int maxOctopusId = 5;
        gachaService = new GachaService(
                walletClient,
                octopusCatalogService,
                octopusInventoryService,
                idempotencyService,
                costMinor,
                maxOctopusId
        );
    }

    @Test
    void when_Spin_WithGoodToken_ReturnsResponse() {
        UUID userId = UUID.randomUUID();
        String token = "Bearer good-token";
        String idempotencyKey = "spin-1";

        when(idempotencyService.run(
                eq(userId),
                eq(GachaService.ACTION_GACHA_SPIN),
                eq(idempotencyKey),
                eq(GachaSpinResponse.class),
                any()
        )).thenAnswer(invocation -> {
            Supplier<GachaSpinResponse> supplier = invocation.getArgument(4);
            return supplier.get();
        });

        when(walletClient.debit(eq(token), any(WalletOperationRequest.class)))
                .thenReturn(new WalletOperationResponse(UUID.randomUUID(), 100L, false));

        OctopusSummaryDto baseOctopus = new OctopusSummaryDto(
                1,
                "Blue Octopus",
                "WATER",
                1,
                "image.png",
                10,
                20,
                30,
                40,
                50,
                0
        );

        when(octopusCatalogService.getById(anyInt()))
                .thenReturn(baseOctopus);

        when(octopusInventoryService.addOne(eq(userId), anyInt()))
                .thenReturn(3);

        GachaSpinResponse response = gachaService.spin(token, userId, idempotencyKey);

        assertNotNull(response);
        assertEquals(idempotencyKey, response.spinId());
        assertEquals(100L, response.balanceMinorAfter());
        assertFalse(response.walletIdempotentReplay());

        assertNotNull(response.octopus());
        assertEquals("Blue Octopus", response.octopus().name());
        assertEquals(3, response.octopus().quantity());

        verify(walletClient).debit(eq(token), any(WalletOperationRequest.class));
        verify(octopusCatalogService).getById(anyInt());
        verify(octopusInventoryService).addOne(eq(userId), anyInt());
        verify(walletClient, never()).creditQuietly(anyString(), any());
    }

    @Test
    void when_Spin_WhenCatalogFails_RefundsAndRethrows() {
        UUID userId = UUID.randomUUID();
        String token = "Bearer good-token";
        String idempotencyKey = "spin-2";

        when(idempotencyService.run(
                eq(userId),
                eq(GachaService.ACTION_GACHA_SPIN),
                eq(idempotencyKey),
                eq(GachaSpinResponse.class),
                any()
        )).thenAnswer(invocation -> {
            Supplier<GachaSpinResponse> supplier = invocation.getArgument(4);
            return supplier.get();
        });

        when(walletClient.debit(eq(token), any(WalletOperationRequest.class)))
                .thenReturn(new WalletOperationResponse(UUID.randomUUID(), 100L, false));

        when(octopusCatalogService.getById(anyInt()))
                .thenThrow(new RuntimeException("catalog failed"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> gachaService.spin(token, userId, idempotencyKey)
        );

        assertEquals("catalog failed", ex.getMessage());

        verify(walletClient).debit(eq(token), any(WalletOperationRequest.class));
        verify(walletClient).creditQuietly(eq(token), any(WalletOperationRequest.class));
        verify(octopusInventoryService, never()).addOne(any(), anyInt());
    }

    @Test
    void when_Spin_WhenWalletDebitFails_DoesNotRefund() {
        UUID userId = UUID.randomUUID();
        String token = "Bearer bad-token";
        String idempotencyKey = "spin-3";

        when(idempotencyService.run(
                eq(userId),
                eq(GachaService.ACTION_GACHA_SPIN),
                eq(idempotencyKey),
                eq(GachaSpinResponse.class),
                any()
        )).thenAnswer(invocation -> {
            Supplier<GachaSpinResponse> supplier = invocation.getArgument(4);
            return supplier.get();
        });

        when(walletClient.debit(eq(token), any(WalletOperationRequest.class)))
                .thenThrow(new RuntimeException("wallet failed"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> gachaService.spin(token, userId, idempotencyKey)
        );

        assertEquals("wallet failed", ex.getMessage());

        verify(walletClient).debit(eq(token), any(WalletOperationRequest.class));
        verify(walletClient, never()).creditQuietly(anyString(), any());
        verifyNoInteractions(octopusCatalogService);
        verifyNoInteractions(octopusInventoryService);
    }

    @Test
    void spin_CallsIdempotencyServiceWithCorrectArguments() {
        UUID userId = UUID.randomUUID();
        String token = "Bearer token";
        String idempotencyKey = "spin-4";

        GachaSpinResponse expected = mock(GachaSpinResponse.class);

        when(idempotencyService.run(
                eq(userId),
                eq(GachaService.ACTION_GACHA_SPIN),
                eq(idempotencyKey),
                eq(GachaSpinResponse.class),
                any()
        )).thenReturn(expected);

        GachaSpinResponse actual = gachaService.spin(token, userId, idempotencyKey);

        assertSame(expected, actual);

        verify(idempotencyService).run(
                eq(userId),
                eq(GachaService.ACTION_GACHA_SPIN),
                eq(idempotencyKey),
                eq(GachaSpinResponse.class),
                any()
        );

        verifyNoInteractions(walletClient);
        verifyNoInteractions(octopusCatalogService);
        verifyNoInteractions(octopusInventoryService);
    }
}