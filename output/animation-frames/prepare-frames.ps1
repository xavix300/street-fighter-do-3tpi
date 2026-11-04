param([string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Drawing2D;
using System.Runtime.InteropServices;
public static class AnimationFrameCanvas {
    public static Rectangle Bounds(Bitmap image) {
        var rect = new Rectangle(0, 0, image.Width, image.Height);
        var data = image.LockBits(rect, ImageLockMode.ReadOnly, PixelFormat.Format32bppArgb);
        int left=image.Width, top=image.Height, right=-1, bottom=-1;
        try {
            var row = new byte[image.Width*4];
            for(int y=0;y<image.Height;y++) {
                Marshal.Copy(IntPtr.Add(data.Scan0,y*data.Stride),row,0,row.Length);
                for(int x=0;x<image.Width;x++) if(row[x*4+3]>16) {
                    left=Math.Min(left,x); top=Math.Min(top,y);
                    right=Math.Max(right,x); bottom=Math.Max(bottom,y);
                }
            }
        } finally { image.UnlockBits(data); }
        if(right<left) throw new Exception("Empty image");
        return Rectangle.FromLTRB(left,top,right+1,bottom+1);
    }
    public static void Normalize(string input, string output, int height) {
        using(var source=new Bitmap(input)) using(var canvas=new Bitmap(128,112,PixelFormat.Format32bppArgb)) {
            Rectangle bounds=Bounds(source);
            float scale=Math.Min((float)height/bounds.Height,120f/bounds.Width);
            float width=bounds.Width*scale, h=bounds.Height*scale;
            using(var graphics=Graphics.FromImage(canvas)) {
                graphics.Clear(Color.Transparent);
                graphics.CompositingMode=CompositingMode.SourceCopy;
                graphics.InterpolationMode=InterpolationMode.HighQualityBicubic;
                graphics.PixelOffsetMode=PixelOffsetMode.HighQuality;
                graphics.DrawImage(source,new RectangleF(64-width/2,108-h,width,h),bounds,GraphicsUnit.Pixel);
            }
            canvas.Save(output,ImageFormat.Png);
        }
    }
}
'@
$sprites = Join-Path $ProjectRoot 'src/main/resources/assets/textures/fighter'
foreach ($action in @('lp','mp','lk','mk')) {
    $folder = Join-Path $PSScriptRoot $action
    $raw = Join-Path $folder 'generated-originals'
    New-Item -ItemType Directory -Force $raw | Out-Null
    foreach ($phase in @(2,3,5)) {
        $name = '{0}_{1:00}.png' -f $action,$phase
        $target = Join-Path $folder $name
        $original = Join-Path $raw $name
        if (-not (Test-Path -LiteralPath $original)) { Copy-Item -LiteralPath $target -Destination $original }
        [AnimationFrameCanvas]::Normalize($original,$target,100)
    }
    foreach ($phase in @(1,6)) {
        Copy-Item -LiteralPath (Join-Path $sprites 'idle/idle_001.png') -Destination (Join-Path $folder ('{0}_{1:00}.png' -f $action,$phase))
    }
    $source = if ($action -eq 'mk') {'hk'} else {$action}
    Copy-Item -LiteralPath (Join-Path $sprites "$source/${source}_001.png") -Destination (Join-Path $folder "${action}_04.png")
}
foreach ($action in @('crouch_lp','crouch_mp','crouch_hp','crouch_lk','crouch_mk','crouch_hk')) {
    $folder = Join-Path $PSScriptRoot $action
    $missing = @(2,3,4,5) | Where-Object { -not (Test-Path -LiteralPath (Join-Path $folder ('{0}_{1:00}.png' -f $action,$_))) }
    if (@($missing).Count -gt 0) { continue }
    $raw = Join-Path $folder 'generated-originals'
    New-Item -ItemType Directory -Force $raw | Out-Null
    foreach ($phase in @(2,3,4,5)) {
        $name = '{0}_{1:00}.png' -f $action,$phase
        $target = Join-Path $folder $name
        $original = Join-Path $raw $name
        if (-not (Test-Path -LiteralPath $original)) { Copy-Item -LiteralPath $target -Destination $original }
        [AnimationFrameCanvas]::Normalize($original,$target,70)
    }
    foreach ($phase in @(1,6)) {
        [AnimationFrameCanvas]::Normalize((Join-Path $sprites 'low_block/low_block_001.png'),(Join-Path $folder ('{0}_{1:00}.png' -f $action,$phase)),70)
    }
}
