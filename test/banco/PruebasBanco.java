package banco;

import java.util.List;

public class PruebasBanco {

    private static int ejecutadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        prueba("Ahorros: retiro válido descuenta sin comisión", PruebasBanco::ahorrosRetiroValido);
        prueba("Ahorros: puede quedar exactamente en 0", PruebasBanco::ahorrosQuedaEnCero);
        prueba("Ahorros: rechaza retiro que deja saldo negativo", PruebasBanco::ahorrosSaldoInsuficiente);
        prueba("Corriente: cobra 2.000 de comisión por retiro", PruebasBanco::corrienteCobraComision);
        prueba("Corriente: límite exacto en -500.000 se permite", PruebasBanco::corrienteLimiteExacto);
        prueba("Corriente: rechaza retiro que pasa de -500.000", PruebasBanco::corrienteSuperaSobregiro);
        prueba("CDT: no permite retiros", PruebasBanco::cdtNoPermiteRetiros);
        prueba("Cuenta: rechaza montos <= 0", PruebasBanco::montoInvalido);
        prueba("Banco: transferencia fallida no deposita", PruebasBanco::transferenciaFallidaNoDeposita);
        prueba("Banco: ejemplo del enunciado", PruebasBanco::ejemploEnunciado);

        System.out.printf("%n%d pruebas, %d fallidas%n", ejecutadas, fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    static void ahorrosRetiroValido() {
        Cuenta c = new CuentaAhorros("A1", "Ana", 100_000);
        c.retirar(30_000);
        igual(70_000, c.getSaldo());
    }

    static void ahorrosQuedaEnCero() {
        Cuenta c = new CuentaAhorros("A1", "Ana", 100_000);
        c.retirar(100_000);
        igual(0, c.getSaldo());
    }

    static void ahorrosSaldoInsuficiente() {
        Cuenta c = new CuentaAhorros("A1", "Ana", 100_000);
        rechaza(() -> c.retirar(100_001), "saldo insuficiente");
        igual(100_000, c.getSaldo());
    }

    static void corrienteCobraComision() {
        Cuenta c = new CuentaCorriente("C1", "Luis", 50_000);
        c.retirar(10_000);
        igual(38_000, c.getSaldo());
    }

    static void corrienteLimiteExacto() {
        Cuenta c = new CuentaCorriente("C1", "Luis", 0);
        c.retirar(498_000); // 498.000 + 2.000 de comisión = 500.000
        igual(-500_000, c.getSaldo());
    }

    static void corrienteSuperaSobregiro() {
        Cuenta c = new CuentaCorriente("C1", "Luis", 0);
        rechaza(() -> c.retirar(498_001), "supera el cupo de sobregiro");
        igual(0, c.getSaldo());
    }

    static void cdtNoPermiteRetiros() {
        Cuenta c = new CuentaCDT("D1", "Eva", 1_000_000);
        rechaza(() -> c.retirar(1), "un CDT no permite retiros");
        igual(1_000_000, c.getSaldo());
    }

    static void montoInvalido() {
        Cuenta c = new CuentaAhorros("A1", "Ana", 100);
        rechaza(() -> c.depositar(0), "monto inválido");
        rechaza(() -> c.depositar(-5), "monto inválido");
        rechaza(() -> c.retirar(-5), "monto inválido");
        igual(100, c.getSaldo());
    }

    static void transferenciaFallidaNoDeposita() {
        Banco banco = Main.crearBanco();
        banco.ejecutarTodas(List.of(
                "CREAR AHORROS A1 Ana 1000",
                "CREAR AHORROS A2 Beto 0",
                "TRANSFERIR A1 A2 5000"
        ));
        igual(1000, banco.getCuenta("A1").getSaldo());
        igual(0, banco.getCuenta("A2").getSaldo());
        igual(1, banco.getRechazos().size());
    }

    static void ejemploEnunciado() {
        Banco banco = Main.crearBanco();
        banco.ejecutarTodas(List.of(
                "CREAR AHORROS A1 Ana 100000",
                "CREAR CORRIENTE C1 Luis 50000",
                "DEPOSITAR A1 50000",
                "RETIRAR A1 200000",
                "TRANSFERIR C1 A1 300000",
                "RETIRAR C1 100000",
                "RETIRAR C1 200000",
                "DEPOSITAR X9 10000",
                "DEPOSITAR A1 -5000"
        ));
        List<Cuenta> ordenadas = banco.cuentasPorSaldo();
        igual("A1", ordenadas.get(0).getNumero());
        igual(450_000, ordenadas.get(0).getSaldo());
        igual("C1", ordenadas.get(1).getNumero());
        igual(-354_000, ordenadas.get(1).getSaldo());

        List<Banco.Rechazo> rechazos = banco.getRechazos();
        igual(4, rechazos.size());
        igual(4, rechazos.get(0).linea());
        igual(7, rechazos.get(1).linea());
        igual(8, rechazos.get(2).linea());
        igual(9, rechazos.get(3).linea());
    }

    private static void prueba(String nombre, Runnable cuerpo) {
        ejecutadas++;
        try {
            cuerpo.run();
            System.out.println("[OK]    " + nombre);
        } catch (Throwable t) {
            fallidas++;
            System.out.println("[FALLA] " + nombre + " -> " + t.getMessage());
        }
    }

    private static void igual(Object esperado, Object obtenido) {
        if (!esperado.equals(obtenido)) {
            throw new AssertionError("se esperaba " + esperado + " pero fue " + obtenido);
        }
    }

    private static void igual(long esperado, long obtenido) {
        if (esperado != obtenido) {
            throw new AssertionError("se esperaba " + esperado + " pero fue " + obtenido);
        }
    }

    private static void rechaza(Runnable accion, String motivoEsperado) {
        try {
            accion.run();
        } catch (OperacionRechazadaException e) {
            igual(motivoEsperado, e.getMessage());
            return;
        }
        throw new AssertionError("se esperaba rechazo: " + motivoEsperado);
    }
}
