# Package `presentation.dto`

## Finalidade

Concentrar DTOs usados como contratos de entrada e saída da API.

## Motivo

Evitar que modelos internos de domínio ou persistência sejam expostos diretamente
pelos endpoints.

## Quando colocar arquivos aqui

- O arquivo representa payloads de request ou response.
- O arquivo define contratos públicos da API.
- A estrutura existe para trafegar dados, sem regra de negócio.
