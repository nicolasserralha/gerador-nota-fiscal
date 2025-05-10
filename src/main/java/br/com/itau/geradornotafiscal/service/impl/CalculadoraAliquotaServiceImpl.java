package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.*;
import br.com.itau.geradornotafiscal.service.CalculadoraAliquotaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CalculadoraAliquotaServiceImpl implements CalculadoraAliquotaService {

    @Override
    public BigDecimal calcularAliquota(Pedido pedido) {
        Destinatario destinatario = pedido.getDestinatario();
        TipoPessoa tipoPessoa = destinatario.getTipoPessoa();
        BigDecimal valorTotalItens = pedido.getValorTotalItens();

        if (tipoPessoa == TipoPessoa.FISICA) {
            if (valorTotalItens.compareTo(BigDecimal.valueOf(500)) < 0) return BigDecimal.valueOf(0.0);
            if (valorTotalItens.compareTo(BigDecimal.valueOf(2000)) <= 0) return BigDecimal.valueOf(0.12);
            if (valorTotalItens.compareTo(BigDecimal.valueOf(3500)) <= 0) return BigDecimal.valueOf(0.15);
            return BigDecimal.valueOf(0.17);
        }

        if (tipoPessoa == TipoPessoa.JURIDICA) {
            RegimeTributacaoPJ regime = destinatario.getRegimeTributacao();
            if (regime == RegimeTributacaoPJ.SIMPLES_NACIONAL) {
                if (valorTotalItens.compareTo(BigDecimal.valueOf(1000)) < 0) return BigDecimal.valueOf(0.03);
                if (valorTotalItens.compareTo(BigDecimal.valueOf(2000)) <= 0) return BigDecimal.valueOf(0.07);
                if (valorTotalItens.compareTo(BigDecimal.valueOf(5000)) <= 0) return BigDecimal.valueOf(0.13);
                return BigDecimal.valueOf(0.19);
            } else if (regime == RegimeTributacaoPJ.LUCRO_REAL) {
                if (valorTotalItens.compareTo(BigDecimal.valueOf(1000)) < 0) return BigDecimal.valueOf(0.03);
                if (valorTotalItens.compareTo(BigDecimal.valueOf(2000)) <= 0) return BigDecimal.valueOf(0.09);
                if (valorTotalItens.compareTo(BigDecimal.valueOf(5000)) <= 0) return BigDecimal.valueOf(0.15);
                return BigDecimal.valueOf(0.20);
            } else if (regime == RegimeTributacaoPJ.LUCRO_PRESUMIDO) {
                if (valorTotalItens.compareTo(BigDecimal.valueOf(1000)) < 0) return BigDecimal.valueOf(0.03);
                if (valorTotalItens.compareTo(BigDecimal.valueOf(2000)) <= 0) return BigDecimal.valueOf(0.09);
                if (valorTotalItens.compareTo(BigDecimal.valueOf(5000)) <= 0) return BigDecimal.valueOf(0.16);
                return BigDecimal.valueOf(0.20);
            }
        }

        throw new PedidoInvalidoException("Tipo de pessoa ou regime tributário inválido.");
    }
}
