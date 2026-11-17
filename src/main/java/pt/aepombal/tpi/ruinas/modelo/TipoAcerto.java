package pt.aepombal.tpi.ruinas.modelo;

/** Regras comuns de counter; não inclui reações especiais próprias de cada golpe de SF6. */
public enum TipoAcerto {
    NORMAL("ON HIT", 0),
    COUNTER_HIT("COUNTER HIT", 2),
    PUNISH_COUNTER("PUNISH COUNTER", 4),
    WHIFF_PUNISH("WHIFF PUNISH / PUNISH COUNTER", 4);

    private final String rotulo;
    private final int stunExtra;

    TipoAcerto(String rotulo, int stunExtra) {
        this.rotulo = rotulo;
        this.stunExtra = stunExtra;
    }

    public static TipoAcerto classificar(FaseAtaque fase, boolean bloqueado, boolean semContacto) {
        if (bloqueado) return NORMAL;
        return switch (fase) {
            case NEUTRA -> NORMAL;
            case PREPARACAO, ATIVA -> COUNTER_HIT;
            case RECUPERACAO -> semContacto ? WHIFF_PUNISH : PUNISH_COUNTER;
        };
    }

    public String rotulo() {
        return rotulo;
    }

    public int dano(int base) {
        return Math.max(1, (int) Math.round(base * (this == NORMAL ? 1.0 : 1.2)));
    }

    public int stun(int base) {
        return base + stunExtra;
    }
}
