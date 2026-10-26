package banco;

public class CuentaAhorros extends Cuenta {

    public CuentaAhorros(String numero, String titular, long saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    @Override
    protected void validarRetiro(long saldoResultante) {
        if (saldoResultante < 0) {
            throw new OperacionRechazadaException("saldo insuficiente");
        }
    }
}
