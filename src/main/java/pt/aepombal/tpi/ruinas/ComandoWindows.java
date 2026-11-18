package pt.aepombal.tpi.ruinas;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import java.util.HashSet;
import java.util.Set;

/** Leitura nativa Xbox / DualSense; o teclado funciona mesmo sem comando. */
final class ComandoWindows {
    static final int ZONA_MORTA = 16000;
    private XInput xinput;
    private Memory estado;
    private int indice = -1;
    private boolean iniciado;
    private final ComandoDualSense dualSense;
    private final Set<Integer> reservados;

    public interface XInput extends StdCallLibrary {
        int XInputGetState(int indice, Pointer estado);
    }

    ComandoWindows() {
        this(new HashSet<>(), new HashSet<>());
    }

    ComandoWindows(Set<Integer> xboxReservados, Set<Integer> sonyReservados) {
        this(null, new ComandoDualSense(sonyReservados), xboxReservados);
    }

    // Permite testar a deteção e os botões sem um comando físico.
    ComandoWindows(XInput xinput) {
        this(xinput, new ComandoDualSense(null, System::nanoTime));
    }

    ComandoWindows(XInput xinput, ComandoDualSense dualSense) {
        this(xinput, dualSense, new HashSet<>());
    }

    ComandoWindows(XInput xinput, ComandoDualSense dualSense, Set<Integer> reservados) {
        this.xinput = xinput;
        this.dualSense = dualSense;
        this.reservados = reservados;
    }

    void iniciar() {
        if (iniciado) return;
        iniciado = true;
        dualSense.iniciar();
        if (xinput == null && Platform.isWindows()) {
            for (String dll : new String[]{"xinput1_4", "xinput1_3", "xinput9_1_0"}) {
                try {
                    xinput = Native.load(dll, XInput.class);
                    break;
                } catch (UnsatisfiedLinkError ignorada) {
                    // Tenta a versão seguinte do XInput; sem DLL, mantém o teclado.
                }
            }
        }
        if (xinput != null) estado = new Memory(16); // sizeof(XINPUT_STATE)
    }

    int botoes() {
        if (!iniciado) return 0;
        // Mantém a fonte escolhida: ligar outro comando não troca os jogadores.
        if (dualSense.ligado()) {
            int botoes = dualSense.botoes();
            if (dualSense.ligado()) return botoes;
        }
        if (estado == null) return dualSense.botoes();
        if (indice >= 0 && xinput.XInputGetState(indice, estado) == 0) return lerBotoes();
        reservados.remove(indice);
        indice = -1;
        // O comando pode estar em qualquer uma das quatro posições XInput.
        for (int i = 0; i < 4; i++) {
            if (!reservados.contains(i) && xinput.XInputGetState(i, estado) == 0) {
                indice = i;
                reservados.add(i);
                return lerBotoes();
            }
        }
        return dualSense.botoes(); // Desligar o comando liberta também as direções.
    }

    private int lerBotoes() {
        // DWORD packet (0), WORD buttons (4), BYTE triggers (6/7), SHORT sticks (8...).
        int botoes = Short.toUnsignedInt(estado.getShort(4));
        int x = estado.getShort(8);
        int y = estado.getShort(10);
        if (x < -ZONA_MORTA) botoes |= 4;
        if (x > ZONA_MORTA) botoes |= 8;
        if (y > ZONA_MORTA) botoes |= 1;
        if (y < -ZONA_MORTA) botoes |= 2;
        return botoes;
    }

    void fechar() {
        dualSense.fechar();
        reservados.remove(indice);
        if (estado != null) estado.close();
        estado = null;
        indice = -1;
        iniciado = false;
    }

    boolean ligado() {
        return indice >= 0 || dualSense.ligado();
    }
}
