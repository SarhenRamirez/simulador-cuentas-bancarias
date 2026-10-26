package banco;

public class CuentaCorriente extends Cuenta {

    public static final long COMISION_RETIRO = 2_000;
    public static final long CUPO_SOBREGIRO = 500_000;

    public CuentaCorriente(String numero, String titular, long saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    @Override
    protected long comisionRetiro() {
        return COMISION_RETIRO;
    }

    @Override
    protected void validarRetiro(long saldoResultante) {
        if (saldoResultante < -CUPO_SOBREGIRO) {
            throw new OperacionRechazadaException("supera el cupo de sobregiro");
        }
    }
}
