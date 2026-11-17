package pt.aepombal.tpi.ruinas.logica;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.scene.Group;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pt.aepombal.tpi.ruinas.FabricaEntidades;
import pt.aepombal.tpi.ruinas.componentes.ComponenteLutador;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;
import pt.aepombal.tpi.ruinas.modelo.Caixa;
import pt.aepombal.tpi.ruinas.modelo.CaixaFrame;
import pt.aepombal.tpi.ruinas.modelo.DefinicaoAnimacao;
import pt.aepombal.tpi.ruinas.modelo.Dificuldade;
import pt.aepombal.tpi.ruinas.modelo.LadoLutador;
import pt.aepombal.tpi.ruinas.modelo.Pontuacao;
import pt.aepombal.tpi.ruinas.modelo.ProgressoCombate;
import pt.aepombal.tpi.ruinas.modelo.RecordesException;
import pt.aepombal.tpi.ruinas.modelo.RepositorioRecordes;
import pt.aepombal.tpi.ruinas.modelo.ResultadoImpacto;
import pt.aepombal.tpi.ruinas.modelo.TipoCaixa;
import pt.aepombal.tpi.ruinas.modelo.TipoAcerto;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.addUINode;
import static com.almasb.fxgl.dsl.FXGL.inc;
import static com.almasb.fxgl.dsl.FXGL.set;
import static com.almasb.fxgl.dsl.FXGL.showMessage;
import static com.almasb.fxgl.dsl.FXGL.spawn;

// [IA] Serviço que contém as regras de ronda e mantém a GameApplication livre de lógica de combate.
/** Coordena uma ronda, as hitboxes, a dificuldade e a persistência de recordes. */
public final class GestorCombate {

    private final Map<AcaoCombate, DefinicaoAnimacao> animacoes;
    private final Map<LadoLutador, ComponenteLutador> lutadores = new EnumMap<>(LadoLutador.class);
    private final RepositorioRecordes recordes;
    private final ProgressoCombate progresso;
    private final Group camadaDepuracao = new Group();
    private boolean rondaTerminada;
    private boolean mostrarCaixas;

    public GestorCombate(Map<AcaoCombate, DefinicaoAnimacao> animacoes, Path ficheiroRecordes,
                         ProgressoCombate progresso) {
        this.animacoes = animacoes;
        this.recordes = new RepositorioRecordes(ficheiroRecordes);
        this.progresso = progresso;
    }

    /** Cria o chão e os dois lutadores da primeira ronda. */
    public void criarMundo() {
        set("dificuldade", progresso.dificuldade().nome().toUpperCase());
        spawn("arena", 0, 0);
        criarLutador(LadoLutador.JOGADOR, 150);
        criarLutador(LadoLutador.OPONENTE, 680);
        camadaDepuracao.setMouseTransparent(true);
        addUINode(camadaDepuracao);
    }

    /** Regista o componente quando a fábrica constrói um lutador. */
    public void registar(LadoLutador lado, ComponenteLutador lutador) {
        lutadores.put(lado, lutador);
        set(lado.variavelVida(), lutador.vida());
        set(lado.variavelGuarda(), lutador.guarda());
        set(lado.variavelAcao(), rotuloAcao(lutador.acao()));
    }

    /** Move o jogador para a direita do ecrã, sem trocar os controlos. */
    public void moverJogadorParaDireita() {
        lutadorJogador().moverHorizontal(1);
    }

    /** Move o jogador para a esquerda do ecrã, sem trocar os controlos. */
    public void moverJogadorParaEsquerda() {
        lutadorJogador().moverHorizontal(-1);
    }

    /** Pára o movimento do jogador humano. */
    public void pararJogador() {
        lutadorJogador().parar();
    }

    /** Faz o jogador humano saltar, opcionalmente para a esquerda ou direita. */
    public void saltarJogador(int direcaoHorizontal) {
        lutadorJogador().saltar(direcaoHorizontal);
    }

