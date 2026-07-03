package cl.duoc.connect.presentation.controller;

import cl.duoc.connect.application.dto.request.CreatePreguntaRequest;
import cl.duoc.connect.application.dto.request.CreateRespuestaRequest;
import cl.duoc.connect.application.dto.response.ApiResponse;
import cl.duoc.connect.application.dto.response.PageResponse;
import cl.duoc.connect.application.dto.response.PreguntaResponse;
import cl.duoc.connect.application.dto.response.RespuestaResponse;
import cl.duoc.connect.application.dto.response.VotoResponse;
import cl.duoc.connect.application.service.ForoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/foro")
@RequiredArgsConstructor
@Tag(name = "Foro académico", description = "Preguntas, respuestas y votos")
@SecurityRequirement(name = "bearerAuth")
public class ForoController {

    private final ForoService foroService;

    @GetMapping("/preguntas")
    @Operation(summary = "Listar preguntas del foro")
    public ApiResponse<PageResponse<PreguntaResponse>> listarPreguntas(
            @RequestParam(required = false) UUID carreraId,
            @RequestParam(required = false) UUID asignaturaId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.ok(foroService.listarPreguntas(carreraId, asignaturaId, q, page, size));
    }

    @PostMapping("/preguntas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear pregunta")
    public ApiResponse<PreguntaResponse> crearPregunta(@Valid @RequestBody CreatePreguntaRequest request) {
        return ApiResponse.ok(foroService.crearPregunta(request), "Pregunta publicada");
    }

    @GetMapping("/preguntas/{id}")
    @Operation(summary = "Obtener pregunta con respuestas")
    public ApiResponse<PreguntaResponse> obtenerPregunta(@PathVariable UUID id) {
        return ApiResponse.ok(foroService.obtenerPregunta(id));
    }

    @PostMapping("/preguntas/{id}/respuestas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Responder una pregunta")
    public ApiResponse<RespuestaResponse> responder(
            @PathVariable UUID id,
            @Valid @RequestBody CreateRespuestaRequest request) {
        return ApiResponse.ok(foroService.responderPregunta(id, request), "Respuesta publicada");
    }

    @PostMapping("/preguntas/{id}/votar")
    @Operation(summary = "Votar pregunta (toggle)")
    public ApiResponse<VotoResponse> votarPregunta(@PathVariable UUID id) {
        return ApiResponse.ok(foroService.votarPregunta(id));
    }

    @PostMapping("/respuestas/{id}/votar")
    @Operation(summary = "Votar respuesta (toggle)")
    public ApiResponse<VotoResponse> votarRespuesta(@PathVariable UUID id) {
        return ApiResponse.ok(foroService.votarRespuesta(id));
    }

    @PatchMapping("/respuestas/{id}/aceptar")
    @Operation(summary = "Marcar respuesta como aceptada")
    public ApiResponse<RespuestaResponse> aceptarRespuesta(@PathVariable UUID id) {
        return ApiResponse.ok(foroService.aceptarRespuesta(id), "Respuesta aceptada");
    }
}
