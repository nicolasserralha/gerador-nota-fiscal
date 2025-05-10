package br.com.itau.geradornotafiscal.service.impl;
import br.com.itau.geradornotafiscal.exception.*;
import br.com.itau.geradornotafiscal.factory.ItemNotaFiscalFactory;
import br.com.itau.geradornotafiscal.factory.NotaFiscalFactory;
import br.com.itau.geradornotafiscal.model.*;
import br.com.itau.geradornotafiscal.service.*;
import br.com.itau.geradornotafiscal.validation.ValidadorPedido;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

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
    private final ValidadorPedido validadorPedido;

    public GeradorNotaFiscalServiceImpl(CalculadoraFreteService calculadoraFreteService,
                                        CalculadoraAliquotaService calculadoraAliquotaService,
                                        EstoqueService estoqueService,
                                        RegistroService registroService,
                                        EntregaService entregaService,
                                        FinanceiroService financeiroService,
                                        ItemNotaFiscalFactory itemNotaFiscalFactory,
                                        NotaFiscalFactory notaFiscalFactory,
                                        ValidadorPedido validadorPedido) {
        this.calculadoraFreteService = calculadoraFreteService;
        this.calculadoraAliquotaService = calculadoraAliquotaService;
        this.estoqueService = estoqueService;
        this.registroService = registroService;
        this.entregaService = entregaService;
        this.financeiroService = financeiroService;
        this.itemNotaFiscalFactory = itemNotaFiscalFactory;
        this.notaFiscalFactory = notaFiscalFactory;
        this.validadorPedido = validadorPedido;
    }

    //GeradorNotaFiscalServiceImpl - orquestra a execução e coordena as chamadas aos serviços externos.
    @Override
    public NotaFiscal gerarNotaFiscal(Pedido pedido) {

        validadorPedido.validarPedido(pedido);

        BigDecimal valorAliquota = calculadoraAliquotaService.calcularAliquota(pedido);
        BigDecimal valorFrete = calculadoraFreteService.calcularFrete(pedido);
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
}