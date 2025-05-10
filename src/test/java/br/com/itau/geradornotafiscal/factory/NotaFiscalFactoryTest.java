package br.com.itau.geradornotafiscal.factory;

import br.com.itau.geradornotafiscal.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NotaFiscalFactoryTest {

    private final NotaFiscalFactory factory = new NotaFiscalFactory();

    @Test
    void deveCriarNotaFiscalComValoresCorretos() {
        Destinatario destinatario = Destinatario.builder().nome("Cliente A").build();
        Pedido pedido = Pedido.builder().valorTotalItens(BigDecimal.valueOf(300.0)).destinatario(destinatario).build();

        ItemNotaFiscal itemNotaFiscal = ItemNotaFiscal.builder()
                .idItem("123")
                .descricao("Produto Teste")
                .quantidade(1)
                .valorUnitario(BigDecimal.valueOf(100.0))
                .valorTributoItem(BigDecimal.valueOf(10.0))
                .build();

        BigDecimal valorFrete = BigDecimal.valueOf(50.0);

        NotaFiscal notaFiscal = factory.criarNotaFiscal(pedido, List.of(itemNotaFiscal), valorFrete);

        assertNotNull(notaFiscal.getIdNotaFiscal());
        assertNotNull(notaFiscal.getData());
        assertEquals(0, notaFiscal.getValorTotalItens().compareTo(BigDecimal.valueOf(300.0)));
        assertEquals(0, notaFiscal.getValorFrete().compareTo(BigDecimal.valueOf(50.0)));
        assertEquals(1, notaFiscal.getItens().size());
        assertEquals(destinatario, notaFiscal.getDestinatario());
    }
}
