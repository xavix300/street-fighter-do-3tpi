package pt.aepombal.tpi.ruinas;

import com.sun.jna.Pointer;

/** Testes sem hardware, incluindo deteção, mapeamento e reconexão. */
public final class ComandoDualSenseTeste {
    private static final class Dispositivo implements ComandoDualSense.WinMM {
        int ligado = 2;
        int botoes;
        int x = 32767, y = 32767, pov = 65535;
        int fabricante = 0x054C, produto = 0x0CE6;
        String nome = "Comando de jogo";
        boolean temPov = true;
        int pesquisas;

        public int joyGetNumDevs() { return 4; }

        public int joyGetDevCapsW(Pointer indice, Pointer caps, int tamanho) {
            verificar(tamanho == 728);
            pesquisas++;
            if (Pointer.nativeValue(indice) != ligado) return 167;
            caps.setShort(0, (short) fabricante);
            caps.setShort(2, (short) produto);
            caps.setWideString(4, nome);
            caps.setInt(68, 0);
            caps.setInt(72, 65535);
            caps.setInt(76, 0);
            caps.setInt(80, 65535);
            caps.setInt(128, temPov ? 0x10 : 0);
            return 0;
        }

        public int joyGetPosEx(int indice, Pointer estado) {
            verificar(estado.getInt(0) == 52 && estado.getInt(4) == 0x283);
            if (indice != ligado) return 167;
            estado.setInt(8, x);
            estado.setInt(12, y);
            estado.setInt(32, botoes);
            estado.setInt(40, pov);
            return 0;
        }
    }

    public static void main(String[] args) {
        Dispositivo d = new Dispositivo();
        long[] tempo = {0};
        ComandoDualSense ps5 = new ComandoDualSense(d, () -> tempo[0]);
        ComandoWindows comando = new ComandoWindows((i, p) -> 1167, ps5);
        verificar(comando.botoes() == 0);
        comando.iniciar();
        comando.iniciar();
        verificar(comando.botoes() == 0);
        int[] fisicos = {1, 8, 32, 2, 4, 16, 256, 512};
        int[] esperados = {0x4000, 0x8000, 0x200, 0x1000, 0x2000, 0x100, 0x20, 0x10};
        for (int i = 0; i < fisicos.length; i++) {
            d.botoes = fisicos[i];
            verificar(comando.botoes() == esperados[i]);
        }
        d.botoes = 0;
        int[] direcoes = {1, 9, 8, 10, 2, 6, 4, 5};
        for (int i = 0; i < 8; i++) {
            d.pov = i * 4500;
            verificar(comando.botoes() == direcoes[i]);
        }
        d.pov = -1;
        verificar(comando.botoes() == 0);
        d.x = 0;
        d.y = 65535;
        verificar(comando.botoes() == 6);
        d.pov = 9000;
        verificar(comando.botoes() == 14); // D-pad e stick combinados.
        d.x = 32767 + 1000;
        d.y = 32767 - 1000;
        d.pov = 65535;
        verificar(comando.botoes() == 0); // Zona morta.
        d.ligado = -1;
        verificar(comando.botoes() == 0);
        int pesquisas = d.pesquisas;
        comando.botoes();
        verificar(d.pesquisas == pesquisas); // Não enumera dispositivos a cada frame.
        d.ligado = 3;
        d.fabricante = d.produto = 0;
        d.nome = "Wireless Controller"; // Fallback de nome (Bluetooth).
        d.botoes = 1;
        tempo[0] += 1_000_000_000L;
        verificar(comando.botoes() == 0x4000);
        comando.fechar();
        verificar(comando.botoes() == 0);
        d.nome = "Outro joystick";
        comando.iniciar();
        verificar(comando.botoes() == 0); // Não aplica o mapa Sony a comandos arbitrários.
        comando.fechar();
        d.nome = "DualSense Wireless Controller";
        d.temPov = false;
        d.pov = 0; // Sem POV, o zero não pode provocar saltos.
        comando.iniciar();
        verificar(comando.botoes() == 0x4000);
        comando.fechar();
        ComandoWindows xbox = new ComandoWindows((i, p) -> {
            p.clear(16);
            p.setShort(4, (short) 0x2000);
            return 0;
        }, new ComandoDualSense(d, () -> tempo[0]));
        xbox.iniciar();
        verificar(xbox.botoes() == 0x2000); // Prioridade XInput, sem duplicação.
        xbox.fechar();
        System.out.println("Comando DualSense: OK");
    }

    private static void verificar(boolean condicao) {
        if (!condicao) throw new AssertionError("Leitura DualSense incorreta");
    }
}
