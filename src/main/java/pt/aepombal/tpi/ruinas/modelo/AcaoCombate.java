package pt.aepombal.tpi.ruinas.modelo;

import java.util.Arrays;

// [IA] Enum central que liga um comando, os ficheiros de frames e as regras de animação.
/**
 * Estados visuais e ações que um lutador pode executar.
 * Cada ação usa um identificador igual ao nome da pasta de assets.
 */
public enum AcaoCombate {

    IDLE("idle", true, false, false),
    STANCE("stance", true, false, false),
    GUARD("guard", true, false, false),
    WALK_FORWARD("walk_forward", true, false, false),
    WALK_BACKWARD("walk_backward", true, false, false),
    JUMP("jump", false, false, false),
    BLOCK("block", true, false, false),
    LOW_BLOCK("low_block", true, false, false),
    LP("lp", false, true, false),
    MP("mp", false, true, false),
    HP("hp", false, true, false),
    LK("lk", false, true, true),
    MK("mk", false, true, true),
    HK("hk", false, true, false),
    HIT("hit", false, false, false),
    KNOCKOUT("knockout", false, false, false),
    TAUNT("taunt", false, false, false),
    VICTORY("victory", false, false, false),
    CELEBRATION("celebration", false, false, false);

    private final String pastaAssets;
    private final boolean repete;
    private final boolean ataque;
    private final boolean exigeBloqueioBaixo;

    AcaoCombate(String pastaAssets, boolean repete, boolean ataque, boolean exigeBloqueioBaixo) {
        this.pastaAssets = pastaAssets;
        this.repete = repete;
        this.ataque = ataque;
        this.exigeBloqueioBaixo = exigeBloqueioBaixo;
    }

    /** Devolve o nome da pasta que contém os frames desta ação. */
    public String pastaAssets() {
        return pastaAssets;
    }

    /** Indica se a animação volta ao primeiro frame quando chega ao fim. */
    public boolean repete() {
        return repete;
    }

    /** Indica se esta ação pode criar uma hitbox ofensiva. */
    public boolean eAtaque() {
        return ataque;
    }

    /** Indica se o golpe só pode ser bloqueado em guarda baixa. */
    public boolean exigeBloqueioBaixo() {
        return exigeBloqueioBaixo;
    }

    /** Nível de força usado nas cadeias de cancel: leve 1, médio 2 e forte 3. */
    public int nivelForca() {
        return switch (this) {
            case LP, LK -> 1;
            case MP, MK -> 2;
            case HP, HK -> 3;
            default -> 0;
        };
    }

    /** Converte o código guardado no ficheiro de dados para a ação correspondente. */
    public static AcaoCombate porCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(acao -> acao.name().equalsIgnoreCase(codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ação desconhecida: " + codigo));
    }
}
