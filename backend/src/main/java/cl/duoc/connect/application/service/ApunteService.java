package cl.duoc.connect.application.service;

import cl.duoc.connect.application.dto.request.ValorarApunteRequest;
import cl.duoc.connect.application.dto.response.ApunteResponse;
import cl.duoc.connect.application.dto.response.PageResponse;
import cl.duoc.connect.application.mapper.ApunteMapper;
import cl.duoc.connect.application.port.out.FileStoragePort;
import cl.duoc.connect.domain.exception.BusinessException;
import cl.duoc.connect.domain.exception.EntityNotFoundException;
import cl.duoc.connect.infrastructure.persistence.entity.ApunteEntity;
import cl.duoc.connect.infrastructure.persistence.entity.AsignaturaEntity;
import cl.duoc.connect.infrastructure.persistence.entity.CarreraEntity;
import cl.duoc.connect.infrastructure.persistence.entity.UsuarioEntity;
import cl.duoc.connect.infrastructure.persistence.entity.ValoracionApunteEntity;
import cl.duoc.connect.infrastructure.persistence.entity.ValoracionApunteId;
import cl.duoc.connect.infrastructure.persistence.repository.ApunteJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.AsignaturaJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.CarreraJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.UsuarioJpaRepository;
import cl.duoc.connect.infrastructure.persistence.repository.ValoracionApunteJpaRepository;
import cl.duoc.connect.infrastructure.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApunteService {

    private final ApunteJpaRepository apunteRepository;
    private final ValoracionApunteJpaRepository valoracionRepository;
    private final CarreraJpaRepository carreraRepository;
    private final AsignaturaJpaRepository asignaturaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final FileStoragePort fileStoragePort;
    private final ApunteMapper apunteMapper;

    @Transactional
    public ApunteResponse subirApunte(
            String titulo,
            String descripcion,
            UUID carreraId,
            UUID asignaturaId,
            MultipartFile file) {

        UsuarioEntity usuario = getCurrentUsuario();

        if (!StringUtils.hasText(titulo)) {
            throw new BusinessException("VALIDATION_ERROR", "El título es obligatorio");
        }

        CarreraEntity carrera = carreraRepository.findById(carreraId)
                .orElseThrow(() -> new BusinessException("CARRERA_NOT_FOUND", "Carrera no encontrada"));

        AsignaturaEntity asignatura = asignaturaRepository.findByIdWithCarrera(asignaturaId)
                .orElseThrow(() -> new BusinessException("ASIGNATURA_NOT_FOUND", "Asignatura no encontrada"));

        if (!asignatura.getCarrera().getId().equals(carreraId)) {
            throw new BusinessException("ASIGNATURA_INVALID", "La asignatura no pertenece a la carrera seleccionada");
        }

        String archivoUrl = fileStoragePort.storeApuntePdf(file);
        String nombreArchivo = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "apunte.pdf");

        ApunteEntity apunte = new ApunteEntity();
        apunte.setTitulo(titulo.trim());
        apunte.setDescripcion(StringUtils.hasText(descripcion) ? descripcion.trim() : null);
        apunte.setUsuario(usuario);
        apunte.setCarrera(carrera);
        apunte.setAsignatura(asignatura);
        apunte.setArchivoUrl(archivoUrl);
        apunte.setNombreArchivo(nombreArchivo);
        apunte.setTamanoBytes(file.getSize());

        ApunteEntity saved = apunteRepository.save(apunte);
        saved.setUsuario(usuario);
        saved.setCarrera(carrera);
        saved.setAsignatura(asignatura);

        return apunteMapper.toResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<ApunteResponse> buscarApuntes(
            UUID carreraId,
            UUID asignaturaId,
            String q,
            int page,
            int size) {

        UUID usuarioId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String query = StringUtils.hasText(q) ? q.trim() : null;

        Page<ApunteEntity> result = query == null
                ? apunteRepository.buscarApuntes(carreraId, asignaturaId, pageable)
                : apunteRepository.buscarApuntesConTexto(carreraId, asignaturaId, query, pageable);

        List<ApunteResponse> content = result.getContent().stream()
                .map(apunte -> apunteMapper.toResponse(apunte, obtenerMiValoracion(apunte.getId(), usuarioId)))
                .toList();

        return PageResponse.<ApunteResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public ApunteResponse obtenerApunte(UUID id) {
        ApunteEntity apunte = findApunteWithDetails(id);
        return apunteMapper.toResponse(apunte, obtenerMiValoracion(id, getCurrentUserId()));
    }

    @Transactional
    public DownloadFile descargarApunte(UUID id) {
        ApunteEntity apunte = findApunteWithDetails(id);
        apunte.setDescargas(apunte.getDescargas() + 1);
        apunteRepository.save(apunte);

        Resource resource = fileStoragePort.loadAsResource(apunte.getArchivoUrl());
        return new DownloadFile(resource, apunte.getNombreArchivo());
    }

    @Transactional
    public ApunteResponse valorarApunte(UUID id, ValorarApunteRequest request) {
        UsuarioEntity usuario = getCurrentUsuario();
        ApunteEntity apunte = findApunteWithDetails(id);

        var valoracionExistente = valoracionRepository.findByApunteIdAndUsuarioId(id, usuario.getId());

        if (valoracionExistente.isPresent()) {
            ValoracionApunteEntity valoracion = valoracionExistente.get();
            short puntuacionAnterior = valoracion.getPuntuacion();
            valoracion.setPuntuacion(request.getPuntuacion());
            valoracionRepository.save(valoracion);

            recalcularPromedio(apunte, puntuacionAnterior, request.getPuntuacion(), false);
        } else {
            ValoracionApunteEntity valoracion = new ValoracionApunteEntity();
            valoracion.setId(new ValoracionApunteId(id, usuario.getId()));
            valoracion.setApunte(apunte);
            valoracion.setUsuario(usuario);
            valoracion.setPuntuacion(request.getPuntuacion());
            valoracionRepository.save(valoracion);

            recalcularPromedio(apunte, (short) 0, request.getPuntuacion(), true);
        }

        apunteRepository.save(apunte);
        return apunteMapper.toResponse(apunte, request.getPuntuacion());
    }

    private void recalcularPromedio(ApunteEntity apunte, short anterior, short nueva, boolean esNueva) {
        int total = apunte.getTotalValoraciones();
        BigDecimal promedio = apunte.getPromedioValoracion() != null
                ? apunte.getPromedioValoracion()
                : BigDecimal.ZERO;

        if (esNueva) {
            BigDecimal suma = promedio.multiply(BigDecimal.valueOf(total)).add(BigDecimal.valueOf(nueva));
            total += 1;
            apunte.setTotalValoraciones(total);
            apunte.setPromedioValoracion(suma.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        } else {
            BigDecimal suma = promedio.multiply(BigDecimal.valueOf(total))
                    .subtract(BigDecimal.valueOf(anterior))
                    .add(BigDecimal.valueOf(nueva));
            apunte.setPromedioValoracion(suma.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        }
    }

    private Short obtenerMiValoracion(UUID apunteId, UUID usuarioId) {
        return valoracionRepository.findByApunteIdAndUsuarioId(apunteId, usuarioId)
                .map(ValoracionApunteEntity::getPuntuacion)
                .orElse(null);
    }

    private ApunteEntity findApunteWithDetails(UUID id) {
        return apunteRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Apunte no encontrado"));
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

    public record DownloadFile(Resource resource, String filename) {}
}
