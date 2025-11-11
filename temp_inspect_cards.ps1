# temp_inspect_cards.ps1
try {
    $cards = Invoke-RestMethod -Uri 'http://localhost:8081/api/cards' -Method Get
    Write-Host "cards count:" ($cards | Measure-Object).Count
    foreach ($c in $cards) {
        $expId = $null
        if ($c.expansion -ne $null) { $expId = $c.expansion.id }
        Write-Host "card id=$($c.id) name=$($c.name) expansionId=$expId"
    }

    $pt = Invoke-RestMethod -Uri 'http://localhost:8081/api/pack-templates/1' -Method Get
    Write-Host "pack-template id=$($pt.id) name=$($pt.name) expansionId=$($pt.expansion?.id) packSlotsCount=$($pt.packSlots?.Count)"
    if ($pt.packSlots -ne $null) {
        foreach ($s in $pt.packSlots) { Write-Host " slot id=$($s.id) rarity=$($s.rarity) expansionId=$($s.expansion?.id)" }
    }
} catch {
    Write-Host "ERROR:" $_.Exception.Message
}

