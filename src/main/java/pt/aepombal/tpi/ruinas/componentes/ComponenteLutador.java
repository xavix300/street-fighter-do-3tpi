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
import pt.aepombal.tpi.ruinas.modelo.RegraDefesa;
import pt.aepombal.tpi.ruinas.modelo.SaltoLutador;
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

    public static final double ESCALA = 1.5;
    public static final int VIDA_MAXIMA = 10_000;
    public static final double LARGURA = 64 * ESCALA;
    public static final double ALTURA = 108 * ESCALA;
    private static final double CHAO_Y = 502 - ALTURA;
    private static final int ESCALA_TEMPO_ATAQUE = 1;
    private static final double VELOCIDADE = 235;
    private static final double VELOCIDADE_SALTO_HORIZONTAL = 205;
    private static final double GUARDA_MAXIMA = 100;
    private static final double RECUPERACAO_GUARDA_POR_SEGUNDO = 18;
    private static final double ATRASO_RECUPERACAO_GUARDA = 1.25;
    private static final int STUN_QUEBRA_GUARDA = 54;
    private static final int STUN_BLOQUEIO_MINIMO = 14;
    private static final double JANELA_BUFFER_ATAQUE = 7.0 / 60.0;

    private final LadoLutador lado;
    private final GestorCombate gestor;
    private final Map<AcaoCombate, DefinicaoAnimacao> animacoes;
    private final VistaLutador vista;
    private int vida = VIDA_MAXIMA;
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
    private final SaltoLutador salto = new SaltoLutador();
    private int movimento;
    private int movimentoRelativo;
    private int orientacaoAtual;
    private boolean movimentoRelativoAoOponente;
    private boolean baixoPremido;
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
        resolverComandosMantidos();
    }

    /** Pára o movimento horizontal quando não está em ataque ou stun. */
    public void parar() {
        movimento = 0;
        movimentoRelativo = 0;
        resolverComandosMantidos();
    }

    /** Inicia um salto neutro ou diagonal se o lutador estiver no chão e livre para agir. */
    public void saltar(int direcaoHorizontal) {
        if (podeControlar() && salto.noChao()) {
            salto.iniciar(direcaoHorizontal);
            mudarAcao(salto.acao());
        }
    }

    /** Baixo só defende quando combinado com a direção oposta ao adversário. */
    public void definirBaixo(boolean premido) {
        baixoPremido = premido;
        resolverComandosMantidos();
    }

    public void provocar() {
        if (podeControlar() && salto.noChao()) mudarAcao(AcaoCombate.TAUNT);
    }

    public void celebrarVitoria() {
        tempoHitstop = 0;
        tempoStun = 0;
        mudarAcao(AcaoCombate.VICTORY);
    }

    /** Guarda o ataque durante sete frames e executa-o assim que ficar permitido. */
    public void atacar(AcaoCombate novoAtaque) {
        if (novoAtaque.eAtaque() && vida > 0) {
            if (!salto.noChao() && salto.ataqueUsado()) return;
            ataquePendente = salto.noChao()
                    ? novoAtaque.selecionarAtaque(baixoPremido, movimentoRelativo < 0)
                    : novoAtaque.aereo();
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
        return RegraDefesa.bloqueia(acao, ataque, movimentoRelativo < 0,
                baixoPremido, salto.noChao(), guarda > 0);
    }

    /** Aplica um impacto, usando stun de bloqueio quando a defesa foi correta. */
    public ResultadoImpacto receberImpacto(CaixaFrame hitbox, boolean bloqueado,
                                            double escalaDano) {
        if (bloqueado) {
            // Guarda continua em 0–100, independentemente da escala da vida/dano.
            double danoNaEscalaDaGuarda = hitbox.dano() * 100.0 / VIDA_MAXIMA;
            guarda = Math.max(0, guarda - Math.max(8, danoNaEscalaDaGuarda * 0.6));
            atrasoRecuperacaoGuarda = ATRASO_RECUPERACAO_GUARDA;
            publicarGuarda();

            if (guarda == 0) {
                tempoStun = STUN_QUEBRA_GUARDA / 60.0;
                atrasoRecuperacaoGuarda = 2.0;
                mudarAcao(AcaoCombate.HIT);
                return new ResultadoImpacto(false, true, STUN_QUEBRA_GUARDA, 0);
            }

            int blockStun = Math.max(STUN_BLOQUEIO_MINIMO, hitbox.blockStun());
            tempoStun = blockStun / 60.0;
            mudarAcao(acao == AcaoCombate.LOW_BLOCK ? AcaoCombate.LOW_BLOCK : AcaoCombate.BLOCK);
            return new ResultadoImpacto(true, false, blockStun, 0);
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
        double restantes = Math.max(0, duracaoAtualFrames() - tempoNoFrame * 60.0);
        for (int i = indiceFrame + 1; i < animacaoAtual().frames().size(); i++) {
            restantes += animacaoAtual().frames().get(i).duracaoFrames() * ESCALA_TEMPO_ATAQUE;
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
            if (acao == AcaoCombate.VICTORY) atualizarFrame(tpf);
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
        if (podeControlar() && salto.noChao()) {
            if (acao != acaoMovimento) {
                mudarAcao(acaoMovimento);
            }
        }
    }

    private void atualizarStun(double tpf) {
        if (tempoStun > 0) {
            tempoStun = Math.max(0, tempoStun - tpf);
            if (tempoStun == 0 && vida > 0 && acao == AcaoCombate.HIT) {
                mudarAcao(acaoParaMovimento());
            } else if (tempoStun == 0 && movimentoRelativoAoOponente) {
                movimento = movimentoRelativo * orientacaoAtual;
            }
            if (tempoStun == 0 && acao == AcaoCombate.LOW_BLOCK) atualizarVista();
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
        if (ataquePendente == null || tempoHitstop > 0) {
            return;
        }
        if (salto.noChao() && ataquePendente.eAtaqueAereo()) {
            ataquePendente = null;
            return;
        }
        if (!salto.noChao()) {
            if (salto.ataqueUsado()) return;
            ataquePendente = ataquePendente.aereo();
        }
        boolean ataqueNormal = podeControlar();
        boolean cancelamento = podeCancelarPara(ataquePendente);
        if (ataqueNormal || cancelamento) {
            if (!salto.noChao() && !salto.consumirAtaque()) return;
            AcaoCombate ataque = ataquePendente;
            ataquePendente = null;
            tempoBufferAtaque = 0;
            mudarAcao(ataque);
        }
    }

    private boolean podeCancelarPara(AcaoCombate proximoAtaque) {
        return salto.noChao() && acao.eAtaque() && acertoRegistado
                && proximoAtaque.nivelForca() > acao.nivelForca();
    }

    private void resolverComandosMantidos() {
        if (!podeControlar() || !salto.noChao()) {
            return;
        }
        AcaoCombate pretendida;
        if (baixoPremido && movimentoRelativo < 0) {
            pretendida = AcaoCombate.LOW_BLOCK;
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
            atualizarVista();
            if (salto.noChao() && tempoStun == 0
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
        if (!salto.noChao()) {
            salto.atualizar(tpf);
            entity.setY(CHAO_Y + salto.altura());
            if (salto.noChao()) {
                if (ataquePendente != null && ataquePendente.eAtaqueAereo()) ataquePendente = null;
                if (acao.eSalto() || acao.eAtaqueAereo()) {
                    mudarAcao(acaoParaMovimento());
                }
            }
        }
    }

    private void atualizarMovimento(double tpf) {
        if (!salto.noChao() && salto.direcao() != 0) {
            empurrar(salto.direcao() * VELOCIDADE_SALTO_HORIZONTAL * tpf);
        } else if (movimento != 0 && podeControlar()
                && (acao == AcaoCombate.WALK_FORWARD || acao == AcaoCombate.WALK_BACKWARD)) {
            empurrar(movimento * VELOCIDADE * tpf);
        }
    }

    private void atualizarFrame(double tpf) {
        if (acao.eSalto()) {
            indiceFrame = salto.indiceFrame(animacaoAtual().frames().size());
            atualizarVista();
            return;
        }
        tempoNoFrame += tpf;
        double duracao = duracaoAtualFrames() / 60.0;
        while (tempoNoFrame >= duracao) {
            tempoNoFrame -= duracao;
            avancarFrame();
            duracao = duracaoAtualFrames() / 60.0;
        }
    }

    private int duracaoAtualFrames() {
        return frameAtual().duracaoFrames() * (acao.eAtaque() ? ESCALA_TEMPO_ATAQUE : 1);
    }

    private void avancarFrame() {
        if (indiceFrame + 1 < animacaoAtual().frames().size()) {
            indiceFrame++;
            atualizarVista();
        } else if (acao.repete()) {
            indiceFrame = 0;
            atualizarVista();
        } else if (acao.eSalto()) {
            indiceFrame = animacaoAtual().frames().size() - 1;
        } else if (acao != AcaoCombate.KNOCKOUT && acao != AcaoCombate.HIT
                && acao != AcaoCombate.VICTORY) {
            mudarAcao(acaoParaMovimento());
        }
    }

    private boolean podeControlar() {
        return gestor.rondaAtiva() && vida > 0 && tempoStun == 0 && tempoHitstop == 0
                && !acao.eAtaque()
                && acao != AcaoCombate.HIT && acao != AcaoCombate.KNOCKOUT
                && acao != AcaoCombate.TAUNT;
    }

    private void mudarAcao(AcaoCombate novaAcao) {
        if (!animacoes.containsKey(novaAcao)) {
            throw new IllegalStateException("Faltam frames para " + novaAcao);
        }
        acao = novaAcao;
        indiceFrame = novaAcao.eSalto() ? salto.indiceFrame(animacoes.get(novaAcao).frames().size()) : 0;
        tempoNoFrame = 0;
        acertoRegistado = false;
        String rotulo = gestor.rotuloAcao(novaAcao);
        atualizarVista();
        gestor.atualizarAcao(this, rotulo);
    }

    private void atualizarVista() {
        vista.mostrar(acao, indiceFrame);
        vista.inclinarSalto(acao == AcaoCombate.JUMP_SIDE ? -8 * salto.direcao() : 0);
    }

    private AcaoCombate acaoParaMovimento() {
        if (!salto.noChao()) return salto.acao();
        if (baixoPremido) {
            return movimentoRelativo < 0 ? AcaoCombate.LOW_BLOCK : AcaoCombate.CROUCH;
        }
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
        Caixa original = caixaFrame.caixa();
        Caixa ampliada = new Caixa(original.x() * ESCALA, original.y() * ESCALA,
                original.largura() * ESCALA, original.altura() * ESCALA);
        Caixa local = orientacaoAtual == 1 ? ampliada : ampliada.espelhada(LARGURA);
        return new CaixaFrame(caixaFrame.tipo(), caixaFrame.identificador(),
                local.noMundo(entity.getX(), entity.getY()), caixaFrame.dano(),
                caixaFrame.hitStun(), caixaFrame.blockStun());
    }
}
