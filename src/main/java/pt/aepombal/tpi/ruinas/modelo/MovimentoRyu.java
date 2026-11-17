package pt.aepombal.tpi.ruinas.modelo;

/** Valores de movimento SF6 em frames; conversão explícita para a arena fotográfica. */
public final class MovimentoRyu {
    public static final int FPS = 60;
    public static final int PRE_SALTO = 4;
    public static final int SALTO_NEUTRO = 39;
    public static final int SALTO_DIAGONAL = 38;
    public static final int RECUPERACAO_AEREO = 3;
    // Mantém os 235 px/s anteriores de avanço, preservando agora a proporção 4,7 : 3,2.
    public static final double PIXELS_POR_UNIDADE = 5.0 / 6.0;
    // Altura adaptada às fotos/arena, não um valor medido do SF6.
    public static final double ALTURA_SALTO = 144;
    public static final double DISTANCIA_SALTO = 190 * PIXELS_POR_UNIDADE;

    private MovimentoRyu() { }

    public static double velocidadeAndar(boolean frente) {
        return (frente ? 4.7 : 3.2) * FPS * PIXELS_POR_UNIDADE;
    }

    public static double velocidadeSalto() {
        return DISTANCIA_SALTO * FPS / SALTO_DIAGONAL;
    }
}
