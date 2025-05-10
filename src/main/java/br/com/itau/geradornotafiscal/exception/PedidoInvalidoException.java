package br.com.itau.geradornotafiscal.exception;

public class PedidoInvalidoException extends RuntimeException {
    public PedidoInvalidoException(String message) {
        super(message);
    }
}
