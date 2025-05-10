package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.exception.*;
import br.com.itau.geradornotafiscal.factory.ItemNotaFiscalFactory;
import br.com.itau.geradornotafiscal.factory.NotaFiscalFactory;
import br.com.itau.geradornotafiscal.model.*;
import br.com.itau.geradornotafiscal.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class GeradorNotaFiscalServiceImplTest {

    @Mock
    private CalculadoraFreteService calculadoraFreteService;

    @Mock
    private CalculadoraAliquotaService calculadoraAliquotaService;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private RegistroService registroService;

    @Mock
    private EntregaService entregaService;

    @Mock
    private FinanceiroService financeiroService;

    @Mock
    private ItemNotaFiscalFactory itemNotaFiscalFactory;

    @Mock
    private NotaFiscalFactory notaFiscalFactory;

    @InjectMocks
    private GeradorNotaFiscalServiceImpl geradorNotaFiscalService;

    private Pedido pedido;
    private NotaFiscal notaFiscalEsperada;
    private ItemNotaFiscal item;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setValorTotalItens(BigDecimal.valueOf(100.0));
        pedido.setValorFrete(BigDecimal.valueOf(10.0));
        Destinatario destinatario = new Destinatario();
        destinatario.setTipoPessoa(TipoPessoa.FISICA);
        Endereco endereco = new Endereco();
        endereco.setFinalidade(Finalidade.ENTREGA);
        endereco.setRegiao(Regiao.SUDESTE);
        destinatario.setEnderecos(Collections.singletonList(endereco));
        pedido.setDestinatario(destinatario);

        item = new ItemNotaFiscal();
        notaFiscalEsperada = new NotaFiscal();
    }

    @Test
    void deveGerarNotaFiscalCorretamente() {
        when(calculadoraAliquotaService.calcularAliquota(pedido)).thenReturn(BigDecimal.valueOf(0.2));
        when(calculadoraFreteService.calcularFrete(pedido)).thenReturn(BigDecimal.valueOf(50.0));
        when(itemNotaFiscalFactory.gerarItensNotaFiscal(pedido, BigDecimal.valueOf(0.2))).thenReturn(Collections.singletonList(item));
        when(notaFiscalFactory.criarNotaFiscal(pedido, Collections.singletonList(item), BigDecimal.valueOf(50.0))).thenReturn(notaFiscalEsperada);

        NotaFiscal resultado = geradorNotaFiscalService.gerarNotaFiscal(pedido);

        assertEquals(notaFiscalEsperada, resultado);
        verifyServicosChamados(notaFiscalEsperada);
    }

    @Test
    void deveGerarNotaFiscalComFreteZero() {
        when(calculadoraAliquotaService.calcularAliquota(pedido)).thenReturn(BigDecimal.valueOf(0.1));
        when(calculadoraFreteService.calcularFrete(pedido)).thenReturn(BigDecimal.valueOf(0.0));
        NotaFiscal notaFiscalMock = mock(NotaFiscal.class);
        List<ItemNotaFiscal> itens = Collections.singletonList(mock(ItemNotaFiscal.class));
        when(itemNotaFiscalFactory.gerarItensNotaFiscal(pedido, BigDecimal.valueOf(0.1))).thenReturn(itens);
        when(notaFiscalFactory.criarNotaFiscal(pedido, itens, BigDecimal.valueOf(0.0))).thenReturn(notaFiscalMock);

        NotaFiscal resultado = geradorNotaFiscalService.gerarNotaFiscal(pedido);

        assertNotNull(resultado);
        verifyServicosChamados(notaFiscalMock);
    }

    @Test
    void deveLancarExcecaoQuandoServicoFalhar() {
        testarFalhaNoServico(EstoqueNotaFiscalException.class, "Erro ao baixar estoque para a nota fiscal", estoqueService, "Erro no estoque");
        testarFalhaNoServico(RegistroNotaFiscalException.class, "Erro ao registrar a nota fiscal", registroService, "Erro no registro");
        testarFalhaNoServico(EntregaNotaFiscalException.class, "Erro ao agendar entrega para a nota fiscal", entregaService, "Erro na entrega");
        testarFalhaNoServico(FinanceiroNotaFiscalException.class, "Erro ao enviar a nota fiscal para o financeiro", financeiroService, "Erro no financeiro");
    }

    @Test
    void deveLancarPedidoInvalidoExceptionQuandoPedidoForNulo() {
        Pedido pedidoNulo = null;
        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class, () -> {
            geradorNotaFiscalService.gerarNotaFiscal(pedidoNulo);
        });
        assertEquals("Pedido não pode ser nulo.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoDestinatarioForNulo() {
        pedido.setDestinatario(null);

        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class, () ->
                geradorNotaFiscalService.gerarNotaFiscal(pedido)
        );
        assertEquals("Destinatário do pedido não pode ser nulo.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoTipoPessoaForNulo() {
        Destinatario destinatario = pedido.getDestinatario();
        destinatario.setTipoPessoa(null);

        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class, () ->
                geradorNotaFiscalService.gerarNotaFiscal(pedido)
        );
        assertEquals("Tipo de pessoa do destinatário não pode ser nulo.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoTipoPessoaForJuridicaSemRegimeTributario() {
        Destinatario destinatario = pedido.getDestinatario();
        destinatario.setTipoPessoa(TipoPessoa.JURIDICA);
        destinatario.setRegimeTributacao(null);

        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class, () ->
                geradorNotaFiscalService.gerarNotaFiscal(pedido)
        );
        assertEquals("Regime tributário não pode ser nulo para pessoas jurídicas.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoValorTotalItensForZero() {
        pedido.setValorTotalItens(BigDecimal.valueOf(0.0));

        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class, () ->
                geradorNotaFiscalService.gerarNotaFiscal(pedido)
        );
        assertEquals("Valor total dos itens deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoValorFreteForNegativo() {
        pedido.setValorFrete(BigDecimal.valueOf(-10.0));

        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class, () ->
                geradorNotaFiscalService.gerarNotaFiscal(pedido)
        );
        assertEquals("Valor do frete não pode ser negativo.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoListaDeEnderecosForVazia() {
        pedido.getDestinatario().setEnderecos(Collections.emptyList());

        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class, () ->
                geradorNotaFiscalService.gerarNotaFiscal(pedido)
        );
        assertEquals("Destinatário deve conter ao menos um endereço.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverEnderecoComFinalidadeEntregaOuCobrancaEntrega() {
        Endereco endereco = new Endereco();
        endereco.setFinalidade(Finalidade.COBRANCA); // inválida para entrega
        pedido.getDestinatario().setEnderecos(Collections.singletonList(endereco));

        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class, () ->
                geradorNotaFiscalService.gerarNotaFiscal(pedido)
        );
        assertEquals("Destinatário deve conter pelo menos um endereço com finalidade de ENTREGA ou COBRANCA_ENTREGA.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverEnderecoComFinalidadeEntregaOuCobrancaEntregaSemRegiao() {
        Endereco endereco = new Endereco();
        endereco.setFinalidade(Finalidade.ENTREGA);
        pedido.getDestinatario().setEnderecos(Collections.singletonList(endereco));

        // Espera-se que a exceção seja lançada
        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class, () -> {
            geradorNotaFiscalService.gerarNotaFiscal(pedido);
        });

        assertEquals("Região não pode ser nula para o endereço de entrega.", exception.getMessage());
    }
    private void testarFalhaNoServico(Class<? extends RuntimeException> excecaoEsperada, String mensagemEsperada, Object servicoMock, String erro) {
        NotaFiscal notaFiscalMock = mock(NotaFiscal.class);
        List<ItemNotaFiscal> itens = Collections.singletonList(mock(ItemNotaFiscal.class));

        when(calculadoraAliquotaService.calcularAliquota(pedido)).thenReturn(BigDecimal.valueOf(0.1));
        when(calculadoraFreteService.calcularFrete(pedido)).thenReturn(BigDecimal.valueOf(15.0));
        when(itemNotaFiscalFactory.gerarItensNotaFiscal(pedido, BigDecimal.valueOf(0.1))).thenReturn(itens);
        when(notaFiscalFactory.criarNotaFiscal(pedido, itens, BigDecimal.valueOf(15.0))).thenReturn(notaFiscalMock);

        if (servicoMock == estoqueService) {
            doThrow(new RuntimeException(erro)).when(estoqueService).enviarNotaFiscalParaBaixaEstoque(notaFiscalMock);
        } else if (servicoMock == registroService) {
            doThrow(new RuntimeException(erro)).when(registroService).registrarNotaFiscal(notaFiscalMock);
        } else if (servicoMock == entregaService) {
            doThrow(new RuntimeException(erro)).when(entregaService).agendarEntrega(notaFiscalMock);
        } else if (servicoMock == financeiroService) {
            doThrow(new RuntimeException(erro)).when(financeiroService).enviarNotaFiscalParaContasReceber(notaFiscalMock);
        }

        RuntimeException exception = assertThrows(excecaoEsperada, () ->
                geradorNotaFiscalService.gerarNotaFiscal(pedido)
        );

        assertEquals(mensagemEsperada + ": " + erro, exception.getMessage());
    }

    private void verifyServicosChamados(NotaFiscal notaFiscal) {
        verify(estoqueService).enviarNotaFiscalParaBaixaEstoque(notaFiscal);
        verify(registroService).registrarNotaFiscal(notaFiscal);
        verify(entregaService).agendarEntrega(notaFiscal);
        verify(financeiroService).enviarNotaFiscalParaContasReceber(notaFiscal);
    }

}
