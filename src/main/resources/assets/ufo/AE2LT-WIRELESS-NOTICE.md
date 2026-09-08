# Quantum Wireless third-party attribution

Project: AE2 Lightning Tech — https://github.com/ae2lt/AE2-Lightning-Tech
Copyright AE2 Lightning Tech contributors.
Source revision: 7e2e2726401e4ff4720e5f7efa9f4c0031b57c22.

QuantumWirelessToggleButton.java adapts client/TextureToggleButton.java under LGPL-3.0.
Changes: two-state controls, UFO namespace and translated Minecraft tooltips.
Source license text is included at META-INF/licenses/AE2LT-LGPL-3.0.txt.

The following icons are redistributed unchanged from assets/ae2lt/textures/gui/buttons/
under CC BY-NC-SA 3.0, in assets/ufo/textures/gui/buttons/:

- wired_mode.png
- wireless_mode.png
- auto_export_off.png
- auto_export_on.png
- auto_input_off.png
- auto_input_on.png
- speed_normal.png
- speed_fast.png

Asset license: https://creativecommons.org/licenses/by-nc-sa/3.0/
Upstream notice: META-INF/licenses/AE2LT-ASSETS.md.

Quantum Interface I/O controls follow the Overloaded ME Interface design. The first
UFO transport implementation is independent and does not yet implement full parity.
The block's quantum_interface.png was supplied by the UFO user; it was not copied by
this work from AE2LT. The Quantum Wireless Tool concept was generated independently.

The quantum_interface screen layout follows AE2 Lightning's overloaded_interface.json.
assets/ae2/textures/guis/quantum_interface.png redistributes its ex_interface.png
unchanged under the same CC BY-NC-SA 3.0 asset license stated above.
QuantumWirelessRenderer follows the source/face/line interaction in AE2 Lightning's
WirelessConnectorRenderer; it uses AE2's OverlayRenderType and a held-tool snapshot.
Rendering source is distributed under LGPL-3.0 with the attribution above.
QuantumWirelessRenderTypes adapts Ae2ltRenderTypes' GREATER-depth source highlight.
The configuration button follows OverloadedInterfaceScreen.SetAmountButton:
background-free COG_DISABLED / COG on hover, with UFO stock tooltips.
Underlying GUI framework attribution: Applied Energistics 2 contributors,
https://github.com/AppliedEnergistics/Applied-Energistics-2.
