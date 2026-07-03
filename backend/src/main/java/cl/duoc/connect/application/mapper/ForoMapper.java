package cl.duoc.connect.application.mapper;

import cl.duoc.connect.application.dto.response.PreguntaResponse;
import cl.duoc.connect.application.dto.response.RespuestaResponse;
import cl.duoc.connect.infrastructure.persistence.entity.PreguntaEntity;
import cl.duoc.connect.infrastructure.persistence.entity.RespuestaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ForoMapper {

    public PreguntaResponse toPreguntaResponse(
            PreguntaEntity entity,
            boolean votadoPorMi,
            long totalRespuestas,
            List<RespuestaResponse> respuestas) {

        return PreguntaResponse.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .contenido(entity.getContenido())
                .usuarioId(entity.getUsuario().getId())
                .usuarioNombre(entity.getUsuario().getNombre())
                .carreraId(entity.getCarrera().getId())
                .carreraNombre(entity.getCarrera().getNombre())
                .asignaturaId(entity.getAsignatura().getId())
                .asignaturaNombre(entity.getAsignatura().getNombre())
                .votos(entity.getVotos())
                .votadoPorMi(votadoPorMi)
                .totalRespuestas(totalRespuestas)
                .createdAt(entity.getCreatedAt())
                .respuestas(respuestas)
                .build();
    }

    public RespuestaResponse toRespuestaResponse(RespuestaEntity entity, boolean votadoPorMi) {
        return RespuestaResponse.builder()
                .id(entity.getId())
                .preguntaId(entity.getPregunta().getId())
                .usuarioId(entity.getUsuario().getId())
                .usuarioNombre(entity.getUsuario().getNombre())
                .contenido(entity.getContenido())
                .votos(entity.getVotos())
                .votadoPorMi(votadoPorMi)
                .aceptada(Boolean.TRUE.equals(entity.getAceptada()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
