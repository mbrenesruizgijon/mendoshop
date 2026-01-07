package mendoshop.mendoshop.Objetos;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import mendoshop.mendoshop.TipoAnimal;

@Entity
@Table(name = "carnes")
@PrimaryKeyJoinColumn(name = "id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Carne extends Producto {

    @Enumerated(EnumType.STRING)
    private TipoAnimal tipoAnimal;

    private String origen;

}



