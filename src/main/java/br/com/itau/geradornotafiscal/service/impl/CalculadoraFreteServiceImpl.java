package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.*;
import br.com.itau.geradornotafiscal.service.CalculadoraFreteService;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CalculadoraFreteServiceImpl implements CalculadoraFreteService {

    private static final Map<Regiao, Double> REGIOES_FRETE_PERCENTUAL = Map.of(
            Regiao.NORTE, 1.08,
            Regiao.NORDESTE, 1.085,
            Regiao.CENTRO_OESTE, 1.07,
            Regiao.SUDESTE, 1.048,
            Regiao.SUL, 1.06
    );

    @Override
    public double calcularFrete(Pedido pedido) {
        double valorFrete = pedido.getValorFrete();
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

    private double aplicarPercentualFrete(double valorFrete, Regiao regiao) {
        if (regiao == null) {
            throw new PedidoInvalidoException("Região da entrega não encontrada.");
        }
        return valorFrete * REGIOES_FRETE_PERCENTUAL.getOrDefault(regiao, 1.0);
    }
}

