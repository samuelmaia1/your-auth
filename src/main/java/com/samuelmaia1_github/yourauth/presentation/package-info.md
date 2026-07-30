# Package `presentation`

## Finalidade

Concentrar a entrada e saída HTTP da aplicação.

## Motivo

Separar contratos de API, controllers, DTOs e tratamento de erros dos detalhes de
infraestrutura e das regras de negócio.

## Quando colocar arquivos aqui

- O arquivo representa comportamento exposto pela API HTTP.
- O arquivo define contratos de request, response ou tratamento de erro.
- O arquivo adapta dados entre a API e o domínio.
