# Simulador de cuentas bancarias

Programa de consola en **Java 17** (solo librería estándar, sin Maven/Gradle).
Probado con **Eclipse Temurin 17.0.20** en Windows 10.

## Cómo ejecutar

Requiere un JDK 17 o superior (`java -version`).

```bash
# Compilar
javac -encoding UTF-8 -d out src/banco/*.java

# Ejecutar el ejemplo del enunciado (operaciones escritas en el código)
java -cp out banco.Main

# Ejecutar leyendo las operaciones de un archivo
java -cp out banco.Main operaciones.txt

# Compilar y correr las pruebas
javac -encoding UTF-8 -d out src/banco/*.java test/banco/*.java
java -cp out banco.PruebasBanco
```

Salida del ejemplo:

```
SALDOS
A1 Ana 450000
C1 Luis -354000
RECHAZADAS (4)
Línea 4: RETIRAR A1 200000 -> saldo insuficiente
Línea 7: RETIRAR C1 200000 -> supera el cupo de sobregiro
Línea 8: DEPOSITAR X9 10000 -> la cuenta X9 no existe
Línea 9: DEPOSITAR A1 -5000 -> monto inválido
```

> En Windows, si las tildes se ven mal en consola, ejecuta `chcp 65001` antes.

## Estructura

| Archivo | Responsabilidad |
|---|---|
| `Cuenta` | Clase abstracta. Saldo `private`; solo cambia con `depositar` y `retirar` (ambos `final`). |
| `CuentaAhorros` | Sin comisión; el saldo no puede quedar negativo. |
| `CuentaCorriente` | Comisión de 2.000 por retiro; sobregiro hasta −500.000. |
| `CuentaCDT` | Tipo extra (opcional): acepta depósitos, no permite retiros. |
| `FabricaCuenta` | Interfaz funcional para crear cuentas de un tipo. |
| `Banco` | Guarda las cuentas, interpreta y ejecuta las operaciones, genera el reporte. |
| `OperacionRechazadaException` | Excepción cuyo mensaje es el motivo del rechazo. |
| `Main` | Lee las operaciones (código o archivo) y registra los tipos de cuenta. |
| `test/PruebasBanco` | Pruebas unitarias. |

## Requerimientos

| # | Requerimiento | Dónde se resuelve |
|---|---|---|
| 1 | `Cuenta` abstracta con `CuentaAhorros` y `CuentaCorriente` | `Cuenta.java`, `CuentaAhorros.java`, `CuentaCorriente.java` |
| 2 | El saldo solo cambia con `depositar` y `retirar` | `Cuenta`: `saldo` es `private`, sin setter; `depositar` y `retirar` son `final` |
| 3 | `Banco` guarda las cuentas y ejecuta las operaciones | `Banco.ejecutar` / `Banco.ejecutarTodas` |
| 4 | Transferir: si el retiro falla, no se deposita | `Banco.transferir`: el depósito va después del retiro, que lanza excepción si falla |
| 5 | Rechazar sin detener el programa | `OperacionRechazadaException`, atrapada en `Banco.ejecutar` |
| 6 | Reporte ordenado por saldo y rechazadas con motivo | `Banco.cuentasPorSaldo` y `Banco.generarReporte` |
| Opc. | Pruebas unitarias (incluye el límite de −500.000) | `test/banco/PruebasBanco.java` |
| Opc. | Tercer tipo de cuenta sin modificar `Banco` | `CuentaCDT.java`, registrada en `Main.crearBanco` |

## Decisiones

- **Template Method para el retiro.** `Cuenta.retirar` hace siempre lo mismo: valida el monto, suma la comisión, pregunta a la subclase si el saldo resultante es válido (`validarRetiro`) y descuenta. Cada tipo solo define su comisión y su regla.
- **La comisión cuenta para el cupo de sobregiro.** Un retiro en corriente se valida con `saldo − monto − 2.000 ≥ −500.000`. Por eso, con saldo 0, el retiro máximo es 498.000. Esto coincide con el ejemplo (C1 queda en −354.000).
- **Agregar tipos sin tocar `Banco`.** `Banco` no conoce las clases concretas: recibe fábricas con `registrarTipo("CDT", CuentaCDT::new)`, que se llaman desde `Main`. Para un tipo nuevo se crea la clase y se registra con una línea.
- **Rechazos con excepciones.** Cualquier regla que falle lanza `OperacionRechazadaException`; `Banco.ejecutar` la atrapa, guarda la línea y el motivo, y sigue con la siguiente operación.
- **Transferencia.** Primero se verifica que existan las dos cuentas, luego se retira del origen (con su comisión y sus reglas) y solo si eso funciona se deposita en el destino. El destino recibe el monto sin la comisión. Se rechaza transferir a la misma cuenta.
- **Montos enteros (`long`).** El COP no se maneja con decimales en el ejemplo; así se evitan errores de redondeo de `double`. Un monto que no sea entero (ej. `10.5`, `abc`) se rechaza como "monto inválido".
- **Saldo inicial.** En `CREAR` se permite saldo inicial 0 y se rechaza si es negativo. Se carga con `depositar`, así que el saldo nunca se asigna directamente.
- **Otras validaciones.** Se rechazan: número de cuenta duplicado, tipo de cuenta desconocido, operación desconocida y número de argumentos incorrecto. Los comandos y tipos no distinguen mayúsculas; los números de cuenta sí.
- **Titular de una sola palabra.** El formato se separa por espacios, así que el titular no puede tener espacios (ej. `Ana`, `Ana_Perez`).
- **Líneas vacías o que empiezan con `#`** se ignoran, pero los números de línea del reporte corresponden a la línea real del archivo.
- **Orden del reporte.** Por saldo de mayor a menor; si hay empate, por número de cuenta.
- **CDT.** Acepta depósitos y transferencias entrantes, pero no retiros ni transferencias salientes.
- **Pruebas sin JUnit.** Como solo se permite la librería estándar, `PruebasBanco` es un pequeño runner con `main` que imprime `[OK]`/`[FALLA]` y termina con código 1 si algo falla. Cubre una prueba por tipo de cuenta, el caso límite de −500.000 exacto, montos inválidos, transferencia fallida y el ejemplo completo.
