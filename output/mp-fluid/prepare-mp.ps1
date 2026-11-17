param(
    [Parameter(Mandatory=$true)][string]$Source
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;

public static class PrepareMp {
    static Rectangle Bounds(Bitmap image) {
        int left=image.Width, top=image.Height, right=-1, bottom=-1;
        for(int y=0;y<image.Height;y++) for(int x=0;x<image.Width;x++) {
            if(image.GetPixel(x,y).A<64) continue;
            left=Math.Min(left,x); top=Math.Min(top,y);
            right=Math.Max(right,x); bottom=Math.Max(bottom,y);
        }
        if(right<left) throw new Exception("Empty sprite");
        return Rectangle.FromLTRB(left,top,right+1,bottom+1);
    }
    // Centre of the planted feet, not the moving arms or the bounding box.
    static double FootAnchor(Bitmap image, Rectangle bounds) {
        double total=0, count=0;
        int start=bounds.Bottom-Math.Max(3,bounds.Height/14);
        for(int y=start;y<bounds.Bottom;y++) for(int x=bounds.Left;x<bounds.Right;x++) {
            if(image.GetPixel(x,y).A<128) continue;
            total+=x; count++;
        }
        return count==0 ? bounds.Left+bounds.Width/2.0 : total/count;
    }
    public static void Run(string source, string idlePath, string output, string reviewPath) {
        Directory.CreateDirectory(output);
        using(var atlas=new Bitmap(source))
        using(var idle=new Bitmap(idlePath))
        using(var review=new Bitmap(128*5,112*4)) {
            var idleBounds=Bounds(idle);
            double targetAnchor=FootAnchor(idle,idleBounds);
            var cells=new Bitmap[20];
            try {
                double[] heights=new double[20];
                for(int i=0;i<20;i++) {
                    int col=i%5, row=i/5;
                    int x0=col*atlas.Width/5, x1=(col+1)*atlas.Width/5;
                    int y0=row*atlas.Height/4, y1=(row+1)*atlas.Height/4;
                    cells[i]=atlas.Clone(new Rectangle(x0,y0,x1-x0,y1-y0),PixelFormat.Format32bppArgb);
                    heights[i]=Bounds(cells[i]).Height;
                }
                Array.Sort(heights);
                double scale=idleBounds.Height/((heights[9]+heights[10])/2.0);
                using(var rg=Graphics.FromImage(review)) {
                    rg.Clear(Color.FromArgb(255,70,75,82));
                    for(int i=0;i<20;i++) {
                        using(var frame=new Bitmap(128,112)) {
                            var b=Bounds(cells[i]);
                            double anchor=FootAnchor(cells[i],b);
                            float width=(float)(b.Width*scale), height=(float)(b.Height*scale);
                            float x=(float)(targetAnchor-(anchor-b.Left)*scale);
                            float y=(float)(idleBounds.Bottom-height);
                            if(x<1 || x+width>127 || y<1) throw new Exception("Sprite would be clipped: "+(i+1));
                            using(var g=Graphics.FromImage(frame)) {
                                g.Clear(Color.Transparent);
                                g.CompositingMode=CompositingMode.SourceCopy;
                                g.InterpolationMode=InterpolationMode.HighQualityBicubic;
                                g.PixelOffsetMode=PixelOffsetMode.HighQuality;
                                g.DrawImage(cells[i],new RectangleF(x,y,width,height),b,GraphicsUnit.Pixel);
                            }
                            frame.Save(Path.Combine(output,"mp_"+(i+1).ToString("00")+".png"),ImageFormat.Png);
                            rg.DrawImageUnscaled(frame,(i%5)*128,(i/5)*112);
                        }
                    }
                }
                review.Save(reviewPath,ImageFormat.Png);
            } finally {
                foreach(var cell in cells) if(cell!=null) cell.Dispose();
            }
        }
    }
}
'@
$project = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$original = Join-Path $PSScriptRoot 'mp-atlas-original.png'
if ((Resolve-Path $Source).Path -ne $original) {
    Copy-Item -LiteralPath $Source -Destination $original
}
$idle = Join-Path $project 'src/main/resources/assets/textures/fighter/sequences/mp/mp_01.png'
$output = Join-Path $project 'src/main/resources/assets/textures/fighter/attacks/mp'
[PrepareMp]::Run($original, $idle, $output, (Join-Path $PSScriptRoot 'mp-review.png'))
Write-Output 'MP: 20 transparent frames prepared.'
