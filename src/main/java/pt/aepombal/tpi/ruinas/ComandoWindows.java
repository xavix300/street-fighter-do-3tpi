package pt.aepombal.tpi.ruinas;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/** Leitura nativa XInput no thread do jogo; o teclado funciona mesmo sem comando. */
final class ComandoWindows {
    static final int ZONA_MORTA = 16000;
    private XInput xinput;
    private Memory estado;
    private int indice = -1;
    private boolean iniciado;

    public interface XInput extends StdCallLibrary {
        int XInputGetState(int indice, Pointer estado);
    }

    ComandoWindows() { }

    // Permite testar a deteção e os botões sem um comando físico.
    ComandoWindows(XInput xinput) {
        this.xinput = xinput;
    }

    void iniciar() {
        if (iniciado) return;
        iniciado = true;
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
        if (estado == null) return 0;
        if (indice >= 0 && xinput.XInputGetState(indice, estado) == 0) return lerBotoes();
        indice = -1;
        // O comando pode estar em qualquer uma das quatro posições XInput.
        for (int i = 0; i < 4; i++) {
            if (xinput.XInputGetState(i, estado) == 0) {
                indice = i;
                return lerBotoes();
            }
        }
        return 0; // Desligar o comando liberta também as direções.
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
        if (estado != null) estado.close();
        estado = null;
        indice = -1;
        iniciado = false;
    }
}
