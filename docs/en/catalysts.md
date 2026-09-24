# Catalysts

The **DMA, QMF, Quantum Slicer, Quantum Processor Assembler and Quantum Cryoforge** accept catalysts in four upgrade slots. There are four families with three tiers each, plus the Dimensional Catalyst.

| Family | Main effect | T1 / T2 / T3 |
|---|---|---|
| Matterflow | Lower recipe energy cost | −10% / −25% / −50% per card |
| Chrono | Faster processing | +25% / +62.5% / +125% speed per card |
| Quantum | Chance of extra output | +10% / +25% / +50% per card |
| Overflux | Thermal modifier | Negative static heat; behavior depends on the controller |

Matterflow and Chrono also multiply the machine's energy buffer by **10 / 100 / 1,000** for tiers T1 / T2 / T3. Combining powerful catalysts can raise heat sharply. Four identical cards add a family-specific synergy and an extra heat penalty.

## Overflux and heat

The old wiki said Overflux gives a failure-chance reduction and active cooling in the DMA. The current processing code has no corresponding failure-chance mechanic. DMA clamps each negative heat contribution to zero, so Overflux does **not** actively cool the DMA. Parallel multiblocks include its negative heat value in their combined thermal profile; a four-card Overflux set reduces that profile further.

## Dimensional Catalyst

This is a **craftable MK3 QMF item**, despite the older “creative catalyst” name in its tooltip. It dramatically accelerates processing, removes processing energy and heat costs, and grants guaranteed bonus output. **Recipe ingredients are still consumed.** Check JEI/EMI for its current expensive QMF recipe.

Use the controller's live status display to assess the combined speed, energy and thermal effects before sustained automation.

*See also: [DMA](dma.md) · [Multiblock line](multiblock-line.md)*
