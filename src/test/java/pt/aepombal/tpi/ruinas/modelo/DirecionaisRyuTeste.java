package pt.aepombal.tpi.ruinas.modelo;

import java.nio.file.Path;
import java.util.HashMap;
import pt.aepombal.tpi.ruinas.componentes.ComponenteLutador;

/** Regressões de comandos, frames, impactos e movimento, sem abrir a janela. */
public final class DirecionaisRyuTeste {
    public static void main(String[] args) throws Exception {
        var mapa = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
        AcaoCombate[] botoes = {AcaoCombate.MP, AcaoCombate.HP, AcaoCombate.HP, AcaoCombate.HK, AcaoCombate.HK};
        int[] direcoes = {1, 1, -1, -1, 1};
        AcaoCombate[] ataques = {AcaoCombate.COLLARBONE_BREAKER, AcaoCombate.SOLAR_PLEXUS,
                AcaoCombate.SHORT_UPPERCUT, AcaoCombate.AXE_KICK, AcaoCombate.WHIRLWIND_KICK};
        int[][] tempos = {{20,4,19,600}, {20,5,16,800}, {7,4,26,800}, {10,14,21,800}, {16,4,20,800}};
        for (int i = 0; i < ataques.length; i++) {
            var acao = ataques[i];
            for (int orientacao : new int[]{-1, 1}) {
                int mundo = direcoes[i] * orientacao;
                verificar(botoes[i].selecionarAtaque(false, mundo * orientacao) == acao, "Comando relativo");
                verificar(botoes[i].selecionarAtaque(true, mundo * orientacao) == botoes[i].agachado(),
                        "Baixo prevalece nas diagonais");
            }
            verificar(acao.aereo().eAtaqueAereo(), "No ar usa normal aéreo");
            var animacao = mapa.get(acao);
            int total = tempos[i][0] - 1 + tempos[i][1] + tempos[i][2];
            verificar(animacao.frames().size() == total, "Duração " + acao);
            var danos = new HashMap<String, Integer>();
            var lutador = new ComponenteLutador(LadoLutador.JOGADOR, null, mapa, null);
            var campo = ComponenteLutador.class.getDeclaredField("acao");
            campo.setAccessible(true);
            campo.set(lutador, acao);
            int impactos = 0;
            for (int f = 1; f <= total; f++) {
                boolean ativo = f >= tempos[i][0] && f < tempos[i][0] + tempos[i][1];
                if (acao == AcaoCombate.AXE_KICK && f >= 15 && f <= 19) ativo = false;
                var frame = animacao.frame(f - 1);
                verificar(frame.temHitbox() == ativo, acao + " hitbox f" + f);
                for (var caixa : frame.caixasDoTipo(TipoCaixa.HIT)) {
                    danos.put(caixa.identificador(), caixa.dano());
                    if (lutador.podeAcertar(caixa)) {
                        impactos++;
                        lutador.registarAcerto(caixa);
                    }
                }
            }
            verificar(danos.values().stream().mapToInt(Integer::intValue).sum() == tempos[i][3], "Dano total");
            verificar(impactos == (acao == AcaoCombate.AXE_KICK ? 2 : 1), "Sem dano repetido por frame");
        }
        verificar(AcaoCombate.MP.selecionarAtaque(false, -1) == AcaoCombate.MP, "Trás+MP não é anti-air");
        verificar(AcaoCombate.MP.selecionarAtaque(true, -1) == AcaoCombate.CROUCH_MP, "Baixo+trás+MP");
        verificar(!RegraDefesa.bloqueia(AcaoCombate.LOW_BLOCK, AcaoCombate.COLLARBONE_BREAKER,
                true, true, true, true), "Overhead não bloqueia agachado (aproximação documentada)");
        verificar(Math.abs(MovimentoRyu.velocidadeAndar(true) - 235) < 1e-9, "Avanço calibrado");
        verificar(Math.abs(MovimentoRyu.velocidadeAndar(false) - 160) < 1e-9, "Recuo mais lento");
        for (int fps : new int[]{30,60,144}) {
            var relogio = new RelogioCombate();
            int[] ticks = {0};
            for (int f = 0; f < fps; f++) relogio.atualizar(1.0 / fps, () -> ticks[0]++);
            verificar(ticks[0] == 60, "60 passos por segundo a " + fps + " FPS");
        }
        System.out.println("Ryu: cinco direcionais, multi-hit, prejump, movimento e relógio: OK");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
