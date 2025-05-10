package br.com.itau.geradornotafiscal.service.impl;
import br.com.itau.geradornotafiscal.exception.*;
import br.com.itau.geradornotafiscal.factory.ItemNotaFiscalFactory;
import br.com.itau.geradornotafiscal.factory.NotaFiscalFactory;
import br.com.itau.geradornotafiscal.model.*;
import br.com.itau.geradornotafiscal.service.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GeradorNotaFiscalServiceImpl implements GeradorNotaFiscalService {
    private final CalculadoraFreteService calculadoraFreteService;
    private final CalculadoraAliquotaService calculadoraAliquotaService;
    private final EstoqueService estoqueService;
    private final RegistroService registroService;
    private final EntregaService entregaService;
    private final FinanceiroService financeiroService;
    private final ItemNotaFiscalFactory itemNotaFiscalFactory;
    private final NotaFiscalFactory notaFiscalFactory;

    public GeradorNotaFiscalServiceImpl(CalculadoraFreteService calculadoraFreteService,
                                        CalculadoraAliquotaService calculadoraAliquotaService,
                                        EstoqueService estoqueService,
                                        RegistroService registroService,
                                        EntregaService entregaService,
                                        FinanceiroService financeiroService,
                                        ItemNotaFiscalFactory itemNotaFiscalFactory,
                                        NotaFiscalFactory notaFiscalFactory) {
        this.calculadoraFreteService = calculadoraFreteService;
        this.calculadoraAliquotaService = calculadoraAliquotaService;
        this.estoqueService = estoqueService;
        this.registroService = registroService;
        this.entregaService = entregaService;
        this.financeiroService = financeiroService;
        this.itemNotaFiscalFactory = itemNotaFiscalFactory;
        this.notaFiscalFactory = notaFiscalFactory;

    }

    //GeradorNotaFiscalServiceImpl - Valida o pedido, orquestra a execução e coordena as chamadas aos serviços externos.
    @Override
    public NotaFiscal gerarNotaFiscal(Pedido pedido) {

        validarPedido(pedido);

        double valorAliquota = calculadoraAliquotaService.calcularAliquota(pedido);
        double valorFrete = calculadoraFreteService.calcularFrete(pedido);
        List<ItemNotaFiscal> itensNotaFiscal = itemNotaFiscalFactory.gerarItensNotaFiscal(pedido, valorAliquota);
        NotaFiscal notaFiscal = notaFiscalFactory.criarNotaFiscal(pedido, itensNotaFiscal, valorFrete);

        try {
            estoqueService.enviarNotaFiscalParaBaixaEstoque(notaFiscal);
        } catch (Exception e) {
            throw new EstoqueNotaFiscalException("Erro ao baixar estoque para a nota fiscal: " + e.getMessage());
        }

        try {
            registroService.registrarNotaFiscal(notaFiscal);
        } catch (Exception e) {
            throw new RegistroNotaFiscalException("Erro ao registrar a nota fiscal: " + e.getMessage());
        }

        try {
            entregaService.agendarEntrega(notaFiscal);
        } catch (Exception e) {
            throw new EntregaNotaFiscalException("Erro ao agendar entrega para a nota fiscal: " + e.getMessage());
        }

        try {
            financeiroService.enviarNotaFiscalParaContasReceber(notaFiscal);
        } catch (Exception e) {
            throw new FinanceiroNotaFiscalException("Erro ao enviar a nota fiscal para o financeiro: " + e.getMessage());
        }

        return notaFiscal;
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new PedidoInvalidoException("Pedido não pode ser nulo.");
        }

        Destinatario destinatario = pedido.getDestinatario();
        if (destinatario == null) {
            throw new PedidoInvalidoException("Destinatário do pedido não pode ser nulo.");
        }

        if (destinatario.getTipoPessoa() == null) {
            throw new PedidoInvalidoException("Tipo de pessoa do destinatário não pode ser nulo.");
        }

        if (pedido.getDestinatario().getTipoPessoa() == TipoPessoa.JURIDICA) {
            RegimeTributacaoPJ regime = pedido.getDestinatario().getRegimeTributacao();
            if (regime == null) {
                throw new PedidoInvalidoException("Regime tributário não pode ser nulo para pessoas jurídicas.");
            }
        }

        if (pedido.getValorTotalItens() <= 0) {
            throw new PedidoInvalidoException("Valor total dos itens deve ser maior que zero.");
        }

        if (pedido.getValorFrete() < 0) {
            throw new PedidoInvalidoException("Valor do frete não pode ser negativo.");
        }

        if (destinatario.getEnderecos() == null || destinatario.getEnderecos().isEmpty()) {
            throw new PedidoInvalidoException("Destinatário deve conter ao menos um endereço.");
        }

        Optional<Endereco> enderecoEntregaOpt = destinatario.getEnderecos().stream()
                .filter(e -> e.getFinalidade() == Finalidade.ENTREGA || e.getFinalidade() == Finalidade.COBRANCA_ENTREGA)
                .findFirst();

        if (enderecoEntregaOpt.isPresent()) {
            Endereco enderecoEntrega = enderecoEntregaOpt.get();
            if (enderecoEntrega.getRegiao() == null) {
                throw new PedidoInvalidoException("Região não pode ser nula para o endereço de entrega.");
            }
        } else {
            throw new PedidoInvalidoException("Destinatário deve conter pelo menos um endereço com finalidade de ENTREGA ou COBRANCA_ENTREGA.");
        }
    }
}