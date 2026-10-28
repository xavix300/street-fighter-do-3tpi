package pt.aepombal.tpi.ruinas.componentes;

import com.almasb.fxgl.entity.component.Component;
import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;
import pt.aepombal.tpi.ruinas.modelo.Caixa;
import pt.aepombal.tpi.ruinas.modelo.CaixaFrame;
import pt.aepombal.tpi.ruinas.modelo.DefinicaoAnimacao;
import pt.aepombal.tpi.ruinas.modelo.FrameCombate;
import pt.aepombal.tpi.ruinas.modelo.LadoLutador;
import pt.aepombal.tpi.ruinas.modelo.ResultadoImpacto;
import pt.aepombal.tpi.ruinas.modelo.TipoCaixa;

import java.util.List;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.getAppWidth;

// [IA] Componente de luta que aplica os frames, movimento, bloqueio e dano de um lutador.
/**
 * Estado e comportamento comum a qualquer lutador.
 * As caixas usadas em cada atualização vêm sempre do frame lógico atual.
 */
public final class ComponenteLutador extends Component {

    public static final double LARGURA = 64;
    public static final double ALTURA = 108;
    private static final double CHAO_Y = 394;
    private static final double VELOCIDADE = 235;
    private static final double VELOCIDADE_SALTO_HORIZONTAL = 205;
    private static final double GRAVIDADE = 1800;
    private static final double IMPULSO_SALTO = -720;
    private static final double GUARDA_MAXIMA = 100;
    private static final double RECUPERACAO_GUARDA_POR_SEGUNDO = 18;
    private static final double ATRASO_RECUPERACAO_GUARDA = 1.25;
    private static final int STUN_QUEBRA_GUARDA = 54;
    private static final double JANELA_BUFFER_ATAQUE = 7.0 / 60.0;

    private final LadoLutador lado;
    private final GestorCombate gestor;
    private final Map<AcaoCombate, DefinicaoAnimacao> animacoes;
    private final VistaLutador vista;
    private int vida = 100;
    private double guarda = GUARDA_MAXIMA;
    private int guardaPublicada = (int) GUARDA_MAXIMA;
    private AcaoCombate acao = AcaoCombate.IDLE;
    private int indiceFrame;
    private double tempoNoFrame;
    private double tempoStun;
    private double tempoHitstop;
    private double atrasoRecuperacaoGuarda;
    private double tempoRecuo;
    private double velocidadeRecuo;
    private double tempoBufferAtaque;
    private double velocidadeY;
    private int movimento;
    private int movimentoRelativo;
    private int movimentoSalto;
    private int orientacaoAtual;
    private boolean movimentoRelativoAoOponente;
    private boolean defesaManualPedida;
    private boolean defesaBaixaPedida;
    private boolean noChao = true;
    private boolean acertoRegistado;
    private AcaoCombate ataquePendente;

    public ComponenteLutador(LadoLutador lado, GestorCombate gestor,
                              Map<AcaoCombate, DefinicaoAnimacao> animacoes, VistaLutador vista) {
        this.lado = lado;
        this.gestor = gestor;
        this.animacoes = animacoes;
        this.vista = vista;
        this.orientacaoAtual = lado.orientacao();
    }

    /** Começa a andar na direção do adversário. */
    public void moverParaFrente() {
        iniciarMovimento(1, AcaoCombate.WALK_FORWARD);
    }

    /** Começa a andar na direção oposta ao adversário. */
    public void moverParaTras() {
        iniciarMovimento(-1, AcaoCombate.WALK_BACKWARD);
    }

    /** Move numa direção fixa do ecrã: -1 para a esquerda, +1 para a direita. */
    public void moverHorizontal(int direcaoMundo) {
        movimentoRelativoAoOponente = false;
        movimento = Integer.signum(direcaoMundo);
        movimentoRelativo = movimento * orientacaoAtual;
        if (podeControlar() && noChao && !defesaManualPedida) {
            AcaoCombate novaAcao = acaoParaMovimento();
            if (novaAcao != acao) {
                mudarAcao(acaoParaMovimento());
            }
        }
    }

