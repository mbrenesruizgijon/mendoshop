package mendoshop.mendoshop.Interfaces;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mendoshop.mendoshop.Objetos.Pedido;
import mendoshop.mendoshop.Objetos.Usuario;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Integer usuarioId);

    List<Pedido> findByUsuario(Usuario usuario);
    
}

