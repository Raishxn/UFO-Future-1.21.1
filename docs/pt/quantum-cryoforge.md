# Quantum Cryoforge

O **Quantum Cryoforge** e a etapa criogenica da linha de receitas universais. Ele produz materiais como Stable Coolant para as maquinas seguintes.

## Operacao

- Executa ate **27 jobs paralelos**, ou **9** no Safe Mode.
- Usa os requisitos MK1–MK3 e o [bonus de tier](multiblock-tiers.md).
- Aceita receitas `ufo:universal_multiblock` com `machine: 'quantum_cryoforge'`; veja [receitas KubeJS](kubejs-recipes.md).
- Exige um Quantum Pattern Buffer ou Proxy vinculado para automacao de patterns AE2.
- Puxa ingredientes da ME e devolve os resultados. Forneca coolant externo ao **ME Massive Fluid Hatch** e FE ao **FE Energy Input Hatch**.

Use o preview da estrutura ou o Structure Scanner para encontrar as posicoes dos hatches.
