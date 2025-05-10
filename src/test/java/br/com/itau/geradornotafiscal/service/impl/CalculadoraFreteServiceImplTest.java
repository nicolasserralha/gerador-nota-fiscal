package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.*;
        import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraFreteServiceImplTest {

    private CalculadoraFreteServiceImpl calculadoraFreteService;

    @BeforeEach
    void setUp() {
        calculadoraFreteService = new CalculadoraFreteServiceImpl();
    }

    @Test
    void testCalcularFrete_Norte() {
        // Criando mock para pedido e destinatário
        Pedido pedido = criarPedido(50.0, Regiao.NORTE);

        double valorFreteCalculado = calculadoraFreteService.calcularFrete(pedido);
        double valorEsperado = 50.0 * 1.08;

        assertEquals(valorEsperado, valorFreteCalculado, "O valor do frete para a região Norte está incorreto.");
    }

    @Test
    void testCalcularFrete_Nordeste() {
        Pedido pedido = criarPedido(100.0, Regiao.NORDESTE);

        double valorFreteCalculado = calculadoraFreteService.calcularFrete(pedido);
        double valorEsperado = 100.0 * 1.085;

        assertEquals(valorEsperado, valorFreteCalculado, "O valor do frete para a região Nordeste está incorreto.");
    }

    @Test
    void testCalcularFrete_CentroOeste() {
        Pedido pedido = criarPedido(100.0, Regiao.CENTRO_OESTE);

        double valorFreteCalculado = calculadoraFreteService.calcularFrete(pedido);
        double valorEsperado = 100.0 * 1.07;

        assertEquals(valorEsperado, valorFreteCalculado, "O valor do frete para a região Centro-Oeste está incorreto.");
    }

    @Test
    void testCalcularFrete_Sudeste() {
        Pedido pedido = criarPedido(100.0, Regiao.SUDESTE);

        double valorFreteCalculado = calculadoraFreteService.calcularFrete(pedido);
        double valorEsperado = 100.0 * 1.048;

        assertEquals(valorEsperado, valorFreteCalculado, "O valor do frete para a região Sudeste está incorreto.");
    }

    @Test
    void testCalcularFrete_Sul() {
        Pedido pedido = criarPedido(100.0, Regiao.SUL);

        double valorFreteCalculado = calculadoraFreteService.calcularFrete(pedido);
        double valorEsperado = 100.0 * 1.06;

        assertEquals(valorEsperado, valorFreteCalculado, "O valor do frete para a região Sul está incorreto.");
    }

    @Test
    void testCalcularFrete_RegiaoNaoEncontrada() {
        Pedido pedido = criarPedido(100.0, null);

        assertThrows(PedidoInvalidoException.class, () -> {
            calculadoraFreteService.calcularFrete(pedido);
        }, "Região da entrega não encontrada.");
    }

    private Pedido criarPedido(double valorFrete, Regiao regiao) {
        Pedido pedido = new Pedido();
        pedido.setValorFrete(valorFrete);

        Destinatario destinatario = Mockito.mock(Destinatario.class);
        Endereco endereco = Mockito.mock(Endereco.class);

        if (regiao != null) {
            Mockito.when(endereco.getFinalidade()).thenReturn(Finalidade.ENTREGA);
            Mockito.when(endereco.getRegiao()).thenReturn(regiao);
        } else {
            Mockito.when(endereco.getFinalidade()).thenReturn(Finalidade.OUTROS);
        }

        Mockito.when(destinatario.getEnderecos()).thenReturn(java.util.List.of(endereco));
        pedido.setDestinatario(destinatario);

        return pedido;
    }
}
