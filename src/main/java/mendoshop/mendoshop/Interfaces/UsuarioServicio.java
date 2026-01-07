package mendoshop.mendoshop.Interfaces;

import java.util.List;

import mendoshop.mendoshop.Objetos.*;

public interface UsuarioServicio {

    public List<Usuario> listarUsuario();

    public Usuario guardarUsuario(Usuario Usuario);

    public Usuario obtenerUsuario(Integer id);

    public Usuario actualizarUsuario(Usuario Usuario);

    public void borrarUsuario(Integer id);

    public boolean existeUsuarioPorEmail(String email);
    
    public Usuario findByNombre(String nombre);

}
