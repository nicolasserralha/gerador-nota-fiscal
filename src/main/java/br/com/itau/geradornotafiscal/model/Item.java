package br.com.itau.geradornotafiscal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class Item {
	@JsonProperty("id_item")
	private String idItem;

	@JsonProperty("descricao")
	private String descricao;

	@JsonProperty("valor_unitario")
	private BigDecimal valorUnitario;

	@JsonProperty("quantidade")
	private int quantidade;
}
