package pt.aepombal.tpi.ruinas.modelo;

/** Trajetória e fase visual do salto, independentes da frequência de desenho. */
public final class SaltoLutador {
    private double altura;
    private double tempo;
    private int direcao;
    private boolean noChao = true;
    private boolean ataqueUsado;
    private double deslocamentoHorizontal;

    public void iniciar(int direcaoHorizontal) {
        if (!noChao) return;
        altura = 0;
        tempo = 0;
        deslocamentoHorizontal = 0;
        direcao = Integer.signum(direcaoHorizontal);
        noChao = false;
        ataqueUsado = false;
    }

    public void atualizar(double segundos) {
        deslocamentoHorizontal = 0;
        if (noChao || segundos <= 0) return;
        double anterior = progressoAereo();
        tempo += segundos;
        double progresso = progressoAereo();
        deslocamentoHorizontal = direcao * MovimentoRyu.DISTANCIA_SALTO * (progresso - anterior);
        altura = -4 * MovimentoRyu.ALTURA_SALTO * progresso * (1 - progresso);
        if (progresso >= 1) {
            altura = 0;
            direcao = 0;
            noChao = true;
        }
    }

    public int indiceFrame(int quantidade) {
        if (noChao) return quantidade - 1;
        double progresso = progressoAereo();
        return Math.min(quantidade - 1, (int) (progresso * quantidade));
    }

    public boolean consumirAtaque() {
        if (!noAr() || ataqueUsado) return false;
        ataqueUsado = true;
        return true;
    }

    public boolean noChao() { return noChao; }
    public double deslocamentoHorizontal() { return deslocamentoHorizontal; }
    public boolean aPreparar() {
        return !noChao && tempo < MovimentoRyu.PRE_SALTO / 60.0 - 1e-9;
    }

    /** Um golpe durante o prejump interrompe a tentativa antes de sair do chão. */
    public void interromperPreparacao() {
        if (!aPreparar()) return;
        noChao = true;
        direcao = 0;
        altura = 0;
        deslocamentoHorizontal = 0;
    }
    public boolean noAr() { return !noChao && !aPreparar(); }
    private double progressoAereo() {
        int frames = direcao == 0 ? MovimentoRyu.SALTO_NEUTRO : MovimentoRyu.SALTO_DIAGONAL;
        double decorrido = tempo * 60 - MovimentoRyu.PRE_SALTO;
        return decorrido >= frames - 1e-8 ? 1 : Math.clamp(decorrido / frames, 0, 1);
    }
    public boolean ataqueUsado() { return ataqueUsado; }
    public double altura() { return altura; }
    public int direcao() { return direcao; }
    public AcaoCombate acao() { return direcao == 0 ? AcaoCombate.JUMP : AcaoCombate.JUMP_SIDE; }
}
