package cl.duoc.connect.infrastructure.persistence.repository;

import cl.duoc.connect.infrastructure.persistence.entity.VotoRespuestaEntity;
import cl.duoc.connect.infrastructure.persistence.entity.VotoRespuestaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VotoRespuestaJpaRepository extends JpaRepository<VotoRespuestaEntity, VotoRespuestaId> {

    boolean existsByRespuestaIdAndUsuarioId(UUID respuestaId, UUID usuarioId);
}
