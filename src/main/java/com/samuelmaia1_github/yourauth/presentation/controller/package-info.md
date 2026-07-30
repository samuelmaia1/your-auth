# Package `presentation.controller`

## Finalidade

Agrupar controllers responsáveis pelos endpoints da API.

## Motivo

Manter a camada HTTP como ponto de entrada da aplicação, delegando regras de
negócio para o domínio e detalhes técnicos para a infraestrutura.

## Quando colocar arquivos aqui

- O arquivo define endpoints, rotas e status HTTP.
- O arquivo recebe requests e retorna responses da API.
- O arquivo coordena chamadas para services sem implementar regra de negócio.