    public void baixoJogador(boolean premido) {
        lutadorJogador().definirBaixo(premido);
    }

    /** Executa um dos seis ataques do jogador. */
    public void atacarJogador(AcaoCombate ataque) {
        lutadorJogador().atacar(ataque);
    }

    public void provocarJogador() {
        lutadorJogador().provocar();
    }

    /** Ativa ou oculta o modo de depuração das hitboxes, hurtboxes e pushboxes. */
    public void alternarDepuracao() {
        mostrarCaixas = !mostrarCaixas;
        atualizarDepuracao();
    }

    /** Atualiza a camada de caixas de depuração para o frame que está ativo. */
    public void atualizarDepuracao() {
        if (!mostrarCaixas) {
            camadaDepuracao.getChildren().clear();
            return;
        }
        camadaDepuracao.getChildren().setAll(criarRetangulosDepuracao());
    }

    /** Calcula a distância horizontal entre a IA e o jogador. */
    public double distanciaAteJogador(ComponenteLutador origem) {
        return Math.abs(origem.getEntity().getX() - lutadorJogador().getEntity().getX());
    }

    /** Aplica os dados de hitbox do frame atual se ainda não existiu contacto. */
    public void processarFrame(ComponenteLutador atacante) {
        if (rondaTerminada || !atacante.acao().eAtaque() || atacante.acertoRegistado()) {
            return;
        }
        ComponenteLutador alvo = lutadores.get(atacante.lado().adversario());
        if (alvo == null || alvo.vida() == 0) {
            return;
        }
        acertarSeSobrepoe(atacante, alvo);
    }

    /** Separa os corpos quando as pushboxes se sobrepõem. */
    public void processarEmpurrao(ComponenteLutador origem) {
        if (origem.lado() != LadoLutador.JOGADOR || rondaTerminada) {
            return;
        }
        ComponenteLutador alvo = lutadores.get(LadoLutador.OPONENTE);
        if (alvo != null && caixasSobrepoem(origem.caixasNoMundo(TipoCaixa.PUSH), alvo.caixasNoMundo(TipoCaixa.PUSH))) {
            separar(origem, alvo);
        }
    }

    /** Atualiza a variável usada pelo marcador de vida. */
    public void atualizarVida(ComponenteLutador lutador) {
        set(lutador.lado().variavelVida(), lutador.vida());
    }

    /** Atualiza a barra de resistência da guarda. */
    public void atualizarGuarda(ComponenteLutador lutador) {
        set(lutador.lado().variavelGuarda(), lutador.guarda());
    }

    /** Atualiza o indicador visual da ação atual de um lutador. */
    public void atualizarAcao(ComponenteLutador lutador, String rotulo) {
        set(lutador.lado().variavelAcao(), rotulo);
    }

    /** Traduz os estados internos para texto curto e legível no HUD. */
    public String rotuloAcao(AcaoCombate acao) {
        return switch (acao) {
            case IDLE, STANCE -> "EM GUARDA";
            case GUARD, BLOCK -> "BLOQUEIO ALTO";
            case LOW_BLOCK -> "BLOQUEIO BAIXO";
            case CROUCH -> "AGACHADO";
            case WALK_FORWARD -> "AVANÇA";
            case WALK_BACKWARD -> "RECUA";
            case JUMP -> "SALTO";
            case JUMP_SIDE -> "SALTO LATERAL";
            case AIR_LP -> "SOCO LEVE NO AR";
            case AIR_MP -> "SOCO MÉDIO NO AR";
            case AIR_HP -> "SOCO FORTE NO AR";
            case AIR_LK -> "PONTAPÉ LEVE NO AR";
            case AIR_MK -> "PONTAPÉ MÉDIO NO AR";
            case AIR_HK -> "PONTAPÉ FORTE NO AR";
            case LP -> "SOCO LEVE";
            case MP -> "SOCO MÉDIO";
            case HP -> "SOCO FORTE";
            case LK -> "PONTAPÉ LEVE";
            case MK -> "PONTAPÉ MÉDIO";
            case HK -> "PONTAPÉ FORTE";
            case CROUCH_LP -> "SOCO LEVE AGACHADO";
            case CROUCH_MP -> "SOCO MÉDIO AGACHADO";
            case CROUCH_HP -> "SOCO FORTE AGACHADO";
            case CROUCH_LK -> "PONTAPÉ LEVE AGACHADO";
            case CROUCH_MK -> "PONTAPÉ MÉDIO AGACHADO";
            case CROUCH_HK -> "PONTAPÉ FORTE AGACHADO";
            case ANTI_AIR_MP -> "ANTI-AIR EM PÉ";
            case ANTI_AIR_CROUCH_MP -> "ANTI-AIR AGACHADO";
            case HIT -> "ATINGIDO";
            case KNOCKOUT -> "K.O.";
            case TAUNT -> "PROVOCAÇÃO";
            case VICTORY, CELEBRATION -> "VITÓRIA";
        };
    }

