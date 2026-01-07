package mendoshop.mendoshop.Interfaces;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mendoshop.mendoshop.Objetos.Verdura;

@Repository
public interface VerduraRepository extends JpaRepository<Verdura, Long> {
    
}

