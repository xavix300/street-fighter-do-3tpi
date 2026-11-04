param([switch]$AvailableOnly)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$actions = @('lp','mp','lk','mk','crouch_lp','crouch_mp','crouch_hp','crouch_lk','crouch_mk','crouch_hk')
if ($AvailableOnly) { $actions = @($actions | Where-Object { Test-Path -LiteralPath (Join-Path $PSScriptRoot "$_/${_}_06.png") }) }
$canvas = [System.Drawing.Bitmap]::new(960,($actions.Count*144))
$graphics = [System.Drawing.Graphics]::FromImage($canvas)
$graphics.Clear([System.Drawing.Color]::FromArgb(70,70,70))
$font = [System.Drawing.Font]::new('Arial',9)
$records = @()
try {
    for ($row=0;$row -lt $actions.Count;$row++) {
        $action = $actions[$row]
        for ($phase=1;$phase -le 6;$phase++) {
            $name = '{0}_{1:00}.png' -f $action,$phase
            $file = Join-Path $PSScriptRoot "$action/$name"
            if (-not (Test-Path -LiteralPath $file)) { throw "Missing frame: $file" }
            $image = [System.Drawing.Bitmap]::new($file)
            try {
                if ($image.Width -ne 128 -or $image.Height -ne 112) { throw "Invalid canvas: $file" }
                if ($image.GetPixel(0,0).A -ne 0) { throw "Opaque background: $file" }
                $left=128; $top=112; $right=-1; $bottom=-1; $transparent=0; $visible=0
                for ($y=0;$y -lt 112;$y++) { for ($x=0;$x -lt 128;$x++) {
                    $alpha=$image.GetPixel($x,$y).A
                    if ($alpha -eq 0) { $transparent++ }
                    if ($alpha -gt 16) {
                        $visible++; $left=[Math]::Min($left,$x); $right=[Math]::Max($right,$x)
                        $top=[Math]::Min($top,$y); $bottom=[Math]::Max($bottom,$y)
                    }
                } }
                if ($visible -eq 0 -or $transparent -eq 0) { throw "Invalid alpha: $file" }
                if ($left -le 0 -or $right -ge 127 -or $top -le 0 -or $bottom -ge 111) { throw "Clipped silhouette: $file" }
                $graphics.DrawImage($image,(($phase-1)*160),($row*144),128,112)
                $graphics.DrawString($name,$font,[System.Drawing.Brushes]::White,(($phase-1)*160),($row*144+116))
                $records += [PSCustomObject]@{action=$action;phase=$phase;file="$action/$name";width=128;height=112;bounds=@($left,$top,$right,$bottom);transparentPixels=$transparent;visiblePixels=$visible;sha256=(Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash}
            } finally { $image.Dispose() }
        }
    }
    $canvas.Save((Join-Path $PSScriptRoot 'sequences-review.png'),[System.Drawing.Imaging.ImageFormat]::Png)
    $records | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'manifest.json') -Encoding UTF8
    if (-not $AvailableOnly) {
        $deliverables = @($records | ForEach-Object { Join-Path $PSScriptRoot $_.file })
        $deliverables += @('README.md','manifest.json','prompts.json','crouch-prompts.json','sequences-review.png','existing-review.png') | ForEach-Object { Join-Path $PSScriptRoot $_ }
        Compress-Archive -LiteralPath $deliverables -DestinationPath (Join-Path $PSScriptRoot 'animation-frames.zip') -Force
    }
    Write-Output "Validated $($records.Count) PNG frames: 128x112, transparent alpha, silhouette inside canvas, SHA-256 manifest."
} finally { $graphics.Dispose(); $canvas.Dispose(); $font.Dispose() }
