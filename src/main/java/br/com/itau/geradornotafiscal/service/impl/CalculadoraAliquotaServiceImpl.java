package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.*;
import br.com.itau.geradornotafiscal.service.CalculadoraAliquotaService;
import org.springframework.stereotype.Service;

@Service
public class CalculadoraAliquotaServiceImpl implements CalculadoraAliquotaService {

    @Override
    public double calcularAliquota(Pedido pedido) {
        Destinatario destinatario = pedido.getDestinatario();
        TipoPessoa tipoPessoa = destinatario.getTipoPessoa();
        double valorTotalItens = pedido.getValorTotalItens();

        if (tipoPessoa == TipoPessoa.FISICA) {
            if (valorTotalItens < 500) return 0;
            if (valorTotalItens <= 2000) return 0.12;
            if (valorTotalItens <= 3500) return 0.15;
            return 0.17;
        }

        if (tipoPessoa == TipoPessoa.JURIDICA) {
            RegimeTributacaoPJ regime = destinatario.getRegimeTributacao();
            if (regime == RegimeTributacaoPJ.SIMPLES_NACIONAL) {
                if (valorTotalItens < 1000) return 0.03;
                if (valorTotalItens <= 2000) return 0.07;
                if (valorTotalItens <= 5000) return 0.13;
                return 0.19;
            } else if (regime == RegimeTributacaoPJ.LUCRO_REAL) {
                if (valorTotalItens < 1000) return 0.03;
                if (valorTotalItens <= 2000) return 0.09;
                if (valorTotalItens <= 5000) return 0.15;
                return 0.20;
            } else if (regime == RegimeTributacaoPJ.LUCRO_PRESUMIDO) {
                if (valorTotalItens < 1000) return 0.03;
                if (valorTotalItens <= 2000) return 0.09;
                if (valorTotalItens <= 5000) return 0.16;
                return 0.20;
            }
        }
        throw new PedidoInvalidoException("Tipo de pessoa ou regime tributário inválido.");
    }
}
