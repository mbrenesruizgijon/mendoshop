package mendoshop.mendoshop.Objetos;

import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
@Component 
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //Variables
    @Column(name = "nombre")
    private String nombre; 

    @Column(name = "email",unique = true)
    private String email; 

    @Column(name = "contrasena")
    private String contrasena;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "CodigoPostal")
    private String codigoPostal;

    @Column(name = "telefono")
    private String telefono;
    
    @Column(name = "activo")
    private boolean admin;

    @OneToMany(mappedBy = "usuario")
    private List<Pedido> pedidos;

}
