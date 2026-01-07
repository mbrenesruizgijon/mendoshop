package mendoshop.mendoshop.Servicio;
import mendoshop.mendoshop.Interfaces.*;
import mendoshop.mendoshop.Objetos.Usuario;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UsuarioServicioImplementar implements UsuarioServicio{

    @Autowired
    private UsuarioRepository repository;

    @Override
    public List<Usuario> listarUsuario() {
        return repository.findAll();
    }

    @Override
    public Usuario guardarUsuario(Usuario Usuario) {
        return repository.save(Usuario);
    }

    @Override
    public Usuario actualizarUsuario(Usuario Usuario) {
        return repository.save(Usuario);
    }

    @Override
    public void borrarUsuario(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public Usuario obtenerUsuario(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public boolean existeUsuarioPorEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Usuario findByNombre(String nombre) {
        return repository.findByNombre(nombre);
    }

}
