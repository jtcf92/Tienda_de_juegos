-- =========================================================
-- Esquema de la tienda de videojuegos (MySQL y H2)
-- =========================================================

-- 1. EL motivo de porque hago drops individuales es porque H2 no tiene Drop database ni Create Database, por lo que debo
-- eliminarlos 1 a 1 asi que mejor lo aplico a los 2 para armonizarlo
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS movimiento_stock;
DROP TABLE IF EXISTS detalle_compra;
DROP TABLE IF EXISTS compra;
DROP TABLE IF EXISTS inventario;
DROP TABLE IF EXISTS juego_plataforma;
DROP TABLE IF EXISTS juego;
DROP TABLE IF EXISTS plataforma;
DROP TABLE IF EXISTS usuario;

-- 2. Crear las tablas

CREATE TABLE usuario (
    id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(50)  NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    fecha_registro DATETIME     NOT NULL
);

CREATE TABLE plataforma (
    id_plataforma INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE juego (
    id_juego INT AUTO_INCREMENT PRIMARY KEY,
    titulo   VARCHAR(100)  NOT NULL UNIQUE,
    precio   DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    activo   BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE juego_plataforma (
    id_juego      INT NOT NULL,
    id_plataforma INT NOT NULL,
    PRIMARY KEY (id_juego, id_plataforma),
    FOREIGN KEY (id_juego)      REFERENCES juego(id_juego),
    FOREIGN KEY (id_plataforma) REFERENCES plataforma(id_plataforma)
);

CREATE TABLE inventario (
    id_juego     INT PRIMARY KEY,
    stock        INT NOT NULL CHECK (stock >= 0),
    stock_minimo INT NOT NULL CHECK (stock_minimo >= 0),
    FOREIGN KEY (id_juego) REFERENCES juego(id_juego)
);

CREATE TABLE compra (
    id_compra  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT           NOT NULL,
    fecha      DATETIME      NOT NULL,
    estado     VARCHAR(20)   NOT NULL CHECK (estado IN ('PENDIENTE', 'PAGADA', 'CANCELADA')),
    total      DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE detalle_compra (
    id_detalle      INT AUTO_INCREMENT PRIMARY KEY,
    id_compra       INT           NOT NULL,
    id_juego        INT           NOT NULL,
    cantidad        INT           NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    importe         DECIMAL(10,2) NOT NULL CHECK (importe >= 0),
    UNIQUE (id_compra, id_juego),
    FOREIGN KEY (id_compra) REFERENCES compra(id_compra),
    FOREIGN KEY (id_juego)  REFERENCES juego(id_juego)
);

CREATE TABLE movimiento_stock (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_juego      INT         NOT NULL,
    id_compra     INT         NULL,
    tipo          VARCHAR(20) NOT NULL CHECK (tipo IN ('REPOSICION', 'VENTA', 'DEVOLUCION')),
    cantidad      INT         NOT NULL CHECK (cantidad > 0),
    fecha         DATETIME    NOT NULL,
    FOREIGN KEY (id_juego)  REFERENCES juego(id_juego),
    FOREIGN KEY (id_compra) REFERENCES compra(id_compra)
);

CREATE TABLE pago (
    id_pago   INT AUTO_INCREMENT PRIMARY KEY,
    id_compra INT           NOT NULL UNIQUE,
    importe   DECIMAL(10,2) NOT NULL CHECK (importe >= 0),
    estado    VARCHAR(20)   NOT NULL CHECK (estado IN ('PAGADO', 'REEMBOLSADO')),
    fecha     DATETIME      NOT NULL,
    FOREIGN KEY (id_compra) REFERENCES compra(id_compra)
);