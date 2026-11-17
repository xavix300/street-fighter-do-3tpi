package pt.aepombal.tpi.ruinas.logica;

/** Parâmetros de feedback do protótipo, independentes das entidades e do motor gráfico. */
public record ReacaoContacto(int pausaFrames, double velocidadeRecuo, int recuoFrames) {

    /** Mantém a pausa comum e empurra o defensor para longe do atacante. */
    public static ReacaoContacto calcular(boolean bloqueado, double atacanteX, double defensorX) {
        double velocidade = bloqueado ? 90.0 : 150.0;
        if (defensorX < atacanteX) {
            velocidade = -velocidade;
        }
        return new ReacaoContacto(5, velocidade, 9);
    }
}
