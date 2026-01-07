package mendoshop.mendoshop.Servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import mendoshop.mendoshop.Estado;
import mendoshop.mendoshop.Interfaces.PedidoRepository;
import mendoshop.mendoshop.Objetos.Pedido;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoScheduler {

    @Autowired
    private PedidoRepository pedidoRepository;

    // Se ejecuta cada 24 horas (millis: 1000*60*60*24)
    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void actualizarPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();

        for (Pedido pedido : pedidos) {
            if (pedido.getEstado() == Estado.EN_CAMINO) {
                LocalDateTime fechaPedido = pedido.getFechaPedido();
                if (fechaPedido != null && fechaPedido.plusDays(3).isBefore(LocalDateTime.now())) {
                    pedido.setEstado(Estado.ENTREGADO);
                    pedidoRepository.save(pedido);
                    System.out.println("Pedido #" + pedido.getId() + " cambiado a ENTREGADO");
                }
            }
        }
    }
}