    /** Termina a ronda, atualiza pontos e grava o top 10. */
    public void lutadorDerrotado(ComponenteLutador derrotado) {
        if (rondaTerminada) {
            return;
        }
        rondaTerminada = true;
        LadoLutador vencedor = derrotado.lado().adversario();
        if (vencedor == LadoLutador.JOGADOR) {
            inc("pontos", 100);
            progresso.registarVitoria();
        }
        String mensagem = "Venceu " + vencedor.name() + ". Pontos: " + comPontos();
        ComponenteLutador lutadorVencedor = lutadores.get(vencedor);
        lutadorVencedor.celebrarVitoria();
        double duracao = animacoes.get(AcaoCombate.VICTORY).frames().stream()
                .mapToInt(frame -> frame.duracaoFrames()).sum() / 60.0;
        PauseTransition espera = new PauseTransition(Duration.seconds(duracao + 0.5));
        espera.setOnFinished(evento -> mostrarFim(mensagem));
        espera.play();
    }

    /** Devolve o componente do adversário de um lutador. */
    public ComponenteLutador oponenteDe(ComponenteLutador lutador) {
        return lutadores.get(lutador.lado().adversario());
    }

    /** Devolve a dificuldade usada pela fábrica para construir a IA. */
    public Dificuldade dificuldade() {
        return progresso.dificuldade();
    }

    /** Indica se os lutadores ainda podem agir nesta ronda. */
    public boolean rondaAtiva() {
        return !rondaTerminada;
    }

    /** Devolve todas as definições de frames carregadas do ficheiro externo. */
    public Map<AcaoCombate, DefinicaoAnimacao> animacoes() {
        return animacoes;
    }

    private void criarLutador(LadoLutador lado, double x) {
        Entity entidade = spawn("lutador", new SpawnData(x, 340)
                .put("lado", lado)
                .put("gestor", this));
        entidade.setY(340);
    }

    private void acertarSeSobrepoe(ComponenteLutador atacante, ComponenteLutador alvo) {
        List<CaixaFrame> zonasVulneraveis = alvo.caixasNoMundo(TipoCaixa.HURT);
        atacante.caixasNoMundo(TipoCaixa.HIT).stream()
                .filter(golpe -> zonasVulneraveis.stream()
                        .anyMatch(zona -> golpe.caixa().interceta(zona.caixa())))
                .findFirst()
                .ifPresent(golpe -> resolverContacto(atacante, alvo, golpe));
    }

