param(
    [Parameter(Mandatory=$true)][ValidatePattern('^[a-z_]+$')][string]$Action,
    [Parameter(Mandatory=$true)][string]$Sheet
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
if (-not ('AttackAtlas' -as [type])) {
Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
public static class AttackAtlas {
    class Pose {
        public Rectangle Bounds;
        public int Id, Area;
    }
    public static void Export(string input, string folder, string action) {
        using(var sheet=new Bitmap(input)) {
            int w=sheet.Width, h=sheet.Height;
            int[] pixels=new int[w*h], labels=new int[w*h], queue=new int[w*h];
            var data=sheet.LockBits(new Rectangle(0,0,w,h),ImageLockMode.ReadOnly,PixelFormat.Format32bppArgb);
            try { for(int y=0;y<h;y++) Marshal.Copy(IntPtr.Add(data.Scan0,y*data.Stride),pixels,y*w,w); }
            finally { sheet.UnlockBits(data); }
            var found=new List<Pose>(); int id=0;
            for(int start=0;start<pixels.Length;start++) {
                if(labels[start]!=0 || (uint)pixels[start]>>24<128) continue;
                id++; int head=0, tail=1; queue[0]=start; labels[start]=id;
                int l=w, t=h, r=0, b=0;
                while(head<tail) {
                    int p=queue[head++], x=p%w, y=p/w;
                    l=Math.Min(l,x);t=Math.Min(t,y);r=Math.Max(r,x);b=Math.Max(b,y);
                    for(int yy=Math.Max(0,y-1);yy<=Math.Min(h-1,y+1);yy++)
                        for(int xx=Math.Max(0,x-1);xx<=Math.Min(w-1,x+1);xx++) {
                            int next=yy*w+xx;
                            if(labels[next]==0 && (uint)pixels[next]>>24>=128) {
                                labels[next]=id; queue[tail++]=next;
                            }
                        }
                }
                if(tail>=400) found.Add(new Pose { Id=id, Area=tail, Bounds=Rectangle.FromLTRB(l,t,r+1,b+1) });
            }
            if(found.Count!=40) throw new Exception("Expected forty separate people, detected "+found.Count+" in "+action);
            var sorted=found.OrderBy(p=>action.StartsWith("air_") ? p.Bounds.Top : p.Bounds.Bottom).ToArray();
            var poses=new List<Pose>();
            for(int row=0;row<5;row++) poses.AddRange(sorted.Skip(row*8).Take(8).OrderBy(p=>p.Bounds.Left));
            double[] anchors=poses.Take(8).Select(p=>p.Bounds.Left+p.Bounds.Width/2.0).ToArray();
            int height=action.StartsWith("crouch_") || action=="anti_air_crouch_mp" ? 72 : action.StartsWith("air_") ? 90 : 100;
            double extent=0;
            for(int i=0;i<40;i++) {
                var b=poses[i].Bounds;
                if(b.Left==0 || b.Top==0 || b.Right==w || b.Bottom==h)
                    throw new Exception("Clipped pose at canvas edge: "+action+" / "+(i+1));
                extent=Math.Max(extent,Math.Max(anchors[i%8]-b.Left,b.Right-anchors[i%8]));
            }
            float scale=(float)Math.Min((double)height/poses.Max(p=>p.Bounds.Height),60.0/extent);
            for(int i=0;i<40;i++) {
                var pose=poses[i]; var b=pose.Bounds;
                var crop=Rectangle.Intersect(new Rectangle(0,0,w,h),Rectangle.Inflate(b,2,2));
                using(var cell=new Bitmap(crop.Width,crop.Height,PixelFormat.Format32bppArgb))
                using(var canvas=new Bitmap(128,112,PixelFormat.Format32bppArgb)) {
                    for(int y=0;y<crop.Height;y++) for(int x=0;x<crop.Width;x++) {
                        int sx=x+crop.Left, sy=y+crop.Top; bool near=false;
                        for(int yy=Math.Max(0,sy-2);!near && yy<=Math.Min(h-1,sy+2);yy++)
                            for(int xx=Math.Max(0,sx-2);xx<=Math.Min(w-1,sx+2);xx++)
                                if(labels[yy*w+xx]==pose.Id) { near=true; break; }
                        if(near && (uint)pixels[sy*w+sx]>>24>=32) cell.SetPixel(x,y,Color.FromArgb(pixels[sy*w+sx]));
                    }
                    using(var g=Graphics.FromImage(canvas)) {
                        g.Clear(Color.Transparent);
                        g.CompositingMode=CompositingMode.SourceCopy;
                        g.InterpolationMode=InterpolationMode.HighQualityBicubic;
                        g.PixelOffsetMode=PixelOffsetMode.HighQuality;
                        float y=action.StartsWith("air_") ? 8+(crop.Top-b.Top)*scale : 108+(crop.Top-b.Bottom)*scale;
                        float x=(float)(64+(crop.Left-anchors[i%8])*scale);
                        g.DrawImage(cell,new RectangleF(x,y,cell.Width*scale,cell.Height*scale));
                    }
                    canvas.Save(System.IO.Path.Combine(folder,action+"_"+(i+1).ToString("00")+".png"),ImageFormat.Png);
                }
            }
        }
    }
}
'@
}
$project = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$specs = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'prompts.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if ($Action -notin $specs.attacks.action) { throw "Unknown attack: $Action" }
$raw = Join-Path $PSScriptRoot "sheets/$Action.png"
New-Item -ItemType Directory -Force (Split-Path $raw) | Out-Null
Copy-Item -LiteralPath $Sheet -Destination $raw -Force
$destination = Join-Path $project "src/main/resources/assets/textures/fighter/attacks40/$Action"
New-Item -ItemType Directory -Force $destination | Out-Null
[AttackAtlas]::Export($raw,$destination,$Action)
$frames = @(1..40 | ForEach-Object {
    $path = Join-Path $destination ('{0}_{1:00}.png' -f $Action,$_)
    [ordered]@{frame=$_;sha256=(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLower();file="$Action/$([IO.Path]::GetFileName($path))"}
})
if (@($frames.sha256 | Select-Object -Unique).Count -ne 40) { throw "Repeated image in $Action" }
$frames | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $PSScriptRoot "$Action-manifest.json") -Encoding UTF8
Write-Output "$Action : 40 unique transparent frames exported."
