# Package `infra.exceptions`

## Finalidade

Concentrar exceptions relacionadas a falhas técnicas de infraestrutura.

## Motivo

Diferenciar erros de integração, persistência, configuração ou segurança das
falhas de regra de negócio do domínio.

## Quando colocar arquivos aqui

- O arquivo representa uma falha técnica causada por recurso externo.
- O arquivo encapsula exceptions de banco, rede, segurança ou configuração.
- A exception não pertence a uma regra de negócio específica.
