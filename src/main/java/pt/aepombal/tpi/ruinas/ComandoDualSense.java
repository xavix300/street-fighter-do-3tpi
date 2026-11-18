package pt.aepombal.tpi.ruinas;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import java.util.Locale;
import java.util.function.LongSupplier;
import java.util.HashSet;
import java.util.Set;

/** DualSense HID através do WinMM, sem emulação de comando Xbox. */
final class ComandoDualSense {
    private static final int TAM_ESTADO = 52; // JOYINFOEX: 13 DWORDs.
    private static final int TAM_CAPACIDADES = 728; // JOYCAPSW (UTF-16).
    private final LongSupplier relogio;
    private final boolean carregarBiblioteca;
    private WinMM winmm;
    private Memory estado;
    private Memory capacidades;
    private int indice = -1;
    private int xmin, xmax, ymin, ymax;
    private boolean temPov;
    private long proximaPesquisa;
    private final Set<Integer> reservados;
    private static final int[] DIRECOES_POV = {1, 9, 8, 10, 2, 6, 4, 5};

    public interface WinMM extends StdCallLibrary {
        int joyGetNumDevs();
        int joyGetDevCapsW(Pointer indice, Pointer capacidades, int tamanho);
        int joyGetPosEx(int indice, Pointer estado);
    }

    ComandoDualSense() {
        this(new HashSet<>());
    }

    ComandoDualSense(Set<Integer> reservados) {
        this(null, System::nanoTime, true, reservados);
    }

    ComandoDualSense(WinMM winmm, LongSupplier relogio) {
        this(winmm, relogio, new HashSet<>());
    }

    ComandoDualSense(WinMM winmm, LongSupplier relogio, Set<Integer> reservados) {
        this(winmm, relogio, false, reservados);
    }

    private ComandoDualSense(WinMM winmm, LongSupplier relogio, boolean carregarBiblioteca,
                            Set<Integer> reservados) {
        this.winmm = winmm;
        this.relogio = relogio;
        this.carregarBiblioteca = carregarBiblioteca;
        this.reservados = reservados;
    }

    void iniciar() {
        if (estado != null) return;
        if (winmm == null && carregarBiblioteca && Platform.isWindows()) {
            try {
                winmm = Native.load("winmm", WinMM.class);
            } catch (UnsatisfiedLinkError ignorada) {
                // Sem WinMM, o teclado e o XInput continuam disponíveis.
            }
        }
        if (winmm == null) return;
        estado = new Memory(TAM_ESTADO);
        capacidades = new Memory(TAM_CAPACIDADES);
        proximaPesquisa = relogio.getAsLong();
    }

    int botoes() {
        if (estado == null) return 0;
        if (indice >= 0) {
            if (lerEstado(indice)) return traduzir();
            reservados.remove(indice);
            indice = -1;
            proximaPesquisa = relogio.getAsLong();
        }
        long agora = relogio.getAsLong();
        if (agora - proximaPesquisa < 0) return 0;
        proximaPesquisa = agora + 1_000_000_000L;
        for (int i = 0; i < winmm.joyGetNumDevs(); i++) {
            if (reservados.contains(i)) continue;
            capacidades.clear();
            if (winmm.joyGetDevCapsW(Pointer.createConstant(i), capacidades, TAM_CAPACIDADES) != 0)
                continue;
            int fabricante = Short.toUnsignedInt(capacidades.getShort(0));
            int produto = Short.toUnsignedInt(capacidades.getShort(2));
            String nome = capacidades.getWideString(4).toLowerCase(Locale.ROOT);
            boolean sony = fabricante == 0x054C && (produto == 0x0CE6 || produto == 0x0DF2);
            if (!sony && !nome.contains("dualsense") && !nome.equals("wireless controller"))
                continue;
            if (!lerEstado(i)) continue;
            xmin = capacidades.getInt(68);
            xmax = capacidades.getInt(72);
            ymin = capacidades.getInt(76);
            ymax = capacidades.getInt(80);
            temPov = (capacidades.getInt(128) & 0x10) != 0;
            indice = i;
            reservados.add(i);
            return traduzir();
        }
        return 0;
    }

    private boolean lerEstado(int i) {
        estado.clear();
        estado.setInt(0, TAM_ESTADO);
        estado.setInt(4, 0x283); // X | Y | BUTTONS | POVCTS (diagonais).
        return winmm.joyGetPosEx(i, estado) == 0;
    }

    private int traduzir() {
        int fisicos = estado.getInt(32);
        int botoes = 0;
        if ((fisicos & 1) != 0) botoes |= 0x4000; // Quadrado: LP.
        if ((fisicos & 8) != 0) botoes |= 0x8000; // Triângulo: MP.
        if ((fisicos & 32) != 0) botoes |= 0x0200; // R1: HP.
        if ((fisicos & 2) != 0) botoes |= 0x1000; // Cruz: LK.
        if ((fisicos & 4) != 0) botoes |= 0x2000; // Círculo: MK.
        if ((fisicos & 16) != 0) botoes |= 0x0100; // L1: HK.
        if ((fisicos & 256) != 0) botoes |= 0x0020; // Create: provocação.
        if ((fisicos & 512) != 0) botoes |= 0x0010; // Options: pausa.
        botoes |= eixo(estado.getInt(8), xmin, xmax, 4, 8);
        botoes |= eixo(estado.getInt(12), ymin, ymax, 1, 2);
        int pov = estado.getInt(40);
        if (temPov && pov >= 0 && pov < 36000) {
            int setor = ((pov + 2250) / 4500) % 8;
            botoes |= DIRECOES_POV[setor];
        }
        return botoes;
    }

    private static int eixo(int valor, int min, int max, int negativo, int positivo) {
        if (max <= min) return 0;
        double normalizado = 2.0 * (valor - (double) min) / (max - (double) min) - 1;
        double zonaMorta = ComandoWindows.ZONA_MORTA / 32768.0;
        return normalizado < -zonaMorta ? negativo : normalizado > zonaMorta ? positivo : 0;
    }

    void fechar() {
        reservados.remove(indice);
        if (estado != null) estado.close();
        if (capacidades != null) capacidades.close();
        estado = null;
        capacidades = null;
        indice = -1;
    }

    boolean ligado() {
        return indice >= 0;
    }
}
