package cl.duoc.connect.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VotoPreguntaId implements Serializable {

    @Column(name = "pregunta_id")
    private UUID preguntaId;

    @Column(name = "usuario_id")
    private UUID usuarioId;
}
