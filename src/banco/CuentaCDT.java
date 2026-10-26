package banco;

public class CuentaCDT extends Cuenta {

    public CuentaCDT(String numero, String titular, long saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    @Override
    protected void validarRetiro(long saldoResultante) {
        throw new OperacionRechazadaException("un CDT no permite retiros");
    }
}
