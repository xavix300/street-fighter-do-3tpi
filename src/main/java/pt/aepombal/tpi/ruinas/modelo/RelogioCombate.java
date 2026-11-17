package pt.aepombal.tpi.ruinas.modelo;

/** Um único relógio de 60 Hz para ambos os lutadores; nunca descarta frames ativos. */
public final class RelogioCombate {
    public static final double PASSO = 1.0 / 60.0;
    private double acumulado;

    public void atualizar(double segundos, Runnable passo) {
        if (!Double.isFinite(segundos) || segundos < 0) {
            throw new IllegalArgumentException("Tempo inválido");
        }
        acumulado += segundos;
        while (acumulado + 1e-9 >= PASSO) {
            acumulado = Math.max(0, acumulado - PASSO);
            passo.run();
        }
    }
}