    /** Pára o movimento horizontal quando não está em ataque ou stun. */
    public void parar() {
        movimento = 0;
        movimentoRelativo = 0;
        if (podeControlar() && noChao && !defesaManualPedida
                && (acao == AcaoCombate.WALK_FORWARD || acao == AcaoCombate.WALK_BACKWARD)) {
            mudarAcao(AcaoCombate.IDLE);
        }
    }

    /** Inicia um salto neutro ou diagonal se o lutador estiver no chão e livre para agir. */
    public void saltar(int direcaoHorizontal) {
        if (podeControlar() && noChao) {
            velocidadeY = IMPULSO_SALTO;
            noChao = false;
            movimentoSalto = Integer.signum(direcaoHorizontal);
            mudarAcao(AcaoCombate.JUMP);
        }
    }

    /** Entra em bloqueio alto ou baixo. */
    public void bloquear(boolean baixo) {
        defesaManualPedida = true;
        defesaBaixaPedida = baixo;
        if (podeControlar() && noChao) {
            AcaoCombate defesa = baixo ? AcaoCombate.LOW_BLOCK : AcaoCombate.BLOCK;
            if (acao != defesa) {
                mudarAcao(defesa);
            }
        }
    }

    /** Termina a defesa manual quando Q/S deixam de estar premidos. */
    public void terminarBloqueio() {
        defesaManualPedida = false;
        defesaBaixaPedida = false;
        if (tempoStun == 0 && podeControlar() && noChao
                && (acao == AcaoCombate.BLOCK || acao == AcaoCombate.LOW_BLOCK)) {
            mudarAcao(acaoParaMovimento());
        }
    }

    /** Guarda o ataque durante sete frames e executa-o assim que ficar permitido. */
    public void atacar(AcaoCombate novoAtaque) {
        if (novoAtaque.eAtaque() && vida > 0) {
            ataquePendente = novoAtaque;
            tempoBufferAtaque = JANELA_BUFFER_ATAQUE;
            tentarExecutarAtaquePendente();
        }
    }

    /** Devolve o lado deste lutador. */
    public LadoLutador lado() {
        return lado;
    }

    /** Devolve a vida restante. */
    public int vida() {
        return vida;
    }

    /** Devolve a resistência de guarda visível, entre 0 e 100. */
    public int guarda() {
        return (int) Math.ceil(guarda);
    }

    /** Devolve a ação que está a ser apresentada neste instante. */
    public AcaoCombate acao() {
        return acao;
    }

    /** Devolve o frame lógico que está ativo neste momento. */
    public FrameCombate frameAtual() {
        return animacaoAtual().frame(indiceFrame);
    }

    /** Indica se uma hitbox ofensiva desta ação já acertou no adversário. */
    public boolean acertoRegistado() {
        return acertoRegistado;
    }

    /** Impede que a mesma animação de ataque cause dano mais do que uma vez. */
    public void registarAcerto() {
        acertoRegistado = true;
    }

    /** Converte as caixas relativas deste frame para coordenadas do mundo. */
    public List<CaixaFrame> caixasNoMundo(TipoCaixa tipo) {
        return frameAtual().caixasDoTipo(tipo).stream().map(this::transformar).toList();
    }

    /** Testa se o bloqueio atual defende corretamente o golpe recebido. */
    public boolean bloqueia(AcaoCombate ataque) {
        if (guarda <= 0) {
            return false;
        }
        return (acao == AcaoCombate.LOW_BLOCK && ataque.exigeBloqueioBaixo())
                || ((acao == AcaoCombate.BLOCK || acao == AcaoCombate.WALK_BACKWARD)
                    && !ataque.exigeBloqueioBaixo());
    }

