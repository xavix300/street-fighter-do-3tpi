package pt.aepombal.tpi.ruinas;

import java.nio.file.Path;
import pt.aepombal.tpi.ruinas.componentes.ComponenteLutador;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;
import pt.aepombal.tpi.ruinas.modelo.FaseAtaque;
import pt.aepombal.tpi.ruinas.modelo.LadoLutador;
import pt.aepombal.tpi.ruinas.modelo.RepositorioFrames;
import pt.aepombal.tpi.ruinas.modelo.TipoAcerto;

/** Exercita a classificação no componente sem abrir uma janela JavaFX. */
public final class PunicoesLutadorTeste {
    public static void main(String[] args) throws Exception {
        var animacoes = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
        var lutador = new ComponenteLutador(LadoLutador.JOGADOR, null, animacoes, null);
        definir(lutador, "acao", AcaoCombate.MP);
        var mp = animacoes.get(AcaoCombate.MP);
        for (int i = 0; i < mp.frames().size(); i++) {
            definir(lutador, "indiceFrame", i);
            definir(lutador, "acertoRegistado", false);
            var esperado = mp.faseAtaque(i) == FaseAtaque.RECUPERACAO
                    ? TipoAcerto.WHIFF_PUNISH : TipoAcerto.COUNTER_HIT;
            verificar(lutador.tipoAcertoRecebido(false) == esperado, "Fase real do MP");
            verificar(lutador.tipoAcertoRecebido(true) == TipoAcerto.NORMAL, "Bloqueio sem bónus");
            if (mp.faseAtaque(i) == FaseAtaque.RECUPERACAO) {
                definir(lutador, "acertoRegistado", true);
                verificar(lutador.tipoAcertoRecebido(false) == TipoAcerto.PUNISH_COUNTER,
                        "Golpe que tocou no adversário não é whiff");
            }
        }
        definir(lutador, "acao", AcaoCombate.IDLE);
        definir(lutador, "indiceFrame", 0);
        definir(lutador, "movimentoRelativo", -1);
        verificar(lutador.bloqueia(AcaoCombate.MP), "Recuar livre permite defesa");
        definir(lutador, "framesRecuperacaoExtra", 2);
        definir(lutador, "recuperacaoExtraSemContacto", true);
        verificar(!lutador.bloqueia(AcaoCombate.MP), "Recuperação extra não pode defender");
        verificar(lutador.tipoAcertoRecebido(false) == TipoAcerto.WHIFF_PUNISH,
                "Whiff extra continua punível apesar da pose idle");
        definir(lutador, "recuperacaoExtraSemContacto", false);
        verificar(lutador.tipoAcertoRecebido(false) == TipoAcerto.PUNISH_COUNTER,
                "Aterragem após contacto não é whiff");
        definir(lutador, "acao", AcaoCombate.HIT);
        definir(lutador, "tempoStun", 0.2);
        verificar(lutador.tipoAcertoRecebido(false) == TipoAcerto.NORMAL,
                "Acertos durante hit-stun não repetem o counter");
        definir(lutador, "acao", AcaoCombate.IDLE);
        definir(lutador, "tempoStun", 0.0);
        definir(lutador, "framesRecuperacaoExtra", 0);
        verificar(lutador.tipoAcertoRecebido(false) == TipoAcerto.NORMAL, "Janela termina com recuperação");
        System.out.println("Componente: counters, whiff e defesa na recuperação extra: OK");
    }

    private static void definir(ComponenteLutador lutador, String nome, Object valor) throws Exception {
        var campo = ComponenteLutador.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(lutador, valor);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
