-- 1. USUARIOS
-- Primero insertamos los usuarios para que los pedidos tengan un dueño válido.
INSERT IGNORE INTO `usuarios` (`id`, `ciudad`, `codigo_postal`, `contrasena`, `direccion`, `email`, `nombre`, `telefono`, `activo`) VALUES
(1, 'Utrera', '41710', 'mendo', 'Calle María Alva 5A', 'miguelangelbrenesorozco@gmail.com', 'mendo', '663458683', 1),
(2, 'Sevilla', '41710', 'juan', 'Calle Mar Menor', 'juan@gmail.com', 'juan', '663458683', 0);

-- 2. PRODUCTOS (Padre)
-- Los productos deben existir antes que sus extensiones (Carnes/Verduras) y antes que los pedidos.
INSERT IGNORE INTO `productos` (`id`, `precio`, `nombre`, `categoria`, `descripcion`, `url_imagen`, `stock`, `peso`, `descuento`) VALUES
(1, 12.99, 'Bistec de res 500g', 'Carne', 'Corte premium de res', 'img/Bistec de res.png', 2, 0.5, 2),
(2, 8.49, 'Pechuga de pollo 400g', 'Carne', 'Filete de pechuga sin piel ni hueso', 'img/Pechuga de pollo.png', 40, 0.4, 0),
(3, 15.75, 'Lomo de cerdo 700g', 'Carne', 'Lomo magro ideal para asar', 'img/Lomo de cerdo.png', 30, 0.7, 0),
(4, 22.9, 'Pierna de cordero 1.2kg', 'Carne', 'Corte jugoso con hueso', 'img/Pierna de cordero.png', 5, 1.2, 20),
(5, 9.99, 'Carne picada de ternera 300g', 'Carne', 'Carne molida fresca', 'img/Carne picada de ternera.png', 48, 0.3, 0),
(6, 1.5, 'Lechuga romana 500g', 'Verdura', 'Fresca y crujiente', 'img/Lechuga romana.png', 100, 0.5, 0),
(7, 10.2, 'Tomate cherry 600g', 'Verdura', 'Tomates dulces', 'img/Tomate cherry.png', 74, 0.6, 5),
(8, 5.1, 'Zanahoria 300g', 'Verdura', 'Rica en betacarotenos', 'img/Zanahoria.png', 59, 0.3, 2),
(9, 1.8, 'Brócoli 700g', 'Verdura', 'Ramilletes frescos', 'img/Brócoli.png', 40, 0.7, 0),
(10, 1.25, 'Cebolla blanca 200g', 'Verdura', 'Perfecta para sofritos', 'img/Cebolla blanca.png', 68, 0.2, 0);

-- 3. CARNES (Hijo - depende de IDs 1-5 de productos)
INSERT IGNORE INTO `carnes` (`id`, `tipo_animal`, `origen`) VALUES
(1, 'Vacuno', 'Argentina'),
(2, 'Pollo', 'España'),
(3, 'Cerdo', 'Alemania'),
(4, 'Cordero', 'Francia'),
(5, 'Vacuno', 'Uruguay');

-- 4. VERDURAS (Hijo - depende de IDs 6-10 de productos)
INSERT IGNORE INTO `verduras` (`id`, `tipo_verdura`, `es_temporada`) VALUES
(6, 'Hoja', 1),
(7, 'Fruto', 1),
(8, 'Raiz', 0),
(9, 'Hoja', 1),
(10, 'Fruto', 0);

-- 5. PEDIDOS
-- Cambiado el ID a 1 (o mantenido en 8 según prefieras) asegurando que usuario_id=2 existe.
INSERT IGNORE INTO `pedidos` (`id`, `direccion_envio`, `fecha_entrega_estimada`, `fecha_pedido`, `total`, `usuario_id`, `estado`) VALUES
(1, 'Calle Mar Menor Sevilla 41710', '2026-01-10 15:47:29', '2026-01-07 15:47:29', 11.24, 2, 'EN_CAMINO');

-- 6. PEDIDO_PRODUCTOS
-- Vinculamos el pedido ID 1 con los productos ID 5 (carne) e ID 10 (verdura).
INSERT IGNORE INTO `pedido_productos` (`pedido_id`, `cantidad`, `producto_id`) VALUES
(1, 1, 5),
(1, 1, 10);