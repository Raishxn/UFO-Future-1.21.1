# AE2 Lightning Tech — referência e créditos do Quantum Wireless

O desenho do Quantum Wireless e a paridade pretendida da Quantum Interface usam como referência a Overloaded ME Interface e os widgets de AE2 Lightning Tech.

- Projeto: [AE2 Lightning Tech](https://github.com/ae2lt/AE2-Lightning-Tech).
- Autoria: AE2 Lightning Tech contributors. O README público consultado lista MOAKIEE, CystrySU, gjmhmm8, _leng, TedXenon e MHanHanBing.
- Revisão local examinada: `7e2e2726401e4ff4720e5f7efa9f4c0031b57c22`.
- Código: [LGPL-3.0](https://github.com/ae2lt/AE2-Lightning-Tech/blob/7e2e2726401e4ff4720e5f7efa9f4c0031b57c22/LICENSE).
- Texturas e assets: [CC BY-NC-SA 3.0](https://github.com/ae2lt/AE2-Lightning-Tech/blob/7e2e2726401e4ff4720e5f7efa9f4c0031b57c22/LICENSE_ASSETS.md).

## Inventário de referências planejadas

Atualização da primeira implementação: `QuantumWirelessToggleButton.java` já adapta a renderização/estados do widget da referência. Oito ícones (wired_mode, wireless_mode, auto_export_off/on, auto_input_off/on, speed_normal/fast) foram copiados sem alteração. O inventário efetivo e as licenças são distribuídos em `src/main/resources/assets/ufo/AE2LT-WIRELESS-NOTICE.md` e `META-INF/licenses/`. As demais classes listadas abaixo continuam sendo referências; não houve cópia integral da lógica de interface AE2LT.

| Origem no AE2LT | Uso pretendido | Estado |
| --- | --- | --- |
| `client/TextureToggleButton.java` | Widgets de estados | Adaptado em QuantumWirelessToggleButton.java; dois estados, namespace UFO e tooltips traduzidos |
| `client/OverloadedInterfaceScreen.java` | Controles de modo, importação, exportação e I/O | Examinado; ainda não adaptado |
| `menu/OverloadedInterfaceMenu.java` | Contrato da tela e ações | Identificado; paridade ainda não implementada |
| `blockentity/OverloadedInterfaceBlockEntity.java` | Vínculos por face e transferência | Trechos examinados; ainda não adaptado |
| `logic/OverloadedInterfaceLogic.java` | Configuração e armazenamento | Trechos examinados; ainda não adaptado |
| `textures/gui/buttons/` | Ícones correspondentes aos widgets | Oito ícones copiados; lista exata no aviso distribuído |

Na implementação, completar a lista com os caminhos individuais efetivamente copiados/adaptados, destino no UFO, licença, autoria indicada na origem e alterações feitas. Preservar avisos nos arquivos derivados e disponibilizar os textos de licença e avisos com a distribuição correspondente.

A textura `assets/ufo/textures/block/quantum_interface.png` foi fornecida pelo usuário para o UFO. Ela não faz parte dos assets AE2LT copiados por esta tarefa.

Crédito público proposto após a adaptação: “Os widgets wireless e as funcionalidades de importação/exportação da Quantum Interface foram adaptados de AE2 Lightning Tech. Agradecimentos aos autores e contribuidores do projeto.” Até a implementação, descrever o trabalho como referência planejada, não como port concluído.
