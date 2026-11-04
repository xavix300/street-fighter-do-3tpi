$ErrorActionPreference = 'Stop'
Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
public static class ComandoXInput {
    [StructLayout(LayoutKind.Sequential)] public struct Gamepad {
        public ushort buttons; public byte lt, rt;
        public short lx, ly, rx, ry;
    }
    [StructLayout(LayoutKind.Sequential)] public struct State {
        public uint packet; public Gamepad gamepad;
    }
    [DllImport("xinput1_4.dll")] public static extern uint XInputGetState(uint index, out State state);
}
'@
while ($true) {
    $state = New-Object ComandoXInput+State
    $found = [ComandoXInput]::XInputGetState(0, [ref]$state) -eq 0
    if ($found) {
        $g = $state.gamepad
        $buttons = [int]$g.buttons
        if ($g.lx -lt -16000) { $buttons = $buttons -bor 4 }
        if ($g.lx -gt 16000) { $buttons = $buttons -bor 8 }
        if ($g.ly -gt 16000) { $buttons = $buttons -bor 1 }
        if ($g.ly -lt -16000) { $buttons = $buttons -bor 2 }
        [Console]::WriteLine($buttons)
    } else { [Console]::WriteLine('0') }
    [System.Threading.Thread]::Sleep(16)
}
