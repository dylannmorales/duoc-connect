package cl.duoc.connect.infrastructure.persistence.repository;

import cl.duoc.connect.infrastructure.persistence.entity.RespuestaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RespuestaJpaRepository extends JpaRepository<RespuestaEntity, UUID> {

    @Query("""
            SELECT r FROM RespuestaEntity r
            JOIN FETCH r.usuario
            JOIN FETCH r.pregunta
            WHERE r.id = :id
            """)
    Optional<RespuestaEntity> findByIdWithDetails(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"usuario", "pregunta"})
    @Query(value = """
            SELECT r FROM RespuestaEntity r
            ORDER BY r.createdAt DESC
            """,
            countQuery = "SELECT COUNT(r) FROM RespuestaEntity r")
    Page<RespuestaEntity> findAllForAdmin(Pageable pageable);

    @Query("""
            SELECT r FROM RespuestaEntity r
            JOIN FETCH r.usuario
            WHERE r.pregunta.id = :preguntaId
            ORDER BY r.aceptada DESC, r.votos DESC, r.createdAt ASC
            """)
    List<RespuestaEntity> findByPreguntaIdWithUsuario(@Param("preguntaId") UUID preguntaId);

    long countByPreguntaId(UUID preguntaId);

    @Modifying
    @Query("UPDATE RespuestaEntity r SET r.aceptada = false WHERE r.pregunta.id = :preguntaId")
    void desmarcarTodas(@Param("preguntaId") UUID preguntaId);
}
