package cl.duoc.connect.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "votos_pregunta")
@Getter
@Setter
public class VotoPreguntaEntity {

    @EmbeddedId
    private VotoPreguntaId id;

    @MapsId("preguntaId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private PreguntaEntity pregunta;

    @MapsId("usuarioId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
