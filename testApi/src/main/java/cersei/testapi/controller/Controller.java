package cersei.testapi.controller;

import cersei.common.error.CustomAccessDeniedHandler;
import cersei.common.error.CustomBearerTokenAuthenticationEntryPoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "TestController", description = "Контроллер просто для теститрования")
@RestController
@Slf4j
public class Controller {

    @Operation(summary = "Simple request with authentication, response - String")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Returns 'Hello World'",
            content = {
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "Hello World")
                )
            }
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication problem",
            content = {
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(
                        implementation = CustomAccessDeniedHandler.class,
                        type = "application/json",
                        example = """
                                {
                                    "status": 401,
                                    "error": "Unauthorized",
                                    "message": "Invalid or missing token"
                                }""")
                )
            }
        )
    }
    )
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        log.info("Hello endpoint called");
        return ResponseEntity.ok("Hello World");
    }

    @Operation(summary = "Simple request with authentication and ADMIN role, response - String")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "returns 'Вы одмен'",
                content = {
                        @Content(
                                mediaType = "text/plain",
                                schema = @Schema(type = "string", example = "Вы одмен")
                        )
                }
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication problem",
            content = {
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(
                        implementation = CustomAccessDeniedHandler.class,
                        type = "application/json",
                        example = """
                                {
                                    "status": 401,
                                    "error": "Unauthorized",
                                    "message": "Invalid or missing token"
                                }""")
                )
            }
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Authorization problem",
            content = {
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(
                        implementation = CustomBearerTokenAuthenticationEntryPoint.class,
                        type = "application/json",
                        example = """
                                {
                                    "status": 403,
                                    "error": "Forbidden",
                                    "message": "Access denied"
                                }""")
                )
            }
        )
    })
    @GetMapping("/admin")
    public ResponseEntity<String> admin(){
        log.info("Admin endpoint called");
        return ResponseEntity.ok("Вы одмен");
    }

    @Operation(summary = "Simple request without any authentication, response - String")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "returns 'pong'",
                    content = {
                            @Content(
                                    mediaType = "text/plain",
                                    schema = @Schema(type = "string", example = "pong")
                            )
                    }
            )
    })
    @GetMapping("/public/ping")
    public ResponseEntity<String> ping(){
        log.info("Received ping request");
        return ResponseEntity.ok("pong");
    }
}
