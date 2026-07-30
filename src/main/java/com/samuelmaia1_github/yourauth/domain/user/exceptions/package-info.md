# Package `domain.user.exceptions`

## Finalidade

Concentrar exceções de domínio relacionadas ao contexto de usuário.

## Motivo

Comunicar falhas de regras de negócio de forma explícita, sem misturar essas
falhas com exceptions técnicas de infraestrutura ou HTTP.

## Quando colocar arquivos aqui

- O arquivo representa uma violação de regra de negócio de usuário.
- A falha pode ser entendida sem depender de banco, rede ou HTTP.
- A exception é lançada pelo domínio de usuário.
