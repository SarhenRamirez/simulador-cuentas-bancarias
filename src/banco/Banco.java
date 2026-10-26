package banco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Banco {

    public record Rechazo(int linea, String operacion, String motivo) {
    }

    private final Map<String, FabricaCuenta> tipos = new HashMap<>();
    private final Map<String, Cuenta> cuentas = new LinkedHashMap<>();
    private final List<Rechazo> rechazos = new ArrayList<>();

    public void registrarTipo(String tipo, FabricaCuenta fabrica) {
        tipos.put(tipo.toUpperCase(Locale.ROOT), fabrica);
    }

    public void ejecutarTodas(List<String> lineas) {
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            ejecutar(linea, i + 1);
        }
    }

    public void ejecutar(String linea, int numeroLinea) {
        try {
            String[] partes = linea.trim().split("\\s+");
            String comando = partes[0].toUpperCase(Locale.ROOT);
            switch (comando) {
                case "CREAR" -> crear(partes);
                case "DEPOSITAR" -> depositar(partes);
                case "RETIRAR" -> retirar(partes);
                case "TRANSFERIR" -> transferir(partes);
                default -> throw new OperacionRechazadaException("operación desconocida: " + partes[0]);
            }
        } catch (OperacionRechazadaException e) {
            rechazos.add(new Rechazo(numeroLinea, linea.trim(), e.getMessage()));
        }
    }

    private void crear(String[] partes) {
        exigirArgumentos(partes, 5, "CREAR <TIPO> <NUMERO> <TITULAR> <SALDO>");
        FabricaCuenta fabrica = tipos.get(partes[1].toUpperCase(Locale.ROOT));
        if (fabrica == null) {
            throw new OperacionRechazadaException("tipo de cuenta desconocido: " + partes[1]);
        }
        String numero = partes[2];
        if (cuentas.containsKey(numero)) {
            throw new OperacionRechazadaException("la cuenta ya existe");
        }
        long saldoInicial = leerMonto(partes[4]);
        cuentas.put(numero, fabrica.crear(numero, partes[3], saldoInicial));
    }

    private void depositar(String[] partes) {
        exigirArgumentos(partes, 3, "DEPOSITAR <NUMERO> <MONTO>");
        Cuenta cuenta = buscar(partes[1]);
        cuenta.depositar(leerMonto(partes[2]));
    }

    private void retirar(String[] partes) {
        exigirArgumentos(partes, 3, "RETIRAR <NUMERO> <MONTO>");
        Cuenta cuenta = buscar(partes[1]);
        cuenta.retirar(leerMonto(partes[2]));
    }

    private void transferir(String[] partes) {
        exigirArgumentos(partes, 4, "TRANSFERIR <ORIGEN> <DESTINO> <MONTO>");
        Cuenta origen = buscar(partes[1]);
        Cuenta destino = buscar(partes[2]);
        if (origen == destino) {
            throw new OperacionRechazadaException("origen y destino son la misma cuenta");
        }
        long monto = leerMonto(partes[3]);
        origen.retirar(monto);
        destino.depositar(monto);
    }

    private Cuenta buscar(String numero) {
        Cuenta cuenta = cuentas.get(numero);
        if (cuenta == null) {
            throw new OperacionRechazadaException("la cuenta " + numero + " no existe");
        }
        return cuenta;
    }

    private static long leerMonto(String texto) {
        try {
            return Long.parseLong(texto);
        } catch (NumberFormatException e) {
            throw new OperacionRechazadaException("monto inválido");
        }
    }

    private static void exigirArgumentos(String[] partes, int cantidad, String formato) {
        if (partes.length != cantidad) {
            throw new OperacionRechazadaException("formato inválido, se esperaba: " + formato);
        }
    }

    public Cuenta getCuenta(String numero) {
        return cuentas.get(numero);
    }

    public List<Rechazo> getRechazos() {
        return Collections.unmodifiableList(rechazos);
    }

    public List<Cuenta> cuentasPorSaldo() {
        List<Cuenta> ordenadas = new ArrayList<>(cuentas.values());
        ordenadas.sort(Comparator.comparingLong(Cuenta::getSaldo).reversed()
                .thenComparing(Cuenta::getNumero));
        return ordenadas;
    }

    public String generarReporte() {
        StringBuilder sb = new StringBuilder();
        sb.append("SALDOS\n");
        for (Cuenta c : cuentasPorSaldo()) {
            sb.append(c.getNumero()).append(' ')
              .append(c.getTitular()).append(' ')
              .append(c.getSaldo()).append('\n');
        }
        sb.append("RECHAZADAS (").append(rechazos.size()).append(")\n");
        for (Rechazo r : rechazos) {
            sb.append("Línea ").append(r.linea()).append(": ")
              .append(r.operacion()).append(" -> ")
              .append(r.motivo()).append('\n');
        }
        return sb.toString();
    }
}
