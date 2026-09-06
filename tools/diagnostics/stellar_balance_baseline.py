#!/usr/bin/env python3
"""Print theoretical Stellar timings from current generated recipes and controller rates."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
controller = (ROOT / 'src/main/java/com/raishxn/ufo/block/entity/StellarNexusControllerBE.java').read_text()
rates_text = re.search(r'ENERGY_RATE_BY_TIER\s*=\s*\{([^}]+)\}', controller).group(1)
rates = [int(value.strip().replace('_', '')) for value in rates_text.split(',')]
capacity = int(re.search(r'GLOBAL_ENERGY_CAPACITY\s*=\s*([\d_]+)L', controller).group(1).replace('_', ''))

print('# F4 — baseline teórica do Stellar Nexus\n')
print('Gerada por `tools/diagnostics/stellar_balance_baseline.py`. Não representa medição no jogo.\n')
print(f'Buffer: {capacity:,} AE. Taxas por tier: ' + ', '.join(f'T{i}={rate:,} AE/t' for i, rate in enumerate(rates) if i) + '.\n')
print('Carga calculada desde buffer vazio, com energia suficiente na rede, no tier mínimo da receita. Segundos assumem 20 TPS. A carga continua durante processamento; não somar os tempos como ciclo sustentado. Pausas, calor, cooldown, portas, estoque e saída cheia podem aumentar os tempos reais.\n')
print('Multiplicadores verificados em `StellarEnergyMath.effectiveCost` e no controller: Safe ×2 energia; OC ×8 energia e avanço ×5; Safe+OC ×16 energia (rebalance 2026-09-06; antes ×2,5/×10/×25). O fuel tem multiplicadores distintos e não está incluído nesta tabela.\n')
print('| Receita | Tier | Modo | Custo AE | Carga desde zero (s) | Processo ideal (s) | Cabe no buffer |')
print('|---|---:|---|---:|---:|---:|---|')
for path in sorted((ROOT / 'src/generated/resources/data/ufo/recipe/stellar_simulation').glob('*.json')):
    recipe = json.loads(path.read_text())
    tier = recipe['field_tier']
    for mode, numerator, denominator, speed in [('Normal', 1, 1, 1), ('Safe', 2, 1, 1), ('OC', 8, 1, 5), ('Safe+OC', 16, 1, 5)]:
        cost = recipe['energy'] * numerator // denominator
        charge_ticks = (cost + rates[tier] - 1) // rates[tier]
        process_ticks = (recipe['time'] + speed - 1) // speed
        print(f'| {path.stem} | {tier} | {mode} | {cost:,} | {charge_ticks / 20:.2f} | {process_ticks / 20:.2f} | {"sim" if cost <= capacity else "não"} |')
print('\nReceitas condicionais de addons só ficam disponíveis quando suas condições são satisfeitas. Os valores são os JSONs gerados, sem overrides de datapacks/KubeJS do mundo.')
