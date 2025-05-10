package br.com.itau.geradornotafiscal.web.controller;

import br.com.itau.geradornotafiscal.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.model.NotaFiscal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.service.GeradorNotaFiscalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedido")
public class GeradorNFController {

	private static final Logger logger = LoggerFactory.getLogger(GeradorNFController.class);

	private final GeradorNotaFiscalService notaFiscalService;

	public GeradorNFController(GeradorNotaFiscalService notaFiscalService) {
		this.notaFiscalService = notaFiscalService;
	}

	@PostMapping("/gerarNotaFiscal")
	public ResponseEntity<?> gerarNotaFiscal(@RequestBody Pedido pedido) {
		try {
			NotaFiscal notaFiscal = notaFiscalService.gerarNotaFiscal(pedido);
			logger.info("Nota fiscal gerada com sucesso para o pedido: {}", pedido.getIdPedido());
			return ResponseEntity.ok(notaFiscal);
		} catch (PedidoInvalidoException ex) {
			logger.info("Pedido Invalido: {}", ex.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		} catch (Exception ex) {
			logger.info("Erro: {}", ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado ao gerar a nota fiscal.");
		}
	}
}

