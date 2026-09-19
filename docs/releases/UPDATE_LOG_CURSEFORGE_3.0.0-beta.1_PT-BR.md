# UFO Future 3.0.0-beta.1

Primeiro beta público da linha 3.0, focado em funcionamento básico, segurança de
save e limpeza do artefato. Esta versão ainda não deve ser tratada como estável:
a migração de um mundo 2.x real e o teste no modpack de produção continuam sendo
os gates humanos finais.

## Correções principais

- Corrigido o travamento permanente em `Saving World` causado por callbacks AE2 e
  atualizações visuais que promoviam chunks durante o desligamento.
- Cabos AE2 conectam em todas as faces do Quantum Pattern Buffer e do ME Massive
  Fluid Hatch, inclusive na frente.
- Pacotes de recuperação exibem o recurso e a quantidade, sem o nome cru
  `Wrapped Generic Stack`.
- Nomes dos baldes e textos em português brasileiro foram corrigidos.
- Outputs, inputs e energia pendentes permanecem transacionais durante rede cheia,
  reload e quebra de máquinas.

## Assets e pacote

- Novos overlays de máquinas e multiblocos integrados.
- Auditoria de procedência zerada: nenhuma correspondência byte a byte antiga e
  nenhum asset all-rights-reserved no pacote.
- Modelos `star`/`space` substituídos por geometria procedural original.
- Backups locais de textura não entram mais nos JARs publicados.

## Dependências

- Minecraft 1.21.1 e NeoForge 21.1.x.
- Applied Energistics 2 19.2.17 ou compatível na linha 19.x.
- RaishxCore 0.1.0-beta.1 ou mais recente na linha 0.1.x.
- Mekanism continua opcional.

Faça backup antes de migrar um mundo antigo e reporte bugs com `latest.log`,
versões exatas e passos de reprodução.
