package mendoshop.mendoshop.Interfaces;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mendoshop.mendoshop.Objetos.Carne;

@Repository
public interface CarneRepository extends JpaRepository<Carne, Long> {}

