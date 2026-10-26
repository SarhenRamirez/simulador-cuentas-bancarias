package banco;

@FunctionalInterface
public interface FabricaCuenta {

    Cuenta crear(String numero, String titular, long saldoInicial);
}
