package cl.duoc.connect.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PreguntaResponse {

    private final UUID id;
    private final String titulo;
    private final String contenido;
    private final UUID usuarioId;
    private final String usuarioNombre;
    private final UUID carreraId;
    private final String carreraNombre;
    private final UUID asignaturaId;
    private final String asignaturaNombre;
    private final boolean tematicaGeneral;
    private final String imagenUrl;
    private final Integer votos;
    private final boolean votadoPorMi;
    private final long totalRespuestas;
    private final Instant createdAt;
    private final List<RespuestaResponse> respuestas;
}