    /** Resolve apenas o primeiro contacto válido de cada ataque. */
    private void resolverContacto(ComponenteLutador atacante, ComponenteLutador alvo,
                                  CaixaFrame golpe) {
        boolean bloqueado = alvo.bloqueia(atacante.acao());
        TipoAcerto tipoAcerto = alvo.tipoAcertoRecebido(bloqueado);
        atacante.registarAcerto();
        ResultadoImpacto resultado = alvo.receberImpacto(golpe, bloqueado, tipoAcerto);
        ReacaoContacto reacao = ReacaoContacto.calcular(resultado.bloqueado(),
                atacante.getEntity().getX(), alvo.getEntity().getX());
        List.of(atacante, alvo).forEach(lutador -> lutador.aplicarHitstop(reacao.pausaFrames()));
        alvo.aplicarRecuo(reacao.velocidadeRecuo(), reacao.recuoFrames());

        mostrarFrameAdvantage(atacante, resultado);
        if (atacante.lado() == LadoLutador.JOGADOR) {
            inc("pontos", resultado.bloqueado() ? 5 : resultado.danoAplicado());
        }
        if (alvo.vida() == 0) {
            lutadorDerrotado(alvo);
        }
    }

    private boolean caixasSobrepoem(List<CaixaFrame> primeiras, List<CaixaFrame> segundas) {
        return primeiras.stream().anyMatch(primeira -> segundas.stream()
                .anyMatch(segunda -> primeira.caixa().interceta(segunda.caixa())));
    }

    private void separar(ComponenteLutador jogador, ComponenteLutador oponente) {
        double centroJogador = jogador.getEntity().getX() + ComponenteLutador.LARGURA / 2;
        double centroOponente = oponente.getEntity().getX() + ComponenteLutador.LARGURA / 2;
        double sentido = centroJogador < centroOponente ? -1 : 1;
        jogador.empurrar(2 * sentido);
        oponente.empurrar(-2 * sentido);
    }

    private void mostrarFrameAdvantage(ComponenteLutador atacante, ResultadoImpacto impacto) {
        int vantagem = impacto.stunFrames() - atacante.framesRecuperacaoRestantes();
        String sinal = vantagem >= 0 ? "+" : "";
        String origem = atacante.lado() == LadoLutador.JOGADOR ? "P1" : "CPU";
        String resultado = impacto.quebraGuarda()
                ? "GUARD BREAK"
                : impacto.bloqueado() ? "ON BLOCK" : "ON HIT";
        set("frameData", "%s  %s%d  %s".formatted(origem, sinal, vantagem, resultado));
        set("tipoAcerto", impacto.bloqueado() || impacto.quebraGuarda()
                || impacto.tipoAcerto() == TipoAcerto.NORMAL
                ? "" : origem + "  " + impacto.tipoAcerto().rotulo());
    }

    private ComponenteLutador lutadorJogador() {
        ComponenteLutador jogador = lutadores.get(LadoLutador.JOGADOR);
        if (jogador == null) {
            throw new IllegalStateException("O jogador ainda não foi criado.");
        }
        return jogador;
    }

    private int comPontos() {
        return com.almasb.fxgl.dsl.FXGL.geti("pontos");
    }

    private void mostrarFim(String mensagem) {
        try {
            List<Pontuacao> top = recordes.acrescentar(new Pontuacao("Jogador", comPontos(), LocalDate.now()));
            showMessage(mensagem + "\nTop 1: " + top.getFirst().nome(),
                    () -> getGameController().startNewGame());
        } catch (RecordesException e) {
            showMessage(mensagem + "\nNão foi possível gravar os recordes.",
                    () -> getGameController().startNewGame());
        }
    }

    private List<Rectangle> criarRetangulosDepuracao() {
        return lutadores.values().stream()
                .flatMap(lutador -> List.of(TipoCaixa.PUSH, TipoCaixa.HURT, TipoCaixa.HIT).stream()
                        .flatMap(tipo -> lutador.caixasNoMundo(tipo).stream())
                        .map(this::retanguloDepuracao))
                .toList();
    }

    private Rectangle retanguloDepuracao(CaixaFrame caixaFrame) {
        Caixa caixa = caixaFrame.caixa();
        Rectangle retangulo = new Rectangle(caixa.x(), caixa.y(), caixa.largura(), caixa.altura());
        retangulo.setFill(Color.TRANSPARENT);
        retangulo.setStroke(Color.web(caixaFrame.tipo().corHexadecimal()));
        retangulo.setStrokeWidth(2);
        return retangulo;
    }

}
