package pt.aepombal.tpi.ruinas.modelo;

import java.nio.file.Path;

/** Confere cada frame lógico com a tabela oficial, sem depender dos PNGs ou de JavaFX. */
public final class TemposRyuTeste {
    public static void main(String[] args) throws Exception {
        var animacoes = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
        AcaoCombate[] acoes = {AcaoCombate.LP, AcaoCombate.LK, AcaoCombate.MP, AcaoCombate.MK,
                AcaoCombate.HP, AcaoCombate.HK, AcaoCombate.CROUCH_LP, AcaoCombate.CROUCH_LK,
                AcaoCombate.CROUCH_MP, AcaoCombate.CROUCH_MK, AcaoCombate.CROUCH_HP, AcaoCombate.CROUCH_HK,
                AcaoCombate.AIR_LP, AcaoCombate.AIR_LK, AcaoCombate.AIR_MP, AcaoCombate.AIR_MK,
                AcaoCombate.AIR_HP, AcaoCombate.AIR_HK, AcaoCombate.ANTI_AIR_MP, AcaoCombate.ANTI_AIR_CROUCH_MP};
        int[][] tabela = {{4,3,7}, {5,3,11}, {6,4,11}, {9,3,18}, {10,5,18}, {12,4,20},
                {4,2,9}, {5,2,10}, {6,4,13}, {8,3,19}, {9,6,22}, {9,3,23},
                {4,10,0}, {6,10,0}, {8,5,0}, {7,6,0}, {9,6,0}, {10,8,0}, {9,6,22}, {9,6,22}};
        for (int i = 0; i < acoes.length; i++) {
            var acao = acoes[i];
            var animacao = animacoes.get(acao);
            int tempo = 0, primeiro = -1, ultimo = -1, ativos = 0;
            int anteriorVisual = -1;
            for (int pose = 0; pose < animacao.frames().size(); pose++) {
                var frame = animacao.frame(pose);
                for (int f = 0; f < frame.duracaoFrames(); f++) {
                    int numero = tempo + f + 1;
                    boolean deveAcertar = numero >= tabela[i][0] && numero < tabela[i][0] + tabela[i][1];
                    verificar(frame.temHitbox() == deveAcertar, acao + " hitbox no frame " + numero);
                    int visual = animacao.indiceAtaque40(pose, f);
                    verificar(visual >= anteriorVisual && visual < 40, acao + " pose visual");
                    verificar(frame.temHitbox() == (visual >= 16 && visual <= 23), acao + " impacto visual");
                    anteriorVisual = visual;
                }
                if (frame.temHitbox()) {
                    if (primeiro < 0) primeiro = tempo;
                    ultimo = tempo + frame.duracaoFrames();
                    ativos += frame.duracaoFrames();
                }
                tempo += frame.duracaoFrames();
            }
            verificar(primeiro + 1 == tabela[i][0] && ativos == tabela[i][1], acao + " arranque/ativos");
            if (!acao.eAtaqueAereo()) {
                verificar(tempo - ultimo == tabela[i][2], acao + " recuperação");
                var dados = TemposRyu.de(acao);
                var hitbox = animacao.frames().stream().filter(FrameCombate::temHitbox)
                        .findFirst().orElseThrow().caixasDoTipo(TipoCaixa.HIT).getFirst();
                int restantes = tempo - primeiro - 1;
                verificar(hitbox.blockStun() - restantes == dados.vantagemBlock(), acao + " on block");
                if (acao != AcaoCombate.CROUCH_HK) {
                    verificar(hitbox.hitStun() - restantes == dados.vantagemHit(), acao + " on hit");
                }
            }
        }
        verificar(TemposRyu.de(AcaoCombate.MP).extraFalha() == 2, "MP whiff");
        verificar(TemposRyu.de(AcaoCombate.CROUCH_MP).extraFalha() == 2, "cr.MP whiff");
        System.out.println("Ryu SF6: 20 ataques, arranque, ativos, recuperação e vantagem: OK");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
