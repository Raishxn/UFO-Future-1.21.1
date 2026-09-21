# Quantum Wireless third-party attribution

Project: AE2 Lightning Tech — https://github.com/ae2lt/AE2-Lightning-Tech
Copyright AE2 Lightning Tech contributors.
Source revision: 7e2e2726401e4ff4720e5f7efa9f4c0031b57c22.

QuantumWirelessToggleButton.java adapts client/TextureToggleButton.java under LGPL-3.0.
Changes: two-state controls, UFO namespace and translated Minecraft tooltips.
Source license text is included at META-INF/licenses/AE2LT-LGPL-3.0.txt.

All widget button textures (wired_mode.png, wireless_mode.png, auto_export_off.png,
auto_export_on.png, auto_input_off.png, auto_input_on.png, speed_normal.png,
speed_fast.png, and quick_build.png) as well as the screen background
(assets/ae2/textures/guis/quantum_interface.png) are original UFO Future artworks
created for this mod. No texture or image assets from AE2 Lightning Tech are
redistributed.

Quantum Interface I/O controls follow the Overloaded ME Interface design. The first
UFO transport implementation is independent and does not yet implement full parity.
The block's quantum_interface.png was supplied by the UFO user; it was not copied by
this work from AE2LT. The Quantum Wireless Tool concept was generated independently.

The quantum_interface screen layout follows AE2 Lightning's overloaded_interface.json.
QuantumWirelessRenderer follows the source/face/line interaction in AE2 Lightning's
WirelessConnectorRenderer; it uses AE2's OverlayRenderType and a held-tool snapshot.
Rendering source is distributed under LGPL-3.0 with the attribution above.
QuantumWirelessRenderTypes adapts Ae2ltRenderTypes' GREATER-depth source highlight.
The configuration button follows OverloadedInterfaceScreen.SetAmountButton:
background-free COG_DISABLED / COG on hover, with UFO stock tooltips.
Underlying GUI framework attribution: Applied Energistics 2 contributors,
https://github.com/AppliedEnergistics/Applied-Energistics-2.
