
# Análise e Refatoração - Desafio Nota Fiscal

## 1. Código difícil de manter e alterar

O principal problema da classe original era a violação do princípio da responsabilidade única (SRP - Single Responsibility Principle). A classe centralizava múltiplas responsabilidades ao mesmo tempo: cálculo de impostos e frete, criação da nota fiscal e ainda orquestração com serviços externos como estoque e financeiro. Isso tornava o código difícil de testar, evoluir ou dar manutenção.

## 2. Muitas regras de negócio acopladas e fluxo complexo

O código misturava diferentes tipos de lógica (negócio, integração, criação de objetos) em um único fluxo, com duplicações e baixa coesão. Essa estrutura dificultava a leitura e tornava o fluxo de geração da nota fiscal propenso a erros e efeitos colaterais.

## 3. Classe principal instável

A classe `GeradorNotaFiscalServiceImpl` foi refatorada para atuar como uma coordenadora do processo de geração da nota fiscal. A lógica foi distribuída para serviços especializados:

- `CalculadoraAliquotaServiceImpl`: responsável pelo cálculo da alíquota
- `CalculadoraFreteServiceImpl`: responsável pelo cálculo do frete
- `ItemNotaFiscalFactory`: responsável por gerar os itens da nota
- `NotaFiscalFactory`: responsável por construir o objeto da nota fiscal
- Serviços de integração (`EstoqueService`, `EntregaService`, `FinanceiroService`, `RegistroService`) foram isolados para tratar suas responsabilidades específicas

A `GeradorNotaFiscalServiceImpl` agora apenas coordena essas operações, sem conhecer os detalhes de implementação, tornando o código mais limpo e desacoplado.

Além disso, todas as dependências são injetadas via construtor, o que facilita a testabilidade com mocks e é compatível com frameworks como Spring.

## 4. Testes frágeis e baixa cobertura

Após as refatorações, os testes foram reestruturados com foco em cobertura, clareza e isolamento. Também foram corrigidas falhas que aconteciam em execuções consecutivas, por conta de estados compartilhados entre testes.

---

## Problemas funcionais e suas causas

### 1. Primeira execução funciona, mas as seguintes acumulam itens

Foi identificado que a classe `CalculadoraAliquotaProduto` utilizava uma lista estática:

```java
private static List<ItemNotaFiscal> itemNotaFiscalList = new ArrayList<>();
```

Essa lista acumulava os itens a cada execução, o que causava inconsistências nos testes e nas integrações.

**Solução:** O estado estático foi removido, e o processamento passou a ser feito com base apenas nos dados da requisição atual.

### 2. Lentidão com pedidos acima de 6 itens

Na classe `EntregaIntegrationPort`, havia uma pegadinha com `Thread.sleep(5000)` sempre que a nota fiscal tivesse mais de 5 itens.

**Solução:** Esse código foi identificado e removido, conforme instrução, para evitar impacto no desempenho real da aplicação.

### 3. Lentidão após várias execuções

Essa lentidão estava relacionada tanto ao `Thread.sleep` acima quanto ao uso da lista estática, que acumulava cada vez mais dados em memória.

Após resolver esses dois pontos, o problema deixou de ocorrer.

### 4. Inconsistência nos valores da nota e quantidade de itens

O valor total dos itens vem do payload, mas como os itens estavam sendo acumulados por conta da lista estática, o número de itens da nota e o total acabavam divergentes.

**Solução:** A lógica foi ajustada para processar apenas os itens recebidos em cada requisição, garantindo consistência entre o valor informado e a lista de itens.

### 5. Demais alterações 

Troca dos tipos dos atributos de valores de ***double*** para ***BigDecimal*** para evitar problemas de precisão nos cálculos.

Adicionei um tratamento de erro simples na controller

Criação de classe para validar o pedido