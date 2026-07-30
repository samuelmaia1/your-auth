# Package `infra.persistence`

## Finalidade

Concentrar modelos e componentes diretamente ligados a persistência de dados.

## Motivo

Separar estruturas de banco de dados do modelo de domínio, permitindo que
entidades, tabelas e detalhes de armazenamento evoluam sem contaminar regras de
negócio.

## Quando colocar arquivos aqui

- O arquivo representa uma entidade ou modelo persistido.
- O arquivo define componentes auxiliares específicos de armazenamento.
- O arquivo depende de anotações ou contratos de persistência.
