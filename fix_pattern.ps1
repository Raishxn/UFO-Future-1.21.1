$file = 'c:\Users\erick\OneDrive\Documents\UFO-Future-1.21.1-main\src\main\java\com\raishxn\ufo\block\entity\pattern\StellarNexusPatternFactory.java'
$lines = [System.IO.File]::ReadAllLines($file)
$result = New-Object System.Collections.Generic.List[string]
$skip = $false
foreach ($line in $lines) {
    if ($line.Trim() -match 'state\.is\(MultiblockBlocks\.STELLAR_FUEL_HATCH\.get\(\)\)') {
        # Skip this line entirely
        # Also fix the previous line to remove the trailing ||
        $lastIdx = $result.Count - 1
        $prevLine = $result[$lastIdx]
        $result[$lastIdx] = $prevLine -replace '\|\|\s*$', ','
        continue
    }
    $result.Add($line)
}
[System.IO.File]::WriteAllLines($file, $result.ToArray())
Write-Host "Removed STELLAR_FUEL_HATCH from pattern factory"
