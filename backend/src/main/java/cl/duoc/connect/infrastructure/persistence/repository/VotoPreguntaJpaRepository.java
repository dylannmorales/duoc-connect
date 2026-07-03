package cl.duoc.connect.infrastructure.persistence.repository;

import cl.duoc.connect.infrastructure.persistence.entity.VotoPreguntaEntity;
import cl.duoc.connect.infrastructure.persistence.entity.VotoPreguntaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VotoPreguntaJpaRepository extends JpaRepository<VotoPreguntaEntity, VotoPreguntaId> {

    boolean existsByPreguntaIdAndUsuarioId(UUID preguntaId, UUID usuarioId);
}
