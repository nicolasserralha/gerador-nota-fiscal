package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.*;
import br.com.itau.geradornotafiscal.service.CalculadoraFreteService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CalculadoraFreteServiceImpl implements CalculadoraFreteService {

    private static final Map<Regiao, BigDecimal> REGIOES_FRETE_PERCENTUAL = Map.of(
            Regiao.NORTE, BigDecimal.valueOf(1.08),
            Regiao.NORDESTE, BigDecimal.valueOf(1.085),
            Regiao.CENTRO_OESTE, BigDecimal.valueOf(1.07),
            Regiao.SUDESTE, BigDecimal.valueOf(1.048),
            Regiao.SUL, BigDecimal.valueOf(1.06)
    );

    @Override
    public BigDecimal calcularFrete(Pedido pedido) {
        BigDecimal valorFrete = pedido.getValorFrete();
        Regiao regiao = obterRegiaoEntrega(pedido);

        return aplicarPercentualFrete(valorFrete, regiao);
    }

    private Regiao obterRegiaoEntrega(Pedido pedido) {
        return pedido.getDestinatario().getEnderecos().stream()
                .filter(endereco -> endereco.getFinalidade() == Finalidade.ENTREGA || endereco.getFinalidade() == Finalidade.COBRANCA_ENTREGA)
                .map(Endereco::getRegiao)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal aplicarPercentualFrete(BigDecimal valorFrete, Regiao regiao) {
        if (regiao == null) {
            throw new PedidoInvalidoException("Região da entrega não encontrada.");
        }
        return valorFrete.multiply(REGIOES_FRETE_PERCENTUAL.getOrDefault(regiao, BigDecimal.valueOf(1.0)));
    }
}