    /** Aplica um impacto, usando stun de bloqueio quando a defesa foi correta. */
    public ResultadoImpacto receberImpacto(CaixaFrame hitbox, boolean bloqueado,
                                            double escalaDano) {
        if (bloqueado) {
            guarda = Math.max(0, guarda - Math.max(8, hitbox.dano() * 0.6));
            atrasoRecuperacaoGuarda = ATRASO_RECUPERACAO_GUARDA;
            publicarGuarda();

            if (guarda == 0) {
                tempoStun = STUN_QUEBRA_GUARDA / 60.0;
                atrasoRecuperacaoGuarda = 2.0;
                mudarAcao(AcaoCombate.HIT);
                return new ResultadoImpacto(false, true, STUN_QUEBRA_GUARDA, 0);
            }

            tempoStun = hitbox.blockStun() / 60.0;
            return new ResultadoImpacto(true, false, hitbox.blockStun(), 0);
        }

        tempoStun = hitbox.hitStun() / 60.0;
        atrasoRecuperacaoGuarda = Math.max(atrasoRecuperacaoGuarda, 0.6);
        int danoEfetivo = Math.max(1, (int) Math.round(hitbox.dano() * escalaDano));
        int vidaAnterior = vida;
        vida = Math.max(0, vida - danoEfetivo);
        danoEfetivo = vidaAnterior - vida;
        mudarAcao(vida == 0 ? AcaoCombate.KNOCKOUT : AcaoCombate.HIT);
        gestor.atualizarVida(this);
        return new ResultadoImpacto(false, false, hitbox.hitStun(), danoEfetivo);
    }

    /** Frames que faltam até o atacante recuperar e voltar a poder agir. */
    public int framesRecuperacaoRestantes() {
        if (!acao.eAtaque()) {
            return 0;
        }
        double restantes = Math.max(0, frameAtual().duracaoFrames() - tempoNoFrame * 60.0);
        for (int i = indiceFrame + 1; i < animacaoAtual().frames().size(); i++) {
            restantes += animacaoAtual().frames().get(i).duracaoFrames();
        }
        return (int) Math.ceil(restantes);
    }

    /** Indica se um novo golpe pode continuar o combo atual. */
    public boolean estaEmHitstun() {
        return tempoStun > 0 && acao == AcaoCombate.HIT;
    }

    /** Congela animação, movimento e stun durante o impacto. */
    public void aplicarHitstop(int frames) {
        tempoHitstop = Math.max(tempoHitstop, frames / 60.0);
    }

    /** Aplica deslocamento residual depois de um acerto ou bloqueio. */
    public void aplicarRecuo(double velocidade, int frames) {
        velocidadeRecuo += velocidade;
        tempoRecuo = Math.max(tempoRecuo, frames / 60.0);
    }

    /** Move o lutador uma pequena distância sem ultrapassar os limites da arena. */
    public void empurrar(double distancia) {
        double novoX = Math.clamp(entity.getX() + distancia, 12, getAppWidth() - LARGURA - 12);
        entity.setX(novoX);
    }

    @Override
    public void onUpdate(double tpf) {
        if (!gestor.rondaAtiva()) {
            return;
        }
        atualizarOrientacao();
        if (tempoHitstop > 0) {
            tempoHitstop = Math.max(0, tempoHitstop - tpf);
            gestor.processarFrame(this);
            gestor.atualizarDepuracao();
            return;
        }
        atualizarStun(tpf);
        atualizarGuarda(tpf);
        atualizarSalto(tpf);
        atualizarFrame(tpf);
        atualizarBufferAtaque(tpf);
        resolverComandosMantidos();
        atualizarRecuo(tpf);
        atualizarMovimento(tpf);
        gestor.processarFrame(this);
        gestor.processarEmpurrao(this);
        gestor.atualizarDepuracao();
    }

    private void iniciarMovimento(int direcaoRelativa, AcaoCombate acaoMovimento) {
        movimentoRelativoAoOponente = true;
        movimentoRelativo = Integer.signum(direcaoRelativa);
        movimento = movimentoRelativo * orientacaoAtual;
        if (podeControlar() && noChao && !defesaManualPedida) {
            if (acao != acaoMovimento) {
                mudarAcao(acaoMovimento);
            }
        }
    }

