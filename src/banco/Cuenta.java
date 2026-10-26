package banco;

public abstract class Cuenta {

    private final String numero;
    private final String titular;
    private long saldo;

    protected Cuenta(String numero, String titular, long saldoInicial) {
        if (saldoInicial < 0) {
            throw new OperacionRechazadaException("el saldo inicial no puede ser negativo");
        }
        this.numero = numero;
        this.titular = titular;
        if (saldoInicial > 0) {
            depositar(saldoInicial);
        }
    }

    public final void depositar(long monto) {
        validarMonto(monto);
        saldo += monto;
    }

    public final void retirar(long monto) {
        validarMonto(monto);
        long total = monto + comisionRetiro();
        validarRetiro(saldo - total);
        saldo -= total;
    }

    protected long comisionRetiro() {
        return 0;
    }

    protected abstract void validarRetiro(long saldoResultante);

    private static void validarMonto(long monto) {
        if (monto <= 0) {
            throw new OperacionRechazadaException("monto inválido");
        }
    }

    public String getNumero() {
        return numero;
    }

    public String getTitular() {
        return titular;
    }

    public long getSaldo() {
        return saldo;
    }
}
