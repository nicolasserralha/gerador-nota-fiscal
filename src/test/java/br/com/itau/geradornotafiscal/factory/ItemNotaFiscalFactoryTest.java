package br.com.itau.geradornotafiscal.factory;

import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemNotaFiscalFactoryTest {

    private final ItemNotaFiscalFactory factory = new ItemNotaFiscalFactory();

    @Test
    void deveGerarItensNotaFiscalComAliquotaCorreta() {
        Item item1 = Item.builder().idItem("1").descricao("Item 1").valorUnitario(BigDecimal.valueOf(100.0)).quantidade(2).build();
        Item item2 = Item.builder().idItem("2").descricao("Item 2").valorUnitario(BigDecimal.valueOf(200.0)).quantidade(1).build();

        Pedido pedido = Pedido.builder().itens(Arrays.asList(item1, item2)).build();

        BigDecimal aliquota = BigDecimal.valueOf(1.1);

        List<ItemNotaFiscal> itensNotaFiscal = factory.gerarItensNotaFiscal(pedido, aliquota);

        assertEquals(2, itensNotaFiscal.size());

        ItemNotaFiscal resultado1 = itensNotaFiscal.get(0);
        assertEquals("1", resultado1.getIdItem());
        assertEquals(0, resultado1.getValorUnitario().compareTo(BigDecimal.valueOf(100.0)));
        assertEquals(0, resultado1.getValorTributoItem().compareTo(BigDecimal.valueOf(110.0)));

        ItemNotaFiscal resultado2 = itensNotaFiscal.get(1);
        assertEquals("2", resultado2.getIdItem());
        assertEquals(0, resultado2.getValorUnitario().compareTo(BigDecimal.valueOf(200.0)));
        assertEquals(0, resultado2.getValorTributoItem().compareTo(BigDecimal.valueOf(220.0)));
    }
}
