package pt.aepombal.tpi.ruinas;

import com.sun.jna.Pointer;
import java.util.HashSet;
import java.util.Set;

public final class DoisComandosTeste {
    public static void main(String[] args) {
        boolean[] ligados = {true, false, true, false};
        Set<Integer> xboxReservados = new HashSet<>();
        ComandoWindows.XInput api = (i, estado) -> {
            if (!ligados[i]) return 1167;
            estado.clear(16);
            estado.setShort(4, (short) (i == 0 ? 0x4004 : 0x8008));
            return 0;
        };
        ComandoWindows p1 = new ComandoWindows(api,
                new ComandoDualSense(null, System::nanoTime), xboxReservados);
        ComandoWindows p2 = new ComandoWindows(api,
                new ComandoDualSense(null, System::nanoTime), xboxReservados);
        p1.iniciar(); p2.iniciar();
        verificar(p1.botoes() == 0x4004 && p2.botoes() == 0x8008);
        ligados[0] = false;
        verificar(p1.botoes() == 0 && p2.botoes() == 0x8008);
        ligados[0] = true;
        verificar(p1.botoes() == 0x4004 && p2.botoes() == 0x8008);
        p1.fechar(); p2.fechar();
        verificar(xboxReservados.isEmpty());

        Set<Integer> sonyReservados = new HashSet<>();
        long[] tempo = {0};
        boolean[] sonyLigados = {true, true};
        ComandoDualSense.WinMM sony = new ComandoDualSense.WinMM() {
            public int joyGetNumDevs() { return 2; }
            public int joyGetDevCapsW(Pointer indice, Pointer caps, int tamanho) {
                int i = (int) Pointer.nativeValue(indice);
                if (!sonyLigados[i]) return 167;
                caps.setShort(0, (short) 0x054C);
                caps.setShort(2, (short) 0x0CE6);
                caps.setInt(72, 65535);
                caps.setInt(80, 65535);
                return 0;
            }
            public int joyGetPosEx(int i, Pointer estado) {
                if (!sonyLigados[i]) return 167;
                estado.setInt(8, 32767);
                estado.setInt(12, 32767);
                estado.setInt(32, i == 0 ? 1 : 8);
                return 0;
            }
        };
        var ds1 = new ComandoDualSense(sony, () -> tempo[0], sonyReservados);
        var ds2 = new ComandoDualSense(sony, () -> tempo[0], sonyReservados);
        ds1.iniciar(); ds2.iniciar();
        verificar(ds1.botoes() == 0x4000 && ds2.botoes() == 0x8000);
        sonyLigados[0] = false;
        verificar(ds1.botoes() == 0 && ds2.botoes() == 0x8000);
        sonyLigados[0] = true;
        tempo[0] += 1_000_000_000L;
        verificar(ds1.botoes() == 0x4000 && ds2.botoes() == 0x8000);
        ds1.fechar(); ds2.fechar();
        verificar(sonyReservados.isEmpty());

        // Um Xbox e um PS5, com reservas independentes por API.
        ligados[2] = false;
        p1 = new ComandoWindows(api, new ComandoDualSense(sony, () -> tempo[0], sonyReservados), xboxReservados);
        p2 = new ComandoWindows(api, new ComandoDualSense(sony, () -> tempo[0], sonyReservados), xboxReservados);
        p1.iniciar(); p2.iniciar();
        verificar(p1.botoes() == 0x4004 && p2.botoes() == 0x4000);
        ligados[2] = true;
        verificar(p2.botoes() == 0x4000); // Não troca o PS5 já atribuído por um Xbox recém-ligado.
        p1.fechar(); p2.fechar();
        System.out.println("Dois comandos: OK (Xbox/Xbox, PS5/PS5, Xbox/PS5 e reconexão)");
    }

    private static void verificar(boolean condicao) {
        if (!condicao) throw new AssertionError("Um comando foi atribuído ao jogador errado");
    }
}
