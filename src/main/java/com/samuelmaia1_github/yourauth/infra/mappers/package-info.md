# Package `infra.mappers`

## Finalidade

Agrupar mappers que adaptam dados entre modelos de infraestrutura e modelos de
domínio.

## Motivo

Isolar transformações técnicas, como entity para domain e domain para entity,
evitando que o domínio conheça formatos de persistência ou integração.

## Quando colocar arquivos aqui

- O arquivo converte entidades de persistência para objetos de domínio.
- O arquivo converte objetos de domínio para modelos usados pela infraestrutura.
- A transformação está ligada a detalhes técnicos, não ao contrato HTTP.
