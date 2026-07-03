package cl.duoc.connect.infrastructure.persistence.repository;

import cl.duoc.connect.infrastructure.persistence.entity.PreguntaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PreguntaJpaRepository extends JpaRepository<PreguntaEntity, UUID> {

    @Query("""
            SELECT p FROM PreguntaEntity p
            JOIN FETCH p.usuario
            JOIN FETCH p.carrera
            JOIN FETCH p.asignatura
            WHERE p.id = :id
            """)
    Optional<PreguntaEntity> findByIdWithDetails(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"usuario", "carrera", "asignatura"})
    @Query(value = """
            SELECT p FROM PreguntaEntity p
            WHERE (:carreraId IS NULL OR p.carrera.id = :carreraId)
            AND (:asignaturaId IS NULL OR p.asignatura.id = :asignaturaId)
            """,
            countQuery = """
            SELECT COUNT(p) FROM PreguntaEntity p
            WHERE (:carreraId IS NULL OR p.carrera.id = :carreraId)
            AND (:asignaturaId IS NULL OR p.asignatura.id = :asignaturaId)
            """)
    Page<PreguntaEntity> buscarPreguntas(
            @Param("carreraId") UUID carreraId,
            @Param("asignaturaId") UUID asignaturaId,
            Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "carrera", "asignatura"})
    @Query(value = """
            SELECT p FROM PreguntaEntity p
            WHERE (:carreraId IS NULL OR p.carrera.id = :carreraId)
            AND (:asignaturaId IS NULL OR p.asignatura.id = :asignaturaId)
            AND (LOWER(p.titulo) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.contenido) LIKE LOWER(CONCAT('%', :q, '%')))
            """,
            countQuery = """
            SELECT COUNT(p) FROM PreguntaEntity p
            WHERE (:carreraId IS NULL OR p.carrera.id = :carreraId)
            AND (:asignaturaId IS NULL OR p.asignatura.id = :asignaturaId)
            AND (LOWER(p.titulo) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.contenido) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<PreguntaEntity> buscarPreguntasConTexto(
            @Param("carreraId") UUID carreraId,
            @Param("asignaturaId") UUID asignaturaId,
            @Param("q") String q,
            Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "asignatura"})
    @Query(value = """
            SELECT p FROM PreguntaEntity p
            ORDER BY p.createdAt DESC
            """,
            countQuery = "SELECT COUNT(p) FROM PreguntaEntity p")
    Page<PreguntaEntity> findAllForAdmin(Pageable pageable);
}
