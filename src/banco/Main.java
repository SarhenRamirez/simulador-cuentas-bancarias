package banco;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    private static final List<String> EJEMPLO = List.of(
            "CREAR AHORROS A1 Ana 100000",
            "CREAR CORRIENTE C1 Luis 50000",
            "DEPOSITAR A1 50000",
            "RETIRAR A1 200000",
            "TRANSFERIR C1 A1 300000",
            "RETIRAR C1 100000",
            "RETIRAR C1 200000",
            "DEPOSITAR X9 10000",
            "DEPOSITAR A1 -5000"
    );

    public static void main(String[] args) {
        List<String> lineas = EJEMPLO;
        if (args.length > 0) {
            try {
                lineas = Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("No se pudo leer el archivo " + args[0] + ": " + e.getMessage());
                System.exit(1);
            }
        }

        Banco banco = crearBanco();
        banco.ejecutarTodas(lineas);
        System.out.print(banco.generarReporte());
    }

    static Banco crearBanco() {
        Banco banco = new Banco();
        banco.registrarTipo("AHORROS", CuentaAhorros::new);
        banco.registrarTipo("CORRIENTE", CuentaCorriente::new);
        banco.registrarTipo("CDT", CuentaCDT::new);
        return banco;
    }
}
