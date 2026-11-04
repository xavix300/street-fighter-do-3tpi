$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$helper = Get-Content (Join-Path $PSScriptRoot '../animation-frames/prepare-frames.ps1') -Raw
$code = ($helper -split "@'",2)[1]
$code = ($code -split "'@",2)[0]
Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition $code
Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition @'
using System.Drawing;
using System.Drawing.Imaging;
public static class JumpFrameAnchor {
    public static void AlignHead(string path) {
        using(var canvas=new Bitmap(128,112,PixelFormat.Format32bppArgb)) {
            using(var source=new Bitmap(path)) {
                int top=112;
                for(int y=0;y<112;y++) for(int x=0;x<128;x++)
                    if(source.GetPixel(x,y).A>16 && y<top) top=y;
                using(var graphics=Graphics.FromImage(canvas)) {
                    graphics.Clear(Color.Transparent);
                    graphics.DrawImageUnscaled(source,0,8-top);
                }
            }
            canvas.Save(path,ImageFormat.Png);
        }
    }
}
'@
$project = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$sprites = Join-Path $project 'src/main/resources/assets/textures/fighter'
$cleanJump = Join-Path $sprites 'sequences/jump_clean'
New-Item -ItemType Directory -Force $cleanJump | Out-Null
foreach ($photo in 5..12) {
    $name = 'jump_{0:000}.png' -f $photo
    $height = @{5=100;6=90;7=100;8=74;9=80;10=94;11=94;12=100}[$photo]
    $target = Join-Path $cleanJump $name
    [AnimationFrameCanvas]::Normalize((Join-Path $PSScriptRoot "jump-clean/$name"),$target,$height)
    [JumpFrameAnchor]::AlignHead($target)
}
$preview = [System.Drawing.Bitmap]::new(960,864)
$graphics = [System.Drawing.Graphics]::FromImage($preview)
$graphics.Clear([System.Drawing.Color]::FromArgb(65,65,65))
$font = [System.Drawing.Font]::new('Arial',9)
$row = 0
try {
    foreach ($base in @('lp','mp','hp','lk','mk','hk')) {
        $action = "air_$base"
        $folder = Join-Path $sprites "sequences/$action"
        New-Item -ItemType Directory -Force $folder | Out-Null
        $impact = Join-Path $PSScriptRoot "$action-impact.png"
        foreach ($phase in 1..6) {
            $name = '{0}_{1:00}.png' -f $action,$phase
            $target = Join-Path $folder $name
            if ($phase -in @(3,4)) {
                [AnimationFrameCanvas]::Normalize($impact,$target,90)
                [JumpFrameAnchor]::AlignHead($target)
            } else {
                $photo = switch ($phase) { 1 {8} 2 {9} 5 {9} 6 {9} }
                $source = Join-Path $cleanJump ('jump_{0:000}.png' -f $photo)
                Copy-Item -LiteralPath $source -Destination $target
            }
            $image = [System.Drawing.Bitmap]::new($target)
            try {
                if ($image.GetPixel(0,0).A -ne 0) { throw "Missing alpha: $name" }
                $graphics.DrawImage($image,(($phase-1)*160),($row*144),128,112)
                $graphics.DrawString($name,$font,[System.Drawing.Brushes]::White,(($phase-1)*160),($row*144+115))
            } finally { $image.Dispose() }
        }
        $row++
    }
    $preview.Save((Join-Path $PSScriptRoot 'air-sequences-review.png'),[System.Drawing.Imaging.ImageFormat]::Png)
} finally { $graphics.Dispose(); $font.Dispose(); $preview.Dispose() }
