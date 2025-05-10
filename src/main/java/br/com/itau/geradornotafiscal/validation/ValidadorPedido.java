package br.com.itau.geradornotafiscal.validation;

import br.com.itau.geradornotafiscal.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class ValidadorPedido {

    public void validarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new PedidoInvalidoException("Pedido não pode ser nulo.");
        }

        Destinatario destinatario = pedido.getDestinatario();
        if (destinatario == null) {
            throw new PedidoInvalidoException("Destinatário do pedido não pode ser nulo.");
        }

        if (destinatario.getTipoPessoa() == null) {
            throw new PedidoInvalidoException("Tipo de pessoa do destinatário não pode ser nulo.");
        }

        if (pedido.getDestinatario().getTipoPessoa() == TipoPessoa.JURIDICA) {
            RegimeTributacaoPJ regime = pedido.getDestinatario().getRegimeTributacao();
            if (regime == null) {
                throw new PedidoInvalidoException("Regime tributário não pode ser nulo para pessoas jurídicas.");
            }
        }

        if (pedido.getValorTotalItens().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PedidoInvalidoException("Valor total dos itens deve ser maior que zero.");
        }

        if (pedido.getValorFrete().compareTo(BigDecimal.ZERO) < 0) {
            throw new PedidoInvalidoException("Valor do frete não pode ser negativo.");
        }

        if (destinatario.getEnderecos() == null || destinatario.getEnderecos().isEmpty()) {
            throw new PedidoInvalidoException("Destinatário deve conter ao menos um endereço.");
        }

        Optional<Endereco> enderecoEntregaOpt = destinatario.getEnderecos().stream()
                .filter(e -> e.getFinalidade() == Finalidade.ENTREGA || e.getFinalidade() == Finalidade.COBRANCA_ENTREGA)
                .findFirst();

        if (enderecoEntregaOpt.isPresent()) {
            Endereco enderecoEntrega = enderecoEntregaOpt.get();
            if (enderecoEntrega.getRegiao() == null) {
                throw new PedidoInvalidoException("Região não pode ser nula para o endereço de entrega.");
            }
        } else {
            throw new PedidoInvalidoException("Destinatário deve conter pelo menos um endereço com finalidade de ENTREGA ou COBRANCA_ENTREGA.");
        }
    }
}
