---
navigation:
  parent: ufo_intro/tools.md
  title: Quantum Wireless Tool
  position: 10
---

# Quantum Wireless Tool

<ItemImage id="ufo:quantum_wireless_tool" scale="4" />

Ative primeiro o modo wireless na origem pelo botão da UI ou por Shift+clique com a ferramenta. Selecionar ou editar vínculos com o modo desligado é recusado com um aviso.

1. Clique com o botão direito no Quantum Pattern Buffer, no Quantum Pattern Hatch legado ou na [Quantum Interface](quantum_interface.md) para selecionar a origem.
2. O Buffer vincula apenas Pattern Proxies; o Hatch legado vincula apenas DMAs.
3. Clique na face de entrada de uma máquina para criar o vínculo. Clique novamente na mesma face para removê-lo.
4. Shift+botão direito na origem alterna entre local e wireless. Também é possível usar o botão na tela.

Segurando a ferramenta, linhas azuis conectam a origem às faces vinculadas em azul. Um cubo amarelo identifica a origem selecionada, e amarelo indica a prévia de uma nova face sob a mira. Os vínculos são atualizados automaticamente enquanto a origem está carregada. O HUD também identifica a origem, coordenadas e dimensão. A versão de cabo Quantum Pattern Provider ainda não funciona como origem wireless.

Para patterns de processamento, vincule a face de entrada de itens/fluidos. Os resultados precisam de um caminho de retorno à rede ME, como Auto Import de uma Quantum Interface vinculada à face de saída. O wireless não carrega chunks nem conecta dimensões ou redes diferentes. Substituir o destino invalida o vínculo antigo.

A textura original 16×16 da ferramenta tem identidade visual do UFO e não reutiliza a ferramenta do AE2 Lightning. Os créditos dos widgets laterais continuam com AE2 Lightning Tech.

<RecipeFor id="ufo:quantum_wireless_tool" />

O alcance padrão é 32 blocos por origem. Ajuste na UI do Buffer/Hatch/Interface. Consulte [Rede e Bônus Wireless](quantum_wireless_buffs.md).
