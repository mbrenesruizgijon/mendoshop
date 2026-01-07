package mendoshop.mendoshop.Objetos;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import mendoshop.mendoshop.TipoVerdura;

@Entity
@Table(name = "verduras")
@PrimaryKeyJoinColumn(name = "id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Verdura extends Producto {

    @Enumerated(EnumType.STRING)
    private TipoVerdura tipoVerdura;

    private Boolean esTemporada;

}


