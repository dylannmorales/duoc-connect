package cl.duoc.connect.application.service;

import cl.duoc.connect.application.dto.request.CreatePreguntaRequest;
import cl.duoc.connect.application.dto.request.CreateRespuestaRequest;
import cl.duoc.connect.application.dto.response.PageResponse;
import cl.duoc.connect.application.dto.response.PreguntaResponse;
import cl.duoc.connect.application.dto.response.RespuestaResponse;
import cl.duoc.connect.application.dto.response.VotoResponse;
import cl.duoc.connect.application.mapper.ForoMapper;
import cl.duoc.connect.domain.exception.BusinessException;
import cl.duoc.connect.domain.exception.EntityNotFoundException;
import cl.duoc.connect.infrastructure.persistence.entity.AsignaturaEntity;
import cl.duoc.connect.infrastructure.persistence.entity.CarreraEntity;
import cl.duoc.connect.infrastructure.persistence.entity.PreguntaEntity;
import cl.duoc.connect.infrastructure.persistence.entity.RespuestaEntity;
import cl.duoc.connect.infrastructure.persistence.entity.UsuarioEntity;
import cl.duoc.connect.infrastructure.persistence.entity.VotoPreguntaEntity;
import cl.duoc.connect.infrastructure.persistence.entity.VotoPreguntaId;
import cl.duoc.connect.infrastructure.persistence.entity.VotoRespuestaEntity;
import cl.duoc.connect.infrastructure.persistence.entity.VotoRespuestaId;
import cl.duoc.connect.infrastructure.persistence.repository.AsignaturaJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.CarreraJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.PreguntaJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.RespuestaJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.UsuarioJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.VotoPreguntaJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.VotoRespuestaJpaRepository;
import cl.duoc.connect.infrastructure.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForoService {

    private final PreguntaJpaRepository preguntaRepository;
    private final RespuestaJpaRepository respuestaRepository;
    private final VotoPreguntaJpaRepository votoPreguntaRepository;
    private final VotoRespuestaJpaRepository votoRespuestaRepository;
    private final CarreraJpaRepository carreraRepository;
    private final AsignaturaJpaRepository asignaturaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final ForoMapper foroMapper;

    @Transactional
    public PreguntaResponse crearPregunta(CreatePreguntaRequest request) {
        UsuarioEntity usuario = getCurrentUsuario();

        CarreraEntity carrera = carreraRepository.findById(request.getCarreraId())
                .orElseThrow(() -> new BusinessException("CARRERA_NOT_FOUND", "Carrera no encontrada"));

        AsignaturaEntity asignatura = asignaturaRepository.findById(request.getAsignaturaId())
                .orElseThrow(() -> new BusinessException("ASIGNATURA_NOT_FOUND", "Asignatura no encontrada"));

        if (!asignatura.getCarrera().getId().equals(carrera.getId())) {
            throw new BusinessException("ASIGNATURA_INVALID", "La asignatura no pertenece a la carrera seleccionada");
        }

        PreguntaEntity pregunta = new PreguntaEntity();
        pregunta.setTitulo(request.getTitulo().trim());
        pregunta.setContenido(request.getContenido().trim());
        pregunta.setUsuario(usuario);
        pregunta.setCarrera(carrera);
        pregunta.setAsignatura(asignatura);

        PreguntaEntity saved = preguntaRepository.save(pregunta);
        saved.setUsuario(usuario);
        saved.setCarrera(carrera);
        saved.setAsignatura(asignatura);

        return foroMapper.toPreguntaResponse(saved, false, 0, List.of());
    }

    @Transactional(readOnly = true)
    public PageResponse<PreguntaResponse> listarPreguntas(
            UUID carreraId,
            UUID asignaturaId,
            String q,
            int page,
            int size) {

        UUID usuarioId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String query = StringUtils.hasText(q) ? q.trim() : null;

        Page<PreguntaEntity> result = query == null
                ? preguntaRepository.buscarPreguntas(carreraId, asignaturaId, pageable)
                : preguntaRepository.buscarPreguntasConTexto(carreraId, asignaturaId, query, pageable);

        List<PreguntaResponse> content = result.getContent().stream()
                .map(p -> toPreguntaSummary(p, usuarioId))
                .toList();

        return PageResponse.<PreguntaResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public PreguntaResponse obtenerPregunta(UUID id) {
        UUID usuarioId = getCurrentUserId();
        PreguntaEntity pregunta = findPreguntaWithDetails(id);
        return toPreguntaDetail(pregunta, usuarioId);
    }

    @Transactional
    public RespuestaResponse responderPregunta(UUID preguntaId, CreateRespuestaRequest request) {
        UsuarioEntity usuario = getCurrentUsuario();
        PreguntaEntity pregunta = findPreguntaWithDetails(preguntaId);

        RespuestaEntity respuesta = new RespuestaEntity();
        respuesta.setPregunta(pregunta);
        respuesta.setUsuario(usuario);
        respuesta.setContenido(request.getContenido().trim());

        RespuestaEntity saved = respuestaRepository.save(respuesta);
        saved.setUsuario(usuario);
        saved.setPregunta(pregunta);

        return foroMapper.toRespuestaResponse(saved, false);
    }

    @Transactional
    public VotoResponse votarPregunta(UUID preguntaId) {
        UsuarioEntity usuario = getCurrentUsuario();
        PreguntaEntity pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada"));

        VotoPreguntaId votoId = new VotoPreguntaId(preguntaId, usuario.getId());
        boolean yaVotado = votoPreguntaRepository.existsById(votoId);

        if (yaVotado) {
            votoPreguntaRepository.deleteById(votoId);
            pregunta.setVotos(pregunta.getVotos() - 1);
        } else {
            VotoPreguntaEntity voto = new VotoPreguntaEntity();
            voto.setId(votoId);
            voto.setPregunta(pregunta);
            voto.setUsuario(usuario);
            votoPreguntaRepository.save(voto);
            pregunta.setVotos(pregunta.getVotos() + 1);
        }

        preguntaRepository.save(pregunta);
        return VotoResponse.builder()
                .votos(pregunta.getVotos())
                .votadoPorMi(!yaVotado)
                .build();
    }

    @Transactional
    public VotoResponse votarRespuesta(UUID respuestaId) {
        UsuarioEntity usuario = getCurrentUsuario();
        RespuestaEntity respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        VotoRespuestaId votoId = new VotoRespuestaId(respuestaId, usuario.getId());
        boolean yaVotado = votoRespuestaRepository.existsById(votoId);

        if (yaVotado) {
            votoRespuestaRepository.deleteById(votoId);
            respuesta.setVotos(respuesta.getVotos() - 1);
        } else {
            VotoRespuestaEntity voto = new VotoRespuestaEntity();
            voto.setId(votoId);
            voto.setRespuesta(respuesta);
            voto.setUsuario(usuario);
            votoRespuestaRepository.save(voto);
            respuesta.setVotos(respuesta.getVotos() + 1);
        }

        respuestaRepository.save(respuesta);
        return VotoResponse.builder()
                .votos(respuesta.getVotos())
                .votadoPorMi(!yaVotado)
                .build();
    }

    @Transactional
    public RespuestaResponse aceptarRespuesta(UUID respuestaId) {
        UsuarioEntity usuario = getCurrentUsuario();

        RespuestaEntity respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        PreguntaEntity pregunta = preguntaRepository.findByIdWithDetails(respuesta.getPregunta().getId())
                .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada"));

        if (!pregunta.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("FORBIDDEN", "Solo el autor de la pregunta puede aceptar una respuesta");
        }

        respuestaRepository.desmarcarTodas(pregunta.getId());
        respuesta.setAceptada(true);
        RespuestaEntity saved = respuestaRepository.save(respuesta);
        saved.setUsuario(respuesta.getUsuario());
        saved.setPregunta(pregunta);

        boolean votado = votoRespuestaRepository.existsByRespuestaIdAndUsuarioId(respuestaId, usuario.getId());
        return foroMapper.toRespuestaResponse(saved, votado);
    }

    private PreguntaResponse toPreguntaSummary(PreguntaEntity pregunta, UUID usuarioId) {
        boolean votado = votoPreguntaRepository.existsByPreguntaIdAndUsuarioId(pregunta.getId(), usuarioId);
        long totalRespuestas = respuestaRepository.countByPreguntaId(pregunta.getId());
        return foroMapper.toPreguntaResponse(pregunta, votado, totalRespuestas, null);
    }

    private PreguntaResponse toPreguntaDetail(PreguntaEntity pregunta, UUID usuarioId) {
        boolean votado = votoPreguntaRepository.existsByPreguntaIdAndUsuarioId(pregunta.getId(), usuarioId);
        List<RespuestaResponse> respuestas = respuestaRepository.findByPreguntaIdWithUsuario(pregunta.getId()).stream()
                .map(r -> foroMapper.toRespuestaResponse(
                        r,
                        votoRespuestaRepository.existsByRespuestaIdAndUsuarioId(r.getId(), usuarioId)))
                .toList();

        return foroMapper.toPreguntaResponse(pregunta, votado, respuestas.size(), respuestas);
    }

    private PreguntaEntity findPreguntaWithDetails(UUID id) {
        return preguntaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada"));
    }

    private UUID getCurrentUserId() {
        return getCurrentUsuario().getId();
    }

    private UsuarioEntity getCurrentUsuario() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return usuarioRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }
}
