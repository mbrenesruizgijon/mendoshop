package mendoshop.mendoshop.Objetos;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@Setter
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class Carrito {
    
    //Variables
    private ArrayList<Producto> items;
    private double total;

}
