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
    JUMP_SIDE("jump_side", false, false, false),
    BLOCK("block", true, false, false),
    LOW_BLOCK("low_block", true, false, false),
    CROUCH("crouch", true, false, false),
    LP("lp", false, true, false),
    MP("mp", false, true, false),
    HP("hp", false, true, false),
    LK("lk", false, true, false),
    MK("mk", false, true, false),
    HK("hk", false, true, false),
    CROUCH_LP("crouch_lp", false, true, false),
    CROUCH_MP("crouch_mp", false, true, false),
    CROUCH_HP("crouch_hp", false, true, false),
    CROUCH_LK("crouch_lk", false, true, true),
    CROUCH_MK("crouch_mk", false, true, true),
    CROUCH_HK("crouch_hk", false, true, true),
    ANTI_AIR_MP("anti_air_mp", false, true, false),
    ANTI_AIR_CROUCH_MP("anti_air_crouch_mp", false, true, false),
    AIR_LP("air_lp", false, true, false),
    AIR_MP("air_mp", false, true, false),
    AIR_HP("air_hp", false, true, false),
    AIR_LK("air_lk", false, true, false),
    AIR_MK("air_mk", false, true, false),
    AIR_HK("air_hk", false, true, false),
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

    /** Sequência fotográfica de 40 poses; os dados de combate mantêm o seu próprio relógio. */
    public String caminhoAtaque40(int indice) {
        if (!eAtaque() || indice < 1 || indice > 40) {
            throw new IllegalArgumentException("Frame de ataque fora de 1–40: " + this + " / " + indice);
        }
        return "/assets/textures/fighter/attacks40/" + pastaAssets + "/"
                + pastaAssets + "_" + String.format("%02d", indice) + ".png";
    }

    /** Caminho do PNG, preservando as pastas das sequências fotográficas antigas. */
    public String caminhoFrame(int indice) {
        if (eSalto() && indice >= 1 && indice <= 12) {
            int foto = new int[]{5, 6, 7, 8, 9, 9, 9, 9, 10, 10, 11, 12}[indice - 1];
            return "/assets/textures/fighter/sequences/jump_clean/jump_" + String.format("%03d", foto) + ".png";
        }
        if (this == ANTI_AIR_MP && indice >= 1 && indice <= 6) {
            return HP.caminhoFrame(new int[]{1, 5, 6, 7, 11, 14}[indice - 1]);
        }
        if (this == ANTI_AIR_CROUCH_MP && indice >= 1 && indice <= 6) {
            return CROUCH_HP.caminhoFrame(new int[]{1, 2, 3, 3, 5, 6}[indice - 1]);
        }
        boolean nova = this == LP || this == MP || this == LK || this == MK
                || this == CROUCH || this == LOW_BLOCK || name().startsWith("CROUCH_") || eAtaqueAereo();
        String pasta = this == HK ? "mk" : pastaAssets;
        return nova
                ? "/assets/textures/fighter/sequences/" + pastaAssets + "/"
                    + pastaAssets + "_" + String.format("%02d", indice) + ".png"
                : "/assets/textures/fighter/" + pasta + "/"
                    + pasta + "_" + String.format("%03d", indice) + ".png";
    }

    /** Indica se a animação volta ao primeiro frame quando chega ao fim. */
    public boolean repete() {
        return repete;
    }

    /** Indica se esta ação pode criar uma hitbox ofensiva. */
    public boolean eAtaque() {
        return ataque;
    }

    public boolean eSalto() { return this == JUMP || this == JUMP_SIDE; }
    public boolean eAtaqueAereo() { return name().startsWith("AIR_"); }
    public boolean exigeBloqueioAlto() { return eAtaqueAereo(); }

    /** No ar não se escolhem variantes de chão, agachadas ou anti-air. */
    public AcaoCombate aereo() {
        return switch (this) {
            case LP, CROUCH_LP, AIR_LP -> AIR_LP;
            case MP, CROUCH_MP, ANTI_AIR_MP, ANTI_AIR_CROUCH_MP, AIR_MP -> AIR_MP;
            case HP, CROUCH_HP, AIR_HP -> AIR_HP;
            case LK, CROUCH_LK, AIR_LK -> AIR_LK;
            case MK, CROUCH_MK, AIR_MK -> AIR_MK;
            case HK, CROUCH_HK, AIR_HK -> AIR_HK;
            default -> this;
        };
    }

    /** Indica se o golpe só pode ser bloqueado em guarda baixa. */
    public boolean exigeBloqueioBaixo() {
        return exigeBloqueioBaixo;
    }

    /** Nível de força usado nas cadeias de cancel: leve 1, médio 2 e forte 3. */
    public int nivelForca() {
        return switch (this) {
            case LP, LK, CROUCH_LP, CROUCH_LK, AIR_LP, AIR_LK -> 1;
            case MP, MK, CROUCH_MP, CROUCH_MK, ANTI_AIR_MP, ANTI_AIR_CROUCH_MP, AIR_MP, AIR_MK -> 2;
            case HP, HK, CROUCH_HP, CROUCH_HK, AIR_HP, AIR_HK -> 3;
            default -> 0;
        };
    }

    /** Variante usada quando o ataque começa com baixo premido. */
    public AcaoCombate agachado() {
        return switch (this) {
            case LP -> CROUCH_LP;
            case MP -> CROUCH_MP;
            case HP -> CROUCH_HP;
            case LK -> CROUCH_LK;
            case MK -> CROUCH_MK;
            case HK -> CROUCH_HK;
            default -> this;
        };
    }

    /** Trás é relativo ao adversário; apenas trás + MP escolhe o anti-air. */
    public AcaoCombate selecionarAtaque(boolean baixo, boolean tras) {
        if (this == MP && tras) {
            return baixo ? ANTI_AIR_CROUCH_MP : ANTI_AIR_MP;
        }
        return baixo ? agachado() : this;
    }

    /** Converte o código guardado no ficheiro de dados para a ação correspondente. */
    public static AcaoCombate porCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(acao -> acao.name().equalsIgnoreCase(codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ação desconhecida: " + codigo));
    }
}
