package mendoshop.mendoshop.Objetos;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import mendoshop.mendoshop.Categoria;
import mendoshop.mendoshop.Interfaces.Descontable;

@Entity
@Table(name = "productos")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode
public abstract class Producto implements Descontable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected double precio;

    protected String nombre;

    @Enumerated(EnumType.STRING)
    protected Categoria categoria;

    protected String descripcion;

    protected String urlImagen;

    protected int stock;

    protected double peso;

    protected double descuento;

    public boolean esElegibleDescuento() {
        return this.descuento > 0;
    }

    public double aplicarDescuento() {
        return (this.peso * this.precio) * (1 - (this.descuento / 100));
    }

    public double getPorcentajeDescuento() {
        return this.descuento;
    }


    @ManyToOne
    @JoinColumn(name = "carne_id")
    private Carne carne;

    @ManyToOne
    @JoinColumn(name = "verdura_id")
    private Verdura verdura;

}


