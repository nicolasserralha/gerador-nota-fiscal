package br.com.itau.geradornotafiscal.factory;

import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemNotaFiscalFactory {

    public List<ItemNotaFiscal> gerarItensNotaFiscal(Pedido pedido, double aliquota) {
        return pedido.getItens().stream()
                .map(item -> criarItemNotaFiscal(item, aliquota))
                .collect(Collectors.toList());
    }

    private ItemNotaFiscal criarItemNotaFiscal(Item item, double aliquota) {
        double valorComAliquota = item.getValorUnitario() * aliquota;

        return ItemNotaFiscal.builder()
                .idItem(item.getIdItem())
                .descricao(item.getDescricao())
                .valorUnitario(item.getValorUnitario())
                .quantidade(item.getQuantidade())
                .valorTributoItem(valorComAliquota)
                .build();
    }
}
