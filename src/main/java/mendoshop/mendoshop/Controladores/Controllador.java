package mendoshop.mendoshop.Controladores;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import mendoshop.mendoshop.Categoria;
import mendoshop.mendoshop.Estado;
import mendoshop.mendoshop.TipoAnimal;
import mendoshop.mendoshop.TipoVerdura;
import mendoshop.mendoshop.Interfaces.PedidoRepository;
import mendoshop.mendoshop.Interfaces.ProductoRepository;
import mendoshop.mendoshop.Interfaces.UsuarioRepository;
import mendoshop.mendoshop.Interfaces.UsuarioServicio;
import mendoshop.mendoshop.Objetos.Carne;
import mendoshop.mendoshop.Objetos.Pedido;
import mendoshop.mendoshop.Objetos.Producto;
import mendoshop.mendoshop.Objetos.Usuario;
import mendoshop.mendoshop.Objetos.Verdura;

@Controller
public class Controllador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private PedidoRepository pedidoRepo;

    private ObjectMapper mapper = new ObjectMapper();

    // --- MÉTODOS AUXILIARES ---
    private HashMap<Long, Integer> getOrCreateCarrito(HttpSession session) {
        HashMap<Long, Integer> carrito = (HashMap<Long, Integer>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new HashMap<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }

    private void updateCarritoCookie(Map<Long, Integer> carrito, HttpServletResponse response) {
        try {
            String jsonCarrito = mapper.writeValueAsString(carrito);
            String encoded = URLEncoder.encode(jsonCarrito, StandardCharsets.UTF_8);

            Cookie cookie = new Cookie("carrito", encoded);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24 * 30);
            cookie.setHttpOnly(false);

            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<Long, Integer> cargarCarritoDesdeCookie(HttpServletRequest request, HttpSession session) {
        HashMap<Long, Integer> carrito = new HashMap<>();

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("carrito") && !c.getValue().isEmpty()) {
                    try {
                        String decoded = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8);
                        carrito = mapper.readValue(decoded, new TypeReference<HashMap<Long, Integer>>() {
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        session.setAttribute("carrito", carrito);
        return carrito;
    }

    private double calcularTotal(Map<Long, Integer> carrito) {
        double total = 0;
        for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
            Producto p = productoRepo.findById(entry.getKey()).orElse(null);
            if (p != null) {
                double subtotal = p.getPrecio() * p.getPeso() * entry.getValue();
                if (p.esElegibleDescuento())
                    subtotal *= (1 - p.getDescuento() / 100.0);
                total += subtotal;
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    // --- REGISTRO ---
    @GetMapping("/registrarse.html")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registrarse";
    }

    @PostMapping("/registrar")
    public String procesarRegistro(Usuario usuario) {
        if (usuarioServicio.existeUsuarioPorEmail(usuario.getEmail()))
            return "registro_fallido";
        usuarioServicio.guardarUsuario(usuario);
        return "registro_exitoso";
    }

    // --- LOGIN ---
    // --- LOGIN ---
    @RequestMapping("/login")
    public String login(@RequestParam(value = "login", required = false) String[] login,
            HttpSession session, HttpServletRequest request,
            HttpServletResponse response) {

        if (login == null || login.length < 2)
            return "login.html";

        Usuario user = usuarioServicio.findByNombre(login[0]);
        if (user == null || !user.getContrasena().equals(login[1]))
            return "login_fallido.html";

        session.setAttribute("usuario", user);

        // 👉 SI ES ADMIN, REDIRIGIR A ADMIN.HTML
        if (user.isAdmin()) {
            return "redirect:/admin";
        }

        // Cargar carrito desde cookie si existe
        HashMap<Long, Integer> carrito = cargarCarritoDesdeCookie(request, session);

        // Actualizar cookie con los productos actuales
        updateCarritoCookie(carrito, response);

        // Recalcular total
        double total = calcularTotal(carrito);
        session.setAttribute("total", total);

        return "redirect:/";
    }

    // --- INDEX ---
    @GetMapping({ "/", "/index.html" })
    public String index(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null)
            return "login.html";

        List<Producto> productos = productoRepo.findAll(Sort.by("nombre"));
        model.addAttribute("productos", productos);

        String errorMensaje = (String) session.getAttribute("error_carrito");
        Long errorProductoId = (Long) session.getAttribute("error_producto_id");
        if (errorMensaje != null && errorProductoId != null) {
            model.addAttribute("error_carrito", errorMensaje);
            model.addAttribute("error_producto_id", errorProductoId);
            session.removeAttribute("error_carrito");
            session.removeAttribute("error_producto_id");
        }

        return "index.html";
    }

    // --- BUSCAR Y ORDENAR PRODUCTOS ---
    @GetMapping("/buscar")
    public String buscar(@RequestParam("query") String query, HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null)
            return "login.html";
        List<Producto> productos = productoRepo.findByNombreContainingIgnoreCase(query);
        model.addAttribute("productos", productos);
        model.addAttribute("query", query);
        return "index.html";
    }

    @PostMapping("/ordenar")
    public String ordenar(@RequestParam("criterio") String criterio, HttpSession session, Model model) {
        List<Producto> productos = productoRepo.findAll();
        switch (criterio) {
            case "nombre":
                productos.sort(Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER));
                break;
            case "nombre-desc":
                productos.sort((p1, p2) -> p2.getNombre().compareToIgnoreCase(p1.getNombre()));
                break;
            case "precio":
                productos.sort(Comparator.comparingDouble(
                        p -> p.esElegibleDescuento() ? p.aplicarDescuento() : p.getPrecio() * p.getPeso()));
                break;
            case "precio-desc":
                productos.sort((p1, p2) -> Double.compare(
                        p2.esElegibleDescuento() ? p2.aplicarDescuento() : p2.getPrecio() * p2.getPeso(),
                        p1.esElegibleDescuento() ? p1.aplicarDescuento() : p1.getPrecio() * p1.getPeso()));
                break;
            case "categoria":
                productos.sort(Comparator.comparing(Producto::getCategoria));
                break;
            case "descuento":
                productos.sort((p1, p2) -> Boolean.compare(p2.esElegibleDescuento(), p1.esElegibleDescuento()));
                break;
        }
        model.addAttribute("productos", productos);
        model.addAttribute("criterioSeleccionado", criterio);
        session.setAttribute("criterioSeleccionado", criterio);
        return "index.html";
    }

    // --- AGREGAR PRODUCTO (VERSIÓN CORREGIDA) ---
    @PostMapping("/agregar")
    @ResponseBody
    public Map<String, Object> agregarCarrito(@RequestParam Long productoId,
            HttpSession session,
            HttpServletResponse response,
            HttpServletRequest request) {

        Map<String, Object> res = new HashMap<>();

        HashMap<Long, Integer> carrito = (HashMap<Long, Integer>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = cargarCarritoDesdeCookie(request, session);
        }

        Producto producto = productoRepo.findById(productoId).orElse(null);
        if (producto == null) {
            res.put("error", "Producto no encontrado.");
            return res;
        }

        int cantidadActual = carrito.getOrDefault(productoId, 0);
        if (cantidadActual >= producto.getStock()) {
            res.put("error", "Stock máximo alcanzado.");
            return res;
        }

        carrito.put(productoId, cantidadActual + 1);
        session.setAttribute("carrito", carrito);

        double total = calcularTotal(carrito);
        session.setAttribute("total", total);

        updateCarritoCookie(carrito, response);

        res.put("success", true);
        res.put("cantidad", cantidadActual + 1);
        res.put("total", total);

        return res;
    }

    @PostMapping("/vaciar")
    @ResponseBody
    public Map<String, Object> vaciarCarrito(HttpSession session, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        HashMap<Long, Integer> carrito = new HashMap<>();
        session.setAttribute("carrito", carrito);
        session.setAttribute("total", 0.0);

        updateCarritoCookie(carrito, response);

        res.put("success", true);
        res.put("total", 0.0);
        return res;
    }

    // --- AGREGAR PRODUCTO AJAX ---
    @PostMapping("/agregarProducto")
    @ResponseBody
    public Map<String, Object> agregarProductoAjax(@RequestParam("productoId") Long productoId,
            HttpSession session,
            HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        Producto producto = productoRepo.findById(productoId).orElse(null);
        if (producto == null) {
            res.put("error", "Producto no encontrado");
            return res;
        }

        HashMap<Long, Integer> carrito = getOrCreateCarrito(session);
        int cantidadActual = carrito.getOrDefault(productoId, 0);

        if (cantidadActual >= producto.getStock()) {
            res.put("error", "Stock máximo alcanzado");
            return res;
        }

        carrito.put(productoId, cantidadActual + 1);
        session.setAttribute("carrito", carrito);

        // Recalcular total y actualizar cookie
        double total = calcularTotal(carrito);
        session.setAttribute("total", total);
        updateCarritoCookie(carrito, response);

        res.put("total", total);
        res.put("cantidad", cantidadActual + 1);
        res.put("productoId", productoId);

        return res;
    }

    // --- CARGAR CARRITO DESDE SESIÓN / COOKIE ---
    @GetMapping("/carrito")
    public String carrito(HttpSession session, Model model, HttpServletRequest request) {
        HashMap<Long, Integer> carrito = (HashMap<Long, Integer>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            carrito = cargarCarritoDesdeCookie(request, session);
        }

        List<Map.Entry<Producto, Integer>> carritoConProductos = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
            Producto p = productoRepo.findById(entry.getKey()).orElse(null);
            if (p != null)
                carritoConProductos.add(new AbstractMap.SimpleEntry<>(p, entry.getValue()));
        }
        model.addAttribute("carritoConProductos", carritoConProductos);

        double total = calcularTotal(carrito);
        session.setAttribute("total", total);

        return "carrito";
    }

    // --- ELIMINAR PRODUCTO ---
    @PostMapping("/eliminar")
    public String eliminarProductoCarrito(@RequestParam("productoId") Long productoId,
            HttpSession session,
            HttpServletResponse response) {
        HashMap<Long, Integer> carrito = getOrCreateCarrito(session);
        if (!carrito.containsKey(productoId))
            return "redirect:/carrito";

        int cantidad = carrito.get(productoId);
        if (cantidad <= 1)
            carrito.remove(productoId);
        else
            carrito.put(productoId, cantidad - 1);

        session.setAttribute("carrito", carrito);
        double total = calcularTotal(carrito);
        session.setAttribute("total", total);
        updateCarritoCookie(carrito, response);

        return "redirect:/carrito";
    }

    @PostMapping("/finalizar_compra")
    @Transactional
    public String finalizarCompra(HttpSession session, HttpServletResponse response) {
        // 1. Obtener usuario de sesión y RE-VINCULARLO con la base de datos
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null)
            return "redirect:/login";

        // Lo buscamos de nuevo para que Hibernate lo tenga en el contexto actual
        Usuario usuario = usuarioRepository.findById(usuarioSesion.getId()).orElse(null);
        if (usuario == null)
            return "redirect:/login";

        HashMap<Long, Integer> carrito = getOrCreateCarrito(session);
        if (carrito.isEmpty())
            return "redirect:/carrito";

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFechaPedido(LocalDateTime.now());

        // Gestión segura de dirección
        String dir = String.format("%s, %s (%s)",
                usuario.getDireccion(), usuario.getCiudad(), usuario.getCodigoPostal());
        pedido.setDireccionEnvio(dir);

        double totalPedido = 0.0;

        // Importante: No asignes el mapa directamente si da problemas,
        // asegúrate que el objeto Pedido tenga inicializada su lista/mapa.
        Map<Long, Integer> productosPedido = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
            Producto producto = productoRepo.findById(entry.getKey()).orElse(null);

            if (producto != null) {
                // Verificar Stock
                if (producto.getStock() < entry.getValue()) {
                    session.setAttribute("error_carrito", "Stock insuficiente para: " + producto.getNombre());
                    return "redirect:/carrito";
                }

                // Actualizar Stock
                producto.setStock(producto.getStock() - entry.getValue());
                productoRepo.save(producto);

                // Calcular Precio
                double precioFinal = producto.getPrecio();
                if (producto.esElegibleDescuento()) {
                    precioFinal *= (1 - producto.getDescuento() / 100.0);
                }
                totalPedido += (precioFinal * entry.getValue());

                productosPedido.put(producto.getId(), entry.getValue());
            }
        }

        pedido.setProductos(productosPedido);
        pedido.setTotal(Math.round(totalPedido * 100.0) / 100.0);
        pedido.setEstado(Estado.EN_CAMINO); // Asignar un estado por defecto si es Enum

        pedidoRepo.save(pedido);

        // Limpieza de Carrito y Cookies
        session.removeAttribute("carrito");
        session.removeAttribute("total");
        Cookie cookie = new Cookie("carrito", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "Compra_Finalizada"; // El archivo debe ser compra_Finalizada.html
    }

    @GetMapping("/pedidos")
    public String verPedidos(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null)
            return "redirect:/login";

        // Obtenemos los pedidos del usuario
        List<Pedido> pedidos = pedidoRepo.findAll() // o findByUsuario si lo defines
                .stream()
                .filter(p -> p.getUsuario().getId().equals(usuario.getId()))
                .toList();

        // Creamos un Map de productos completos por pedido
        Map<Long, Producto> productosMap = new HashMap<>();
        for (Pedido pedido : pedidos) {
            for (Long idProd : pedido.getProductos().keySet()) {
                Producto producto = productoRepo.findById(idProd).orElse(null);
                if (producto != null) {
                    productosMap.put(idProd, producto);
                }
            }
        }

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("productosMap", productosMap);

        return "pedidos"; // nombre de la vista Thymeleaf
    }

    @GetMapping("/cerrar_sesion")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "login.html";
    }

    // ZONA ADMIN
    @PostMapping("/add")
    public String agregarProducto(
            @RequestParam String nombre,
            @RequestParam double precio,
            @RequestParam Categoria categoria,
            @RequestParam String descripcion,
            @RequestParam("imagen") MultipartFile imagen,
            @RequestParam int stock,
            @RequestParam double peso,
            @RequestParam double descuento,
            @RequestParam(required = false) TipoVerdura tipoVerdura,
            @RequestParam(required = false) Boolean esTemporada,
            @RequestParam(required = false) TipoAnimal tipoAnimal,
            @RequestParam(required = false) String origen) throws IOException {

        Producto producto;

        switch (categoria) {
            case Verdura -> {
                Verdura verdura = new Verdura();
                verdura.setTipoVerdura(tipoVerdura);
                verdura.setEsTemporada(esTemporada);
                producto = verdura;
            }
            case Carne -> {
                Carne carne = new Carne();
                carne.setTipoAnimal(tipoAnimal);
                carne.setOrigen(origen);
                producto = carne;
            }
            default -> throw new IllegalArgumentException("Tipo de producto no válido");
        }

        // Datos generales
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        producto.setDescripcion(descripcion);
        producto.setStock(stock);
        producto.setPeso(peso);
        producto.setDescuento(descuento);

        // Guardar imagen en una carpeta fuera del proyecto
        if (!imagen.isEmpty()) {
            String home = System.getProperty("user.home"); // Carpeta del usuario
            Path imgDir = Paths.get(home, "mendoshop_uploads", "img");
            if (!Files.exists(imgDir)) {
                Files.createDirectories(imgDir);
            }

            String filename = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
            Path filePath = imgDir.resolve(filename);
            imagen.transferTo(filePath.toFile());

            // Ruta accesible desde el navegador
            producto.setUrlImagen("/uploads/img/" + filename);
        }

        productoRepo.save(producto);

        return "redirect:/admin";
    }

    // Modificar producto
    @PostMapping("editar")
    public String editarProducto(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam double precio,
            @RequestParam Categoria categoria,
            @RequestParam String descripcion,
            @RequestParam String urlImagen,
            @RequestParam int stock,
            @RequestParam double peso,
            @RequestParam double descuento) {

        Producto producto = productoRepo.getReferenceById(id); // o getOne(id) según la versión
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        producto.setDescripcion(descripcion);
        producto.setUrlImagen(urlImagen);
        producto.setStock(stock);
        producto.setPeso(peso);
        producto.setDescuento(descuento);

        productoRepo.save(producto);

        return "redirect:/admin";
    }

    // Eliminar producto
    @PostMapping("/delete")
    public String eliminarProducto(@RequestParam Long id) {
        productoRepo.deleteById(id);
        return "redirect:/admin";
    }

    @RequestMapping("/admin")
    public String mostrarProductos(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login"; // No logueado
        }

        if (!usuario.isAdmin()) {
            return "redirect:/"; // No es admin, lo mandamos al home
        }

        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("tiposVerdura", TipoVerdura.values());
        model.addAttribute("tiposAnimal", TipoAnimal.values());
        return "admin";
    }

    // Listar todos los usuarios en la página admin
    @GetMapping("/adminUsuarios")
    public String listarUsuarios(Model model, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login"; // No logueado
        }

        if (!usuario.isAdmin()) {
            return "redirect:/"; // No es admin, lo mandamos al home
        }

        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        return "adminUsuarios"; // nombre del html Thymeleaf
    }

    // Agregar un nuevo usuario
    @PostMapping("/agregarUsuario")
    public String agregarUsuario(@ModelAttribute Usuario usuario) {
        // Podrías encriptar la contraseña aquí si quieres
        usuarioRepository.save(usuario);
        return "redirect:/adminUsuarios";
    }

    // Editar un usuario existente
    @PostMapping("/editarUsuario")
    public String editarUsuario(@ModelAttribute Usuario usuario) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(usuario.getId());
        if (optionalUsuario.isPresent()) {
            Usuario u = optionalUsuario.get();
            u.setNombre(usuario.getNombre());
            u.setEmail(usuario.getEmail());
            if (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
                u.setContrasena(usuario.getContrasena()); // aquí puedes aplicar hash
            }
            u.setDireccion(usuario.getDireccion());
            u.setCiudad(usuario.getCiudad());
            u.setCodigoPostal(usuario.getCodigoPostal());
            u.setTelefono(usuario.getTelefono());
            u.setAdmin(usuario.isAdmin());
            usuarioRepository.save(u);
        }
        return "redirect:/adminUsuarios";
    }

    // Eliminar un usuario
    @PostMapping("/eliminarUsuario")
    public String eliminarUsuario(@RequestParam Integer id) {
        usuarioRepository.deleteById(id);
        return "redirect:/adminUsuarios";
    }

