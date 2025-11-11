# temp_test_openpack.ps1
# Login admin and attempt to open pack template 1 for userId 1
try {
    $body = @{ email = 'admin@local.com'; password = 'admin123' } | ConvertTo-Json
    Write-Host "Logging in..."
    $login = Invoke-RestMethod -Uri 'http://localhost:8081/api/v1/auth/login' -Method Post -ContentType 'application/json' -Body $body
    Write-Host "Login response object:"
    $login | ConvertTo-Json -Depth 5
    $token = $login.token
    Write-Host "TOKEN:" $token

    Write-Host "Fetching all cards from API..."
    $cardsAll = Invoke-RestMethod -Uri 'http://localhost:8081/api/cards' -Method Get
    if ($cardsAll -eq $null) { Write-Host "cardsAll: null" } else { Write-Host "cardsAll count:" ($cardsAll | Measure-Object).Count }

    Write-Host "Calling pack open API..."
    $headers = @{ Authorization = "Bearer $token" }
    $cards = Invoke-RestMethod -Uri 'http://localhost:8081/api/pack-openings/1/open?userId=1' -Method Post -Headers $headers
    if ($cards -eq $null) {
        Write-Host "OPEN_OK but returned null"
    } else {
        if ($cards -is [System.Array]) {
            Write-Host "OPEN_OK returned array length:" $cards.Length
        } else {
            Write-Host "OPEN_OK returned object:"
            $cards | ConvertTo-Json -Depth 5
        }
    }
} catch {
    Write-Host "ERROR during test"
    Write-Host $_.Exception.Message
    if ($_.Exception.Response -ne $null) {
        try {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $body = $reader.ReadToEnd()
            Write-Host "Response body:" $body
        } catch {
            Write-Host "No response body available"
        }
    }
}
