# Package `presentation.mapper`

## Finalidade

Agrupar mappers que adaptam dados entre DTOs da API e modelos de domínio.

## Motivo

Manter transformações de entrada e saída HTTP fora dos controllers e impedir que
DTOs contaminem o domínio.

## Quando colocar arquivos aqui

- O arquivo converte requests em comandos ou objetos de domínio.
- O arquivo converte modelos de domínio em responses.
- A transformação está ligada ao contrato HTTP da API.
