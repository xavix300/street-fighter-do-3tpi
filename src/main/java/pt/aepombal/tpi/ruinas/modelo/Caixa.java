package pt.aepombal.tpi.ruinas.modelo;

// [IA] Caixa simples e independente do FXGL para dados de hitbox e hurtbox.
/**
 * Retângulo relativo ao canto superior esquerdo do lutador.
 * As caixas são lidas de dados/frames.csv, uma por frame visual.
 */
public record Caixa(double x, double y, double largura, double altura) {

    public Caixa {
        if (largura <= 0 || altura <= 0) {
            throw new IllegalArgumentException("Uma caixa precisa de largura e altura positivas.");
        }
    }

    /** Testa a interseção de duas caixas já convertidas para coordenadas do mundo. */
    public boolean interceta(Caixa outra) {
        return x < outra.x + outra.largura
                && x + largura > outra.x
                && y < outra.y + outra.altura
                && y + altura > outra.y;
    }

    /** Espelha a caixa horizontalmente dentro de um lutador com a largura indicada. */
    public Caixa espelhada(double larguraLutador) {
        return new Caixa(larguraLutador - x - largura, y, largura, altura);
    }

    /** Converte uma caixa relativa numa caixa em coordenadas do mundo. */
    public Caixa noMundo(double origemX, double origemY) {
        return new Caixa(origemX + x, origemY + y, largura, altura);
    }
}
