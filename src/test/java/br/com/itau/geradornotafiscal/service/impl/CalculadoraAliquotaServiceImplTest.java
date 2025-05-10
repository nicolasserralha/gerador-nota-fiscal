package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraAliquotaServiceImplTest {

    private CalculadoraAliquotaServiceImpl calculadoraAliquotaService;
    private Pedido pedido;
    private Destinatario destinatario;

    @BeforeEach
    void setUp() {
        calculadoraAliquotaService = new CalculadoraAliquotaServiceImpl();
    }

    private Pedido criarPedido(double valorTotal, TipoPessoa tipoPessoa, RegimeTributacaoPJ regime) {
        Destinatario destinatario = Destinatario.builder()
                .tipoPessoa(tipoPessoa)
                .regimeTributacao(regime)
                .build();

        return Pedido.builder()
                .valorTotalItens(valorTotal)
                .destinatario(destinatario)
                .build();
    }

    @Test
    void deveCalcularAliquotaCorretamenteParaPessoaFisica() {
        assertEquals(0.0, calculadoraAliquotaService.calcularAliquota(criarPedido(400, TipoPessoa.FISICA, null)));
        assertEquals(0.12, calculadoraAliquotaService.calcularAliquota(criarPedido(1500, TipoPessoa.FISICA, null)));
        assertEquals(0.15, calculadoraAliquotaService.calcularAliquota(criarPedido(3000, TipoPessoa.FISICA, null)));
        assertEquals(0.17, calculadoraAliquotaService.calcularAliquota(criarPedido(4000, TipoPessoa.FISICA, null)));
    }

    @Test
    void deveCalcularAliquotaCorretamenteParaPessoaJuridicaSimplesNacional() {
        assertEquals(0.03, calculadoraAliquotaService.calcularAliquota(criarPedido(800, TipoPessoa.JURIDICA, RegimeTributacaoPJ.SIMPLES_NACIONAL)));
        assertEquals(0.07, calculadoraAliquotaService.calcularAliquota(criarPedido(1500, TipoPessoa.JURIDICA, RegimeTributacaoPJ.SIMPLES_NACIONAL)));
        assertEquals(0.13, calculadoraAliquotaService.calcularAliquota(criarPedido(3000, TipoPessoa.JURIDICA, RegimeTributacaoPJ.SIMPLES_NACIONAL)));
        assertEquals(0.19, calculadoraAliquotaService.calcularAliquota(criarPedido(6000, TipoPessoa.JURIDICA, RegimeTributacaoPJ.SIMPLES_NACIONAL)));
    }

    @Test
    void deveCalcularAliquotaCorretamenteParaPessoaJuridicaLucroReal() {
        assertEquals(0.03, calculadoraAliquotaService.calcularAliquota(criarPedido(800, TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL)));
        assertEquals(0.09, calculadoraAliquotaService.calcularAliquota(criarPedido(1500, TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL)));
        assertEquals(0.15, calculadoraAliquotaService.calcularAliquota(criarPedido(3000, TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL)));
        assertEquals(0.20, calculadoraAliquotaService.calcularAliquota(criarPedido(6000, TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_REAL)));
    }

    @Test
    void deveCalcularAliquotaCorretamenteParaPessoaJuridicaLucroPresumido() {
        assertEquals(0.03, calculadoraAliquotaService.calcularAliquota(criarPedido(800, TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_PRESUMIDO)));
        assertEquals(0.09, calculadoraAliquotaService.calcularAliquota(criarPedido(1500, TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_PRESUMIDO)));
        assertEquals(0.16, calculadoraAliquotaService.calcularAliquota(criarPedido(3000, TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_PRESUMIDO)));
        assertEquals(0.20, calculadoraAliquotaService.calcularAliquota(criarPedido(6000, TipoPessoa.JURIDICA, RegimeTributacaoPJ.LUCRO_PRESUMIDO)));
    }

    @Test
    void deveLancarExcecaoParaTipoInvalido() {
        Destinatario destinatario = Destinatario.builder().tipoPessoa(null).build();
        Pedido pedido = Pedido.builder().valorTotalItens(1000).destinatario(destinatario).build();
        assertThrows(PedidoInvalidoException.class, () -> calculadoraAliquotaService.calcularAliquota(pedido));
    }

    @Test
    void deveLancarExcecaoParaRegimeInvalido() {
        Destinatario destinatario = Destinatario.builder().tipoPessoa(TipoPessoa.JURIDICA).regimeTributacao(null).build();
        Pedido pedido = Pedido.builder().valorTotalItens(1000).destinatario(destinatario).build();
        assertThrows(PedidoInvalidoException.class, () -> calculadoraAliquotaService.calcularAliquota(pedido));
    }
}


