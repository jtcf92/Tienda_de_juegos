# Uso de inteligencia artificial

## Herramientas utilizadas

- **Claude** (Anthropic), mediante la aplicación de Claude, como asistente conversacional durante todo el desarrollo.

No se ha utilizado ninguna herramienta de IA integrada en el IDE: el código se escribió y se ejecutó en IntelliJ IDEA.

## Forma de uso

La IA se ha usado como **profesor particular y revisor**, avanzando por pasos pequeños:

- **Planificación**: dividir el enunciado en fases con una estimación de tiempo y ajustar el plan según el tiempo disponible.
- **Aprendizaje de la teoría**: explicación de JDBC, drivers, conexiones, pool de conexiones, configuración externa, `PreparedStatement`, `ResultSet`, lotes, claves generadas, transacciones, DAO, DTO y procedimientos almacenados, con analogías, diagramas y pequeños tests de comprobación antes de aplicar cada concepto.
- **Diseño**: simplificación del modelo de datos de la tienda (de 15 a 9 tablas), comprobando que cumpliera todos los requisitos del enunciado.
- **Código guiado**: propuestas de código clase a clase, con la explicación de cada parte, que se incorporaban al proyecto y se ejecutaban antes de pasar a la siguiente.
- **Revisión y depuración**: revisión del proyecto completo en distintas fases y diagnóstico de errores a partir de la consola y los logs.
- **Documentación**: ayuda en la redacción del README.

## Verificación del resultado

Todo el código propuesto se ejecutó y se comprobó en cada fase:

- Ejecución en las 4 combinaciones de gestor (MySQL / H2) y modo de conexión (DriverManager / HikariCP), y en el modo `FALLO_TRANSACCION`.
- Consultas en MySQL Workbench para comprobar los datos guardados tras cada operación.
- Revisión de los logs y de las evidencias de ejecución.
- Pruebas JUnit 5, incluida una comprobación de que fallan cuando el resultado no es el esperado.

Durante el desarrollo se detectaron y corrigieron errores, algunos en propuestas de la propia IA:

- Un `import` de `LogManager` del paquete equivocado (`java.util.logging` en vez de Log4j).
- La dependencia de H2 copiada con `<scope>test</scope>`, que impedía usar H2 al ejecutar la aplicación.
- La clase de inicialización convertía las `SQLException` en `RuntimeException`, ocultando los errores al `Main`.
- Las restricciones `CHECK ... IN (...)` fallaban en H2 en memoria. La primera solución propuesta (cambiar `IN` por `OR`) no funcionó, porque H2 convierte internamente el `OR` en `IN`. Se resolvió usando H2 en fichero en las pruebas.
- Una prueba de rollback que pasaba por el motivo equivocado: se reforzó comprobando el mensaje de la excepción.

## Prompts principales

Copiados literalmente.

1. > Tengo que realizar este trabajo de acceso a datos, necesito que me lo expliques y lo dividas por indices que debo cumplir, diciendo cuanto me llevara cada indice en tiempo y si es necesario que los indices tengan sub indices, esto es un proyecto para aprender como hacerlo

2. > La báse de datos ya la tengo hecha, es esta, si no tengo conceptos bien en java, ya que mysql si lo tengo hecho, teniendo en cuenta esto actualiza el horario y indices

3. > Disculpa me mal entendiste si se programar en java pero no se jdbc es lo que queria decir, asi que empecemos por ese indice, dame ejemplos con código y teoria

4. > Okey estas volviendo a olvidar explicarme cosas, que es h2,  que es pool qie es el .properties, haz un método de estudio de verdad porfavor, solo supon que se programar en java y ya esta, nada de estos temas de jdb y persistencia, no hace falta que hagamos código en cada charla, solo que me expliques de manera didactica y visual y cuando veas que se lo suficiente para poner en el proyecto me lo dices

5. > Fuiste muy rapido con la clase configuracion, creemoslo poco a poco ¿Okey?

6. > okey avancemos al mini f, pero recuerda que tambien en el trabajo se deben enviar los logs asi que en ves de usar print usemos logs ya de una ves, yo se usar un poco de logs, no se si pide algo nuevo el ejercicios, solo dame el xml configurado

7. > Ya uni las ramas, ahora quiero que mantengas la base de datos simples, antes de hacer código genera un diagrama y has un check de si cumple con todos los criterios, manten la tematica de tienda de videojuegos, si no puedes no pasa nada y elimina cosas como el tipo de usuario si ves oportuno ya que solo quiero resolverlo de la forma mas simple posible

8. > Yo se sql y me parece extraño que lo estes haciendo asi, mejor crealas en una base de datos como pide unidad 2 haces un drop database if exist y luego un create database unidad 2 y te ahorras todos los drops individuales, ademas ahi no asocias las tablas a una base de datos

9. > Osea ya terminamos con todos los daos o falta alguno? luego creo que debemos de unificar todos los daos en una clase que facilita mejor el uso ¿no?

10. > Okey de momento llevamos esto quiero que lo analices para saber que tal lo hice y si esta todo correcto avancemos

11. > Me estoy preocupando, date un tiempo para razonar mejor, en stock DeJuego solo le pasas el int juego y no el proveedor

12. > Por cierto el simulador te parece bien hacerlo todo en el main? en el ejercicio no dejo en claro algo de las responsabilidades,y por cierto si todo va bien explica metodo a metodo para entenderlo mejor