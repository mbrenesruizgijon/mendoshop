package mendoshop.mendoshop.Objetos;
import mendoshop.mendoshop.Estado;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Guardamos solo el ID del producto y la cantidad
    @ElementCollection
    @CollectionTable(name = "pedido_productos", joinColumns = @JoinColumn(name = "pedido_id"))
    @MapKeyColumn(name = "producto_id")
    @Column(name = "cantidad")
    private Map<Long, Integer> productos = new HashMap<>();

    private LocalDateTime fechaPedido;
    private double total;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.EN_CAMINO;

    private String direccionEnvio;
    private LocalDateTime fechaEntregaEstimada;
}


