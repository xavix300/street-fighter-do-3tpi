package pt.aepombal.tpi.ruinas.modelo;

import java.nio.file.Path;
import java.util.List;

/** Verifica counters e limites de fase sem depender do motor gráfico. */
public final class TipoAcertoTeste {
    public static void main(String[] args) throws Exception {
        for (boolean semContacto : new boolean[]{false, true}) {
            for (FaseAtaque fase : FaseAtaque.values()) {
                verificar(TipoAcerto.classificar(fase, true, semContacto) == TipoAcerto.NORMAL,
                        "Bloqueio nunca recebe bónus");
            }
            verificar(TipoAcerto.classificar(FaseAtaque.NEUTRA, false, semContacto) == TipoAcerto.NORMAL,
                    "Neutro não é counter");
            for (FaseAtaque fase : new FaseAtaque[]{FaseAtaque.PREPARACAO, FaseAtaque.ATIVA}) {
                verificar(TipoAcerto.classificar(fase, false, semContacto) == TipoAcerto.COUNTER_HIT,
                        "Preparação/ativo é Counter Hit, não Whiff Punish");
            }
        }
        verificar(TipoAcerto.classificar(FaseAtaque.RECUPERACAO, false, true) == TipoAcerto.WHIFF_PUNISH,
                "Recuperação sem contacto é Whiff Punish");
        verificar(TipoAcerto.classificar(FaseAtaque.RECUPERACAO, false, false) == TipoAcerto.PUNISH_COUNTER,
                "Recuperação após contacto é Punish Counter");
        verificar(TipoAcerto.NORMAL.dano(600) == 600 && TipoAcerto.NORMAL.stun(16) == 16,
                "Acerto normal conserva dano e stun");
        verificar(TipoAcerto.COUNTER_HIT.dano(600) == 720 && TipoAcerto.COUNTER_HIT.stun(16) == 18,
                "Counter Hit: +20% dano e +2f");
        for (TipoAcerto tipo : new TipoAcerto[]{TipoAcerto.PUNISH_COUNTER, TipoAcerto.WHIFF_PUNISH}) {
            verificar(tipo.dano(600) == 720 && tipo.stun(16) == 20, "Punish: +20% dano e +4f");
        }
        var animacoes = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
        int ataques = 0;
        for (var animacao : animacoes.values()) {
            if (!animacao.acao().eAtaque()) {
                verificar(animacao.faseAtaque(0) == FaseAtaque.NEUTRA, "Estado fora de ataque");
                continue;
            }
            ataques++;
            var tempos = TemposRyu.de(animacao.acao());
            int decorrido = 0;
            for (int i = 0; i < animacao.frames().size(); i++) {
                var frame = animacao.frame(i);
                for (int f = 0; f < frame.duracaoFrames(); f++) {
                    int numero = decorrido + f + 1;
                    FaseAtaque esperada = numero < tempos.arranque() ? FaseAtaque.PREPARACAO
                            : numero < tempos.arranque() + tempos.ativos() ? FaseAtaque.ATIVA
                            : FaseAtaque.RECUPERACAO;
                    verificar(animacao.faseAtaque(i) == esperada,
                            animacao.acao() + " fase no frame " + numero);
                }
                decorrido += frame.duracaoFrames();
            }
        }
        // Uma pausa entre duas hitboxes não é recuperação punível.
        var vazio = new FrameCombate(1, List.of(new CaixaFrame(TipoCaixa.HURT, "corpo",
                new Caixa(0, 0, 10, 10), 0, 0, 0)));
        var golpe = new FrameCombate(1, List.of(new CaixaFrame(TipoCaixa.HIT, "teste",
                new Caixa(0, 0, 10, 10), 600, 16, 10)));
        var duplo = new DefinicaoAnimacao(AcaoCombate.MP, List.of(vazio, golpe, vazio, golpe, vazio));
        verificar(duplo.faseAtaque(2) == FaseAtaque.ATIVA, "Intervalo de ataque multi-hit");
        verificar(duplo.faseAtaque(4) == FaseAtaque.RECUPERACAO, "Fim do ataque multi-hit");
        verificar(ataques == 20, "Cobertura dos vinte ataques");
        System.out.println("Counters, dano, stun e fases dos 20 ataques: OK");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
