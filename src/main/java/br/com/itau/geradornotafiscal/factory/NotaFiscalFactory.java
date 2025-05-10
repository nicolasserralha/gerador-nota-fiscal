package br.com.itau.geradornotafiscal.factory;

import br.com.itau.geradornotafiscal.model.NotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class NotaFiscalFactory {

    public NotaFiscal criarNotaFiscal(Pedido pedido, List<ItemNotaFiscal> itensNotaFiscal, double valorFrete) {
        return NotaFiscal.builder()
                .idNotaFiscal(UUID.randomUUID().toString())
                .data(LocalDateTime.now())
                .valorTotalItens(pedido.getValorTotalItens())
                .valorFrete(valorFrete)
                .itens(itensNotaFiscal)
                .destinatario(pedido.getDestinatario())
                .build();
    }
}