    private void atualizarStun(double tpf) {
        if (tempoStun > 0) {
            tempoStun = Math.max(0, tempoStun - tpf);
            if (tempoStun == 0 && vida > 0 && acao == AcaoCombate.HIT) {
                mudarAcao(defesaManualPedida
                        ? (defesaBaixaPedida ? AcaoCombate.LOW_BLOCK : AcaoCombate.BLOCK)
                        : acaoParaMovimento());
            } else if (tempoStun == 0 && movimentoRelativoAoOponente) {
                movimento = movimentoRelativo * orientacaoAtual;
            }
        }
    }

    private void atualizarBufferAtaque(double tpf) {
        if (ataquePendente == null) {
            return;
        }
        tentarExecutarAtaquePendente();
        if (ataquePendente != null) {
            tempoBufferAtaque = Math.max(0, tempoBufferAtaque - tpf);
            if (tempoBufferAtaque == 0) {
                ataquePendente = null;
            }
        }
    }

    private void tentarExecutarAtaquePendente() {
        if (ataquePendente == null || !noChao || tempoHitstop > 0) {
            return;
        }
        boolean ataqueNormal = podeControlar();
        boolean cancelamento = podeCancelarPara(ataquePendente);
        if (ataqueNormal || cancelamento) {
            AcaoCombate ataque = ataquePendente;
            ataquePendente = null;
            tempoBufferAtaque = 0;
            mudarAcao(ataque);
        }
    }

    private boolean podeCancelarPara(AcaoCombate proximoAtaque) {
        return acao.eAtaque() && acertoRegistado
                && proximoAtaque.nivelForca() > acao.nivelForca();
    }

    private void resolverComandosMantidos() {
        if (!podeControlar() || !noChao) {
            return;
        }
        AcaoCombate pretendida;
        if (defesaManualPedida) {
            pretendida = defesaBaixaPedida ? AcaoCombate.LOW_BLOCK : AcaoCombate.BLOCK;
        } else {
            pretendida = acaoParaMovimento();
        }
        if (acao != pretendida) {
            mudarAcao(pretendida);
        }
    }

    private void atualizarRecuo(double tpf) {
        if (tempoRecuo <= 0) {
            velocidadeRecuo = 0;
            return;
        }
        empurrar(velocidadeRecuo * tpf);
        velocidadeRecuo *= Math.pow(0.08, tpf);
        tempoRecuo = Math.max(0, tempoRecuo - tpf);
    }

    private void atualizarOrientacao() {
        ComponenteLutador oponente = gestor.oponenteDe(this);
        if (oponente == null) {
            return;
        }
        double centro = entity.getX() + LARGURA / 2;
        double centroOponente = oponente.getEntity().getX() + LARGURA / 2;
        int novaOrientacao = centroOponente >= centro ? 1 : -1;
        if (novaOrientacao != orientacaoAtual) {
            orientacaoAtual = novaOrientacao;
            if (movimentoRelativoAoOponente) {
                movimento = movimentoRelativo * orientacaoAtual;
            } else {
                movimentoRelativo = movimento * orientacaoAtual;
            }
            vista.olharPara(orientacaoAtual);
            vista.mostrar(acao, indiceFrame);
            if (noChao && tempoStun == 0
                    && (acao == AcaoCombate.WALK_FORWARD || acao == AcaoCombate.WALK_BACKWARD)) {
                AcaoCombate novaAcao = acaoParaMovimento();
                if (novaAcao != acao) {
                    mudarAcao(novaAcao);
                }
            }
        }
    }

    private void atualizarGuarda(double tpf) {
        if (atrasoRecuperacaoGuarda > 0) {
            atrasoRecuperacaoGuarda = Math.max(0, atrasoRecuperacaoGuarda - tpf);
            return;
        }
        if (guarda < GUARDA_MAXIMA && tempoStun == 0 && !estaADefender()) {
            guarda = Math.min(GUARDA_MAXIMA, guarda + RECUPERACAO_GUARDA_POR_SEGUNDO * tpf);
            publicarGuarda();
        }
    }

