package pt.aepombal.tpi.ruinas.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Dados de Capcom (2026-11-17); geometria/fotografias adaptadas às do nosso lutador. */
public final class NormaisDirecionaisRyu {
    private NormaisDirecionaisRyu() { }

    public static void acrescentar(Map<AcaoCombate, DefinicaoAnimacao> animacoes) {
        for (var acao : AcaoCombate.values()) {
            if (!acao.eDirecional()) continue;
            var base = animacoes.get(acao.baseVisual());
            var corpo = base.frames().getFirst().caixas().stream()
                    .filter(c -> c.tipo() != TipoCaixa.HIT).toList();
            var impacto = base.frames().stream().filter(FrameCombate::temHitbox)
                    .findFirst().orElseThrow().caixasDoTipo(TipoCaixa.HIT);
            var tempos = TemposRyu.de(acao);
            int total = tempos.arranque() - 1 + tempos.ativos() + tempos.recuperacao();
            List<FrameCombate> frames = new ArrayList<>();
            for (int f = 1; f <= total; f++) {
                List<CaixaFrame> caixas = new ArrayList<>(corpo);
                boolean ativo = f >= tempos.arranque() && f < tempos.arranque() + tempos.ativos();
                if (acao == AcaoCombate.AXE_KICK && f >= 15 && f <= 19) ativo = false;
                if (ativo) {
                    int grupo = acao == AcaoCombate.AXE_KICK && f >= 20 ? 2 : 1;
                    int inicioGrupo = grupo == 2 ? 20 : tempos.arranque();
                    int restantes = total - inicioGrupo;
                    int dano = acao == AcaoCombate.COLLARBONE_BREAKER ? 600
                            : acao == AcaoCombate.AXE_KICK ? 400 : 800;
                    for (var caixa : impacto) {
                        caixas.add(new CaixaFrame(TipoCaixa.HIT, "golpe-" + grupo, caixa.caixa(), dano,
                                restantes + tempos.vantagemHit(), restantes + tempos.vantagemBlock()));
                    }
                }
                frames.add(new FrameCombate(1, caixas));
            }
            animacoes.put(acao, new DefinicaoAnimacao(acao, frames));
        }
    }
}
