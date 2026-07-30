# Package `infra.beans`

## Finalidade

Declarar beans de infraestrutura usados pela aplicação.

## Motivo

Centralizar a criação e customização de componentes técnicos que precisam ser
gerenciados pelo container do Spring.

## Quando colocar arquivos aqui

- O arquivo expõe um bean técnico via métodos anotados com `@Bean`.
- O arquivo configura instâncias compartilhadas por outras camadas.
- A classe existe principalmente para composição do contexto Spring.
