# Package `infra`

## Finalidade

Concentrar detalhes técnicos necessários para executar a aplicação, como
configurações, persistência, segurança, validações técnicas e integrações.

## Motivo

Separar mecanismos de infraestrutura das regras de negócio, mantendo o domínio
menos acoplado a frameworks e recursos externos.

## Quando colocar arquivos aqui

- O arquivo depende diretamente de frameworks ou recursos externos.
- O arquivo implementa detalhes de banco de dados, segurança ou configuração.
- O arquivo adapta dados entre o domínio e tecnologias externas.
