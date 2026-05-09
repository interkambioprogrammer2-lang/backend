# Feria Backend - Módulo de Control de Libros para Ferias

Sistema backend completo para gestionar libros, inventario y distribución en eventos de ferias. Desarrollado con Spring Boot 3.2.0, MySQL y arquitectura RESTful.

---

## 📋 Descripción

**Feria Backend** es una aplicación backend que proporciona una API completa para:
-  Gestión de catálogo de libros
-  Control de inventario y almacenes
-  Administración de ferias y eventos
-  Gestión de usuarios y permisos
-  Seguimiento de transacciones de inventario
-  Generación de reportes en PDF
-  Documentación interactiva con Swagger/OpenAPI

---

## 🛠 Requisitos Previos

- **Java 17** o superior
- **Maven 3.8.0** o superior
- **MySQL 8.0** o superior (Si usas MYSQL genera la base de datos desde Workbrench m[as sencillo])
- **Git** (opcional)

### Verificar Instalación
```bash
java -version
mvn -version
mysql --version
```

---

## 📦 Dependencias Principales

```xml
<!-- Spring Boot Web & Data JPA -->
spring-boot-starter-web: Framework web
spring-boot-starter-data-jpa: ORM y acceso a datos

<!-- MySQL Connector -->
mysql-connector-j: Driver JDBC para MySQL

<!-- Lombok -->
lombok: Generación automática de getters/setters

<!-- OpenPDF -->
openpdf (v1.3.30): Generación de documentos PDF

<!-- Swagger/OpenAPI -->
springdoc-openapi-starter-webmvc-ui (v2.5.0): Documentación interactiva
```

---

## 🚀 Instalación y Configuración

### 1. Clonar o Descargar el Proyecto
```bash
git clone <URL_DEL_REPOSITORIO>
cd feria-backend
```

### 2. Configurar Base de Datos

#### Crear la base de datos y el usuario:
```sql
CREATE DATABASE inventario_db;
CREATE USER 'root'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON inventario_db.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

#### Ejecutar el script de inicialización:
```sql
USE inventario_db;

-- Eliminar tablas sobrantes (si aún existen)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS fair_return_items;
DROP TABLE IF EXISTS fair_dispatch_items;
DROP TABLE IF EXISTS fairs;
SET FOREIGN_KEY_CHECKS = 1;

-- Ajustar inventory_transactions
ALTER TABLE inventory_transactions DROP COLUMN IF EXISTS quantity_change;

-- Aumentar stock (según necesidad)
SET SQL_SAFE_UPDATES = 0;
UPDATE book_stock_locations SET stock = stock + 50 WHERE stock < 50;
SET SQL_SAFE_UPDATES = 1;

-- Datos mínimos
INSERT INTO users (id, name, email) VALUES (1, 'Admin', 'admin@example.com') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO warehouses (id, name) VALUES (1, 'Almacén principal') ON DUPLICATE KEY UPDATE name=name;
```

### 3. Configurar Propiedades de la Aplicación

Editar `src/main/resources/application.properties`:
```properties
# Base de Datos
spring.datasource.url=jdbc:mysql://localhost:3306/inventario_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password

# JPA & Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Swagger/OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

**Nota:** Las variables de entorno disponibles son:
- `SPRING_DATASOURCE_URL`: URL de conexión a la base de datos

### 4. Compilar el Proyecto
```bash
mvn clean install
```

---

## ▶️ Ejecución

### Opción 1: Ejecutar desde Maven (Siempre use esta)
```bash
mvn spring-boot:run
```

### Opción 2: Ejecutar JAR compilado
```bash
java -jar target/feria-backend-1.0-SNAPSHOT.jar
```

### Opción 3: Ejecutar desde IDE (Usa IntelliJ IDEA, VSC es un poco pesado con esto)
- Abre el proyecto en tu IDE favorito (IntelliJ IDEA, Eclipse, VS Code)
- Ejecuta `FeriasApplication.java` como aplicación Spring Boot

---

## 📡 API y Documentación

Una vez iniciada la aplicación, accede a:

