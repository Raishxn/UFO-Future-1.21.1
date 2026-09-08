---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Quantum Interface
  position: 47
---

# Quantum Interface

<BlockImage id="ufo:quantum_interface" scale="3"></BlockImage>

Configura 36 recursos em duas páginas de dezoito slots. Conecte a uma rede ME energizada, configure o coolant ou outro insumo e ajuste a quantidade pela engrenagem acima do slot. Cada slot suporta 1024 bytes de armazenamento, convertidos para as unidades do recurso.

Ative **Auto Export** para enviar o estoque às faces de entrada das máquinas. **Auto Import** devolve outros recursos ao armazenamento ME. Recursos configurados são excluídos da importação para evitar que os insumos retornem imediatamente à rede. Os dois modos podem funcionar juntos.

Shift+clique na engrenagem ativa o fornecimento contínuo pelo estoque ME: não gera recursos gratuitos. O botão de velocidade aumenta a frequência das transferências, não a velocidade das receitas.

## Destinos wireless

Selecione esta interface com a [Quantum Wireless Tool](quantum_wireless_tool.md), depois clique na face de entrada de coolant de cada destino. Ative wireless pelo botão lateral ou por Shift+clique na interface com a ferramenta. O modo local atende vizinhos; o wireless atende os vínculos salvos.

Os destinos precisam estar carregados, na mesma dimensão e dentro do alcance. Cada origem começa em 32 blocos, ajustáveis pela engrenagem de alcance até o máximo do servidor. Dispositivos ligados a outra rede ME não podem ser usados como ponte. Dispositivos isolados são aceitos.

A Quantum Interface fornece apenas logística e não concede buffs wireless. O painel de bônus wireless pertence à Quantum Pattern Hatch.

Créditos dos widgets wireless e de transferência: **AE2 Lightning Tech**. Autoria e licenças estão no aviso de assets incluído no mod. Ainda não há paridade completa com o Overloaded Interface, como o modo EJECT e seus filtros.

<RecipeFor id="ufo:quantum_interface" />

Veja [Rede e Bônus Wireless](quantum_wireless_buffs.md) para montagem da rede, alcance e configuração dos bônus de receitas.
