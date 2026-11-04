package pt.aepombal.tpi.ruinas.modelo;

import java.nio.file.Path;

/** Trajetória, fases, comandos aéreos e regras de defesa usando os dados do jogo. */
public final class SaltoTeste {
    public static void main(String[] args) throws Exception {
        for (int direcao : new int[]{-1, 0, 1}) {
            var salto = new SaltoLutador();
            salto.iniciar(direcao);
            verificar(!salto.noChao() && salto.direcao() == direcao, "Direção inicial");
            verificar(salto.acao() == (direcao == 0 ? AcaoCombate.JUMP : AcaoCombate.JUMP_SIDE), "Tipo de salto");
            salto.atualizar(0.4);
            verificar(Math.abs(salto.altura() + 144) < 0.001, "Topo a 0,4 segundos");
            verificar(salto.indiceFrame(12) == 6, "Pose do topo");
            verificar(salto.consumirAtaque() && !salto.consumirAtaque(), "Só um ataque por salto");
            salto.atualizar(0.4);
            verificar(salto.noChao() && salto.altura() == 0 && salto.direcao() == 0, "Aterragem");
            verificar(!salto.consumirAtaque(), "Ataque aéreo proibido no chão");
            salto.iniciar(direcao);
            verificar(salto.consumirAtaque(), "Novo salto permite novo ataque");
        }
        for (int fps : new int[]{30, 60, 144}) {
            var salto = new SaltoLutador();
            salto.iniciar(1);
            int anterior = 0;
            for (int i = 0; i < fps; i++) {
                salto.atualizar(1.0 / fps);
                if (salto.noChao()) break;
                int frame = salto.indiceFrame(12);
                verificar(frame >= anterior && frame < 12, "Fases devem avançar com a trajetória");
                anterior = frame;
            }
            verificar(salto.noChao(), "Aterragem independente do FPS");
        }
        var animacoes = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
        for (var base : new AcaoCombate[]{AcaoCombate.LP, AcaoCombate.MP, AcaoCombate.HP,
                AcaoCombate.LK, AcaoCombate.MK, AcaoCombate.HK}) {
            var aereo = base.aereo();
            verificar(aereo.eAtaqueAereo() && aereo.nivelForca() == base.nivelForca(), "Variante aérea");
            var frames = animacoes.get(aereo).frames();
            verificar(frames.size() == 6, "Seis fases por golpe aéreo");
            for (int i = 0; i < 6; i++) {
                verificar(!frames.get(i).caixasDoTipo(TipoCaixa.HIT).isEmpty() == (i == 3), "Impacto só na fase 4");
            }
            verificar(RegraDefesa.bloqueia(AcaoCombate.WALK_BACKWARD, aereo, true, false, true, true), "Defesa alta");
            verificar(!RegraDefesa.bloqueia(AcaoCombate.LOW_BLOCK, aereo, true, true, true, true), "Defesa baixa não cobre ataques aéreos");
            verificar(!RegraDefesa.bloqueia(AcaoCombate.JUMP, aereo, true, false, false, true), "Sem defesa no ar");
        }
        verificar(AcaoCombate.ANTI_AIR_CROUCH_MP.aereo() == AcaoCombate.AIR_MP, "Não usar anti-air de chão no ar");
        System.out.println("Salto vertical/lateral, trajetória, ataques aéreos e defesa: OK");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