### Swagger UI (Documentación Interactiva)
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/api-docs
```

### Endpoints Principales

#### Ferias (`/api/fairs`)
- `GET /api/fairs/{id}` - Obtener detalle las ferias
- `PUT /api/fairs/{id}` - Editar una feria existente
- `PUT /api/fairs/{id}/record-return` - Registrar retorno de libros
- `PUT /api/fairs/{id}/confirm-dispatch` - Confirmar envio y descontar inventario
- `GET /api/fairs` - Listar todas las ferias 
- `POST /api/fairs` -Crear nueva feria
- `POST /api/fairs/{id}/dispatch-items` - Agregar libros al envio
- `GET /api/fairs/{id}/report/sebdout` - Generar PDF del listado
- `GET /api/fairs/{id}/report/final` - Generar PDF del resumen final


#### Almacenes (`/api/warehouses`)
- `GET /api/warehouses` - Listar todos los almacenes


#### Usuarios (`/api/users`)
- `GET /api/users` - Listado de todos los usuarios

---

## Estructura del Proyecto (Clean Architecture)

```
feria-backend/
│
├── src/
│   ├── main/
│   │   ├── java/org/interkambio/ferias/
│   │   │   ├── FeriasApplication.java              # Clase principal de Spring Boot
│   │   │   ├── config/                              # Configuraciones
│   │   │   │   ├── CorsConfig.java                 # CORS
│   │   │   │   └── OpenApiConfig.java              # Swagger
│   │   │   ├── controller/                          # Controladores REST
│   │   │   │   ├── BookController.java
│   │   │   │   ├── FairController.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── WarehouseController.java
│   │   │   ├── dto/                                 # Data Transfer Objects
│   │   │   │   ├── FairDetail.java
│   │   │   │   ├── FairRequest.java
│   │   │   │   ├── FairResponse.java
│   │   │   │   ├── FairSummary.java
│   │   │   │   ├── DispatchItemRequest.java
│   │   │   │   └── ReturnRequest.java
│   │   │   ├── entity/                              # Entidades JPA
│   │   │   │   ├── Book.java
│   │   │   │   ├── Fair.java
│   │   │   │   ├── FairDispatchItem.java
│   │   │   │   ├── FairStatus.java
│   │   │   │   ├── User.java
│   │   │   │   ├── Warehouse.java
│   │   │   │   ├── BookStockLocation.java
│   │   │   │   └── InventoryTransaction.java
│   │   │   ├── exception/                           # Manejo de excepciones
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── NotFoundException.java
│   │   │   ├── repository/                          # Repositorios JPA
│   │   │   │   ├── BookRepository.java
│   │   │   │   ├── FairRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── WarehouseRepository.java
│   │   │   │   ├── BookStockLocationRepository.java
│   │   │   │   ├── FairDispatchItemRepository.java
│   │   │   │   └── InventoryTransactionRepository.java
│   │   │   └── service/                             # Lógica de negocio
│   │   │       ├── FairService.java
│   │   │       ├── InventoryService.java
│   │   │       └── PdfService.java
│   │   └── resources/
│   │       ├── application.properties               # Configuración de la app
│   │       └── images/                              # Recursos de imágenes
│   │
│   └── test/                                        # Tests unitarios
│
├── target/                                          # Artefactos compilados
├── pom.xml                                          # Configuración de Maven
└── README.md                                        # Este archivo
```

---

## 🔧 Guía de Desarrollo

### Agregar Nueva Entidad

1. **Crear la clase entidad** en `entity/`:
```java
@Entity
@Table(name = "nueva_tabla")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NuevaEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
}
```

2. **Crear el repositorio** en `repository/`:
```java
public interface NuevaEntidadRepository extends JpaRepository<NuevaEntidad, Long> {
    // Métodos personalizados si es necesario
}
```

3. **Crear el controlador** en `controller/`:
```java
@RestController
@RequestMapping("/api/nueva-entidad")
@CrossOrigin(origins = "*")
public class NuevaEntidadController {
    // Implementar endpoints
}
```

### Build y Pruebas

```bash
# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Build completo
mvn clean package

# Skip tests
mvn clean package -DskipTests
```

---

## Diagrama de Base de Datos

### Tablas Principales

| Tabla | Descripción |
|-------|-------------|
| **books** | Catálogo de libros |
| **book_stock_locations** | Stock por almacén |
| **warehouses** | Almacenes disponibles |
| **users** | Usuarios del sistema |
| **fairs** | Ferias/eventos |
| **fair_dispatch_items** | Libros despachados a ferias |
| **inventory_transactions** | Registro de transacciones |

---

## Seguridad

### CORS
- Configurado en `CorsConfig.java`
- Actualmente permite solicitudes desde cualquier origen
- **Recomendación:** Restringir en producción

### Base de Datos
- Ejecuta el script de data-table en MYSQL
- Cambiar credenciales por defecto (root/password)
- Usar variables de entorno para sensibles



---

## 🐛 Troubleshooting

### Error: "No suitable driver found for jdbc:mysql"
```bash
mvn clean install
mvn dependency:resolve
```

### Error: "Access denied for user 'root'@'localhost'"
- Verificar credenciales en `application.properties`
- Asegurar que MySQL está corriendo: `mysql -u root -p`

### Error: "Table 'inventario_db.books' doesn't exist"
- Ejecutar el script SQL de inicialización
- Verificar que Hibernate DDL está en `update` mode

### Puerto 8080 ya en uso
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

---

## Logs y Debugging

Los logs se configuran en `application.properties`:
```properties
spring.jpa.show-sql=true          # Mostrar SQL generado
logging.level.root=INFO
logging.level.org.interkambio=DEBUG
```

Ver logs en tiempo real:
```bash
tail -f target/spring-boot.log
```

---

## 🚀 Deployment

```

### Variables de Entorno para Producción
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://prod-host:3306/inventario_db
export SPRING_DATASOURCE_USERNAME=user_prod
export SPRING_DATASOURCE_PASSWORD=pass_prod
mvn spring-boot:run
```

---


## 📄 Licencia

Este proyecto es propiedad de Interkambio.

---


## 📅 Historial de Versiones

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0-SNAPSHOT | 2026-05-09 | Versión inicial |

---

**Última actualización:** 9 de Mayo de 2026

**Desarrollado con ❤️ por   GhostNotFound404**
