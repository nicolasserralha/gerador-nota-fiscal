package br.com.itau.geradornotafiscal.validation;

import br.com.itau.geradornotafiscal.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ValidadorPedidoTest {

    private ValidadorPedido validadorPedido;

    @Mock
    private Pedido pedido;

    @Mock
    private Destinatario destinatario;

    @Mock
    private Endereco endereco;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validadorPedido = new ValidadorPedido();
    }

    @Test
    public void deveLancarExcecaoQuandoPedidoForNull() {
        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(null));
    }

    @Test
    public void deveLancarExcecaoQuandoDestinatarioForNull() {
        when(pedido.getDestinatario()).thenReturn(null);
        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(pedido));
    }

    @Test
    public void deveLancarExcecaoQuandoTipoPessoaForNull() {
        when(pedido.getDestinatario()).thenReturn(destinatario);
        when(destinatario.getTipoPessoa()).thenReturn(null);
        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(pedido));
    }

    @Test
    public void deveLancarExcecaoQuandoTipoPessoaJuridicaERegimeForNull() {
        when(pedido.getDestinatario()).thenReturn(destinatario);
        when(destinatario.getTipoPessoa()).thenReturn(TipoPessoa.JURIDICA);
        when(destinatario.getRegimeTributacao()).thenReturn(null);
        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(pedido));
    }

    @Test
    public void deveLancarExcecaoQuandoValorTotalForMenorOuIgualAZero() {
        when(pedido.getDestinatario()).thenReturn(destinatario);
        when(pedido.getValorTotalItens()).thenReturn(BigDecimal.ZERO);
        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(pedido));
    }

    @Test
    public void deveLancarExcecaoQuandoValorFreteForNegativo() {
        when(pedido.getDestinatario()).thenReturn(destinatario);
        when(pedido.getValorFrete()).thenReturn(BigDecimal.valueOf(-1));
        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(pedido));
    }

    @Test
    public void deveLancarExcecaoQuandoEnderecoNullOuVazio() {
        when(pedido.getDestinatario()).thenReturn(destinatario);
        when(destinatario.getEnderecos()).thenReturn(null);
        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(pedido));

        when(destinatario.getEnderecos()).thenReturn(Collections.emptyList());
        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(pedido));
    }

    @Test
    public void deveLancarExcecaoQuandoEnderecoEntregaForNull() {
        when(pedido.getDestinatario()).thenReturn(destinatario);
        when(destinatario.getEnderecos()).thenReturn(Collections.singletonList(endereco));
        when(endereco.getFinalidade()).thenReturn(Finalidade.COBRANCA);

        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(pedido));
    }

    @Test
    public void deveLancarExcecaoQuandoRegiaoDeEntregaForNull() {
        when(pedido.getDestinatario()).thenReturn(destinatario);
        when(destinatario.getEnderecos()).thenReturn(Collections.singletonList(endereco));
        when(endereco.getFinalidade()).thenReturn(Finalidade.ENTREGA);
        when(endereco.getRegiao()).thenReturn(null);

        assertThrows(PedidoInvalidoException.class, () -> validadorPedido.validarPedido(pedido));
    }

    @Test
    public void devePassarQuandoTudoEstiverCorreto() {
        when(pedido.getDestinatario()).thenReturn(destinatario);
        when(destinatario.getTipoPessoa()).thenReturn(TipoPessoa.FISICA);
        when(pedido.getValorTotalItens()).thenReturn(BigDecimal.TEN);
        when(pedido.getValorFrete()).thenReturn(BigDecimal.ONE);
        when(destinatario.getEnderecos()).thenReturn(Collections.singletonList(endereco));
        when(endereco.getFinalidade()).thenReturn(Finalidade.ENTREGA);
        when(endereco.getRegiao()).thenReturn(Regiao.SUDESTE);

        assertDoesNotThrow(() -> validadorPedido.validarPedido(pedido));
    }
}