@GetMapping("/adminPedidos")
public String listarPedidos(Model model, HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || !usuario.isAdmin()) return "redirect:/login";

    List<Pedido> pedidos = pedidoRepo.findAll();
    List<Usuario> usuarios = usuarioRepository.findAll();

    Map<Long, Producto> productosMap = new HashMap<>();
    List<Producto> todosLosProds = productoRepo.findAll();
    for (Producto p : todosLosProds) {
        productosMap.put(p.getId(), p);
    }

    model.addAttribute("pedidos", pedidos);
    model.addAttribute("usuarios", usuarios);
    model.addAttribute("productosMap", productosMap); // Esta línea es clave

    return "adminPedidos"; // Sin .html
}

    @PostMapping("/editarPedido")
    public String editarPedido(@RequestParam Long id,
            @RequestParam String direccionEnvio,
            @RequestParam String fechaEntregaEstimada) {
        Optional<Pedido> optionalPedido = pedidoRepo.findById(id);
        if (optionalPedido.isPresent()) {
            Pedido pedido = optionalPedido.get();
            pedido.setDireccionEnvio(direccionEnvio);
            if (!fechaEntregaEstimada.isEmpty()) {
                pedido.setFechaEntregaEstimada(LocalDateTime.parse(fechaEntregaEstimada));
            }
            pedidoRepo.save(pedido);
        }
        return "redirect:/adminPedidos";
    }

    @PostMapping("/eliminarPedido")
    public String eliminarPedido(@RequestParam Long id) {
        pedidoRepo.deleteById(id);
        return "redirect:/adminPedidos";
    }

}
