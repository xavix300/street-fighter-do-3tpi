package pt.aepombal.tpi.ruinas.modelo;

/** Trajetória e fase visual do salto, independentes da frequência de desenho. */
public final class SaltoLutador {
    private static final double IMPULSO = -720;
    private static final double GRAVIDADE = 1800;
    private double altura;
    private double velocidade;
    private int direcao;
    private boolean noChao = true;
    private boolean ataqueUsado;

    public void iniciar(int direcaoHorizontal) {
        if (!noChao) return;
        altura = 0;
        velocidade = IMPULSO;
        direcao = Integer.signum(direcaoHorizontal);
        noChao = false;
        ataqueUsado = false;
    }

    public void atualizar(double segundos) {
        if (noChao || segundos <= 0) return;
        altura += velocidade * segundos + 0.5 * GRAVIDADE * segundos * segundos;
        velocidade += GRAVIDADE * segundos;
        if (altura >= 0) {
            altura = 0;
            velocidade = 0;
            direcao = 0;
            noChao = true;
        }
    }

    public int indiceFrame(int quantidade) {
        if (noChao) return quantidade - 1;
        double progresso = Math.clamp((velocidade - IMPULSO) / (-2 * IMPULSO), 0, 1);
        return Math.min(quantidade - 1, (int) (progresso * quantidade));
    }

    public boolean consumirAtaque() {
        if (noChao || ataqueUsado) return false;
        ataqueUsado = true;
        return true;
    }

    public boolean noChao() { return noChao; }
    public boolean ataqueUsado() { return ataqueUsado; }
    public double altura() { return altura; }
    public int direcao() { return direcao; }
    public AcaoCombate acao() { return direcao == 0 ? AcaoCombate.JUMP : AcaoCombate.JUMP_SIDE; }
}
