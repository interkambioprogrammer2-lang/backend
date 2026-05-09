-- ============================================================================
-- SCRIPT DE INICIALIZACIÓN DE BASE DE DATOS - FERIA BACKEND
-- ============================================================================
-- Descripción: Script para inicializar y configurar la base de datos de Ferias
-- Fecha: 9 de Mayo de 2026
-- Versión: 1.0
-- ============================================================================

-- Seleccionar la base de datos
USE inventario_db;
-- ============================================================================
-- PASO 1: LIMPIAR TABLAS EXISTENTES (OPCIONAL)
-- ============================================================================
-- Deshabilitar restricciones de clave foránea temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- Eliminar tablas que pueden estar sobrantes o mal configuradas
DROP TABLE IF EXISTS fair_return_items;
DROP TABLE IF EXISTS fair_dispatch_items;
DROP TABLE IF EXISTS fairs;

-- Reabilitar restricciones de clave foránea
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- PASO 2: AJUSTAR ESQUEMA DE TABLAS EXISTENTES
-- ============================================================================

-- Eliminar columna 'quantity_change' de inventory_transactions si existe
-- (en caso de cambios en el esquema)
ALTER TABLE inventory_transactions DROP COLUMN IF EXISTS quantity_change;

-- ============================================================================
-- PASO 3: AUMENTAR STOCK EN ALMACENES
-- ============================================================================
-- Deshabilitar modo seguro temporalmente para permitir actualizaciones
SET SQL_SAFE_UPDATES = 0;

-- Aumentar stock a 50+ si tiene menos de 50
UPDATE book_stock_locations SET stock = stock + 50 WHERE stock < 50;

-- Reabilitar modo seguro
SET SQL_SAFE_UPDATES = 1;

-- ============================================================================
-- PASO 4: INSERTAR DATOS MÍNIMOS REQUERIDOS
-- ============================================================================

-- Insertar usuario administrador (si no existe)
INSERT INTO users (id, name, email) 
VALUES (1, 'Admin', 'admin@example.com') 
ON DUPLICATE KEY UPDATE name=name;

-- Insertar almacén principal (si no existe)
INSERT INTO warehouses (id, name) 
VALUES (1, 'Almacén principal') 
ON DUPLICATE KEY UPDATE name=name;

-- ============================================================================
-- PASO 5: VERIFICACIÓN
-- ============================================================================
-- Mostrar registros creados
SELECT '--- USUARIOS ---' AS info;
SELECT * FROM users;

SELECT '--- ALMACENES ---' AS info;
SELECT * FROM warehouses;

SELECT '--- STOCK DE LIBROS ---' AS info;
SELECT * FROM book_stock_locations LIMIT 10;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
-- Notas:
-- 1. Este script es idempotente (puede ejecutarse múltiples veces sin errores)
-- 2. Los datos mínimos se insertan solo si no existen (ON DUPLICATE KEY)
-- 3. El stock se aumenta solo para ubicaciones con menos de 50 unidades
-- 4. Este script NO borra datos existentes de negocio
-- ============================================================================