    private void publicarGuarda() {
        int valor = guarda();
        if (valor != guardaPublicada) {
            guardaPublicada = valor;
            gestor.atualizarGuarda(this);
        }
    }

    private boolean estaADefender() {
        return acao == AcaoCombate.BLOCK || acao == AcaoCombate.LOW_BLOCK
                || acao == AcaoCombate.WALK_BACKWARD;
    }

    private void atualizarSalto(double tpf) {
        if (!noChao) {
            velocidadeY += GRAVIDADE * tpf;
            entity.translateY(velocidadeY * tpf);
            if (entity.getY() >= CHAO_Y) {
                entity.setY(CHAO_Y);
                velocidadeY = 0;
                noChao = true;
                movimentoSalto = 0;
                if (acao == AcaoCombate.JUMP) {
                    mudarAcao(acaoParaMovimento());
                }
            }
        }
    }

    private void atualizarMovimento(double tpf) {
        if (!noChao && movimentoSalto != 0) {
            empurrar(movimentoSalto * VELOCIDADE_SALTO_HORIZONTAL * tpf);
        } else if (movimento != 0 && podeControlar()
                && (acao == AcaoCombate.WALK_FORWARD || acao == AcaoCombate.WALK_BACKWARD)) {
            empurrar(movimento * VELOCIDADE * tpf);
        }
    }

    private void atualizarFrame(double tpf) {
        tempoNoFrame += tpf;
        double duracao = frameAtual().duracaoFrames() / 60.0;
        while (tempoNoFrame >= duracao) {
            tempoNoFrame -= duracao;
            avancarFrame();
            duracao = frameAtual().duracaoFrames() / 60.0;
        }
    }

    private void avancarFrame() {
        if (indiceFrame + 1 < animacaoAtual().frames().size()) {
            indiceFrame++;
            vista.mostrar(acao, indiceFrame);
        } else if (acao.repete()) {
            indiceFrame = 0;
            vista.mostrar(acao, indiceFrame);
        } else if (acao == AcaoCombate.JUMP) {
            indiceFrame = animacaoAtual().frames().size() - 1;
        } else if (acao != AcaoCombate.KNOCKOUT && acao != AcaoCombate.HIT) {
            mudarAcao(AcaoCombate.IDLE);
        }
    }

    private boolean podeControlar() {
        return gestor.rondaAtiva() && vida > 0 && tempoStun == 0 && tempoHitstop == 0
                && !acao.eAtaque()
                && acao != AcaoCombate.HIT && acao != AcaoCombate.KNOCKOUT;
    }

    private void mudarAcao(AcaoCombate novaAcao) {
        if (!animacoes.containsKey(novaAcao)) {
            throw new IllegalStateException("Faltam frames para " + novaAcao);
        }
        acao = novaAcao;
        indiceFrame = 0;
        tempoNoFrame = 0;
        acertoRegistado = false;
        String rotulo = gestor.rotuloAcao(novaAcao);
        vista.mostrar(novaAcao, indiceFrame);
        gestor.atualizarAcao(this, rotulo);
    }

    private AcaoCombate acaoParaMovimento() {
        if (movimentoRelativo == 0) {
            return AcaoCombate.IDLE;
        }
        return movimentoRelativo > 0
                ? AcaoCombate.WALK_FORWARD : AcaoCombate.WALK_BACKWARD;
    }

    private DefinicaoAnimacao animacaoAtual() {
        DefinicaoAnimacao animacao = animacoes.get(acao);
        if (animacao == null) {
            throw new IllegalStateException("Ação sem definição: " + acao);
        }
        return animacao;
    }

    private CaixaFrame transformar(CaixaFrame caixaFrame) {
        Caixa local = orientacaoAtual == 1
                ? caixaFrame.caixa()
                : caixaFrame.caixa().espelhada(LARGURA);
        return new CaixaFrame(caixaFrame.tipo(), caixaFrame.identificador(),
                local.noMundo(entity.getX(), entity.getY()), caixaFrame.dano(),
                caixaFrame.hitStun(), caixaFrame.blockStun());
    }
}
