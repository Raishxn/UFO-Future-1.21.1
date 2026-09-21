# AE2 Lightning Tech — referência e créditos do Quantum Wireless

O desenho do Quantum Wireless e a paridade pretendida da Quantum Interface usam como referência a Overloaded ME Interface e os widgets de AE2 Lightning Tech.

- Projeto: [AE2 Lightning Tech](https://github.com/ae2lt/AE2-Lightning-Tech).
- Autoria: AE2 Lightning Tech contributors. O README público consultado lista MOAKIEE, CystrySU, gjmhmm8, _leng, TedXenon e MHanHanBing.
- Revisão local examinada: `7e2e2726401e4ff4720e5f7efa9f4c0031b57c22`.
- Código: [LGPL-3.0](https://github.com/ae2lt/AE2-Lightning-Tech/blob/7e2e2726401e4ff4720e5f7efa9f4c0031b57c22/LICENSE).
- Texturas e assets: [CC BY-NC-SA 3.0](https://github.com/ae2lt/AE2-Lightning-Tech/blob/7e2e2726401e4ff4720e5f7efa9f4c0031b57c22/LICENSE_ASSETS.md).

## Inventário de referências planejadas

Atualização da implementação: `QuantumWirelessToggleButton.java` adapta a lógica de estados do widget da referência sob LGPL-3.0. Todos os ícones (wired_mode, wireless_mode, auto_export_off/on, auto_input_off/on, speed_normal/fast) e a textura de fundo da GUI (`quantum_interface.png`) foram recriados como artes 100% autorais do UFO Future, eliminando qualquer redistribuição de imagens do AE2LT. O inventário efetivo e as licenças são distribuídos em `src/main/resources/assets/ufo/AE2LT-WIRELESS-NOTICE.md`.

| Origem no AE2LT | Uso pretendido | Estado |
| --- | --- | --- |
| `client/TextureToggleButton.java` | Widgets de estados | Adaptado em QuantumWirelessToggleButton.java sob LGPL-3.0; dois estados, namespace UFO e tooltips traduzidos |
| `client/OverloadedInterfaceScreen.java` | Controles de modo, importação, exportação e I/O | Examinado; ainda não adaptado |
| `menu/OverloadedInterfaceMenu.java` | Contrato da tela e ações | Identificado; paridade ainda não implementada |
| `blockentity/OverloadedInterfaceBlockEntity.java` | Vínculos por face e transferência | Trechos examinados; ainda não adaptado |
| `logic/OverloadedInterfaceLogic.java` | Configuração e armazenamento | Trechos examinados; ainda não adaptado |
| `textures/gui/buttons/` | Ícones correspondentes aos widgets | Substituídos por pixel art original do UFO Future (nenhum asset redistribuído) |

Na implementação, completar a lista com os caminhos individuais efetivamente copiados/adaptados, destino no UFO, licença, autoria indicada na origem e alterações feitas. Preservar avisos nos arquivos derivados e disponibilizar os textos de licença e avisos com a distribuição correspondente.

A textura `assets/ufo/textures/block/machines/quantum_interface.png` foi fornecida pelo usuário para o UFO. Ela não faz parte dos assets AE2LT copiados por esta tarefa.

Crédito público proposto após a adaptação: “Os widgets wireless e as funcionalidades de importação/exportação da Quantum Interface foram adaptados de AE2 Lightning Tech. Agradecimentos aos autores e contribuidores do projeto.” Até a implementação, descrever o trabalho como referência planejada, não como port concluído.
