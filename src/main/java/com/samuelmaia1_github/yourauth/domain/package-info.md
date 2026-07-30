# Package `domain`

## Finalidade

Concentrar o modelo de domínio da aplicação e as regras de negócio que
independem de frameworks, banco de dados ou transporte HTTP.

## Motivo

Manter o núcleo do sistema protegido de detalhes técnicos, facilitando testes,
evolução das regras e troca de implementações externas.

## Quando colocar arquivos aqui

- O arquivo representa um conceito central do negócio.
- O arquivo implementa uma regra de negócio pura.
- O arquivo não depende diretamente de Spring, JPA, controllers ou DTOs.
