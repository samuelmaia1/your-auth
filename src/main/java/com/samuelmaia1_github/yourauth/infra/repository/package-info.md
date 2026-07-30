# Package `infra.repository`

## Finalidade

Agrupar repositórios e adaptadores responsáveis por acesso a dados.

## Motivo

Manter consultas, comandos e integrações com mecanismos de armazenamento fora do
domínio e da camada de apresentação.

## Quando colocar arquivos aqui

- O arquivo define interfaces ou implementações de acesso a dados.
- O arquivo encapsula consultas e comandos de persistência.
- O arquivo adapta um contrato de repositorio para uma tecnologia concreta.
