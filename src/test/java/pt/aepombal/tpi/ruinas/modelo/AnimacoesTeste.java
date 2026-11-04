package pt.aepombal.tpi.ruinas.modelo;

import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Verifica os dados reais e os PNGs que o jogo carrega, sem abrir uma janela. */
public final class AnimacoesTeste {
    public static void main(String[] args) throws Exception {
        var animacoes = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
        for (var acao : AcaoCombate.values()) {
            var animacao = animacoes.get(acao);
            verificar(animacao != null, "Falta ação: " + acao);
            for (int i = 0; i < animacao.frames().size(); i++) {
                try (var ficheiro = AnimacoesTeste.class.getResourceAsStream(acao.caminhoFrame(i + 1))) {
                    verificar(ficheiro != null, "Falta PNG: " + acao + " / " + (i + 1));
                    var imagem = ImageIO.read(ficheiro);
                    verificar(imagem != null && imagem.getWidth() == 128 && imagem.getHeight() == 112,
                            "Dimensões inválidas: " + acao);
                    verificar(imagem.getColorModel().hasAlpha(), "Falta transparência: " + acao);
                }
            }
        }
        for (var acao : AcaoCombate.values()) {
            if (!(acao == AcaoCombate.LP || acao == AcaoCombate.MP || acao == AcaoCombate.LK
                    || acao == AcaoCombate.MK || acao.name().startsWith("CROUCH_"))) continue;
            var frames = animacoes.get(acao).frames();
            verificar(frames.size() == 6, "São necessários seis frames: " + acao);
            for (int i = 0; i < frames.size(); i++) {
                verificar(!frames.get(i).caixasDoTipo(TipoCaixa.HIT).isEmpty() == (i == 3),
                        "Hitbox fora do impacto: " + acao + " / " + (i + 1));
            }
        }
        verificar(animacoes.get(AcaoCombate.HP).frames().size() == 14, "HP alterado");
        verificar(animacoes.get(AcaoCombate.HK).frames().size() == 12, "HK alterado");
        verificar(AcaoCombate.MP.selecionarAtaque(true, true) == AcaoCombate.ANTI_AIR_CROUCH_MP,
                "Baixo + trás + MP deve selecionar anti-air agachado");
        verificar(AcaoCombate.MP.selecionarAtaque(false, true) == AcaoCombate.ANTI_AIR_MP,
                "Trás + MP deve selecionar anti-air em pé");
        verificar(AcaoCombate.MP.selecionarAtaque(true, false) == AcaoCombate.CROUCH_MP,
                "Baixo + MP sem trás deve manter o golpe normal");
        verificar(AcaoCombate.MP.selecionarAtaque(false, false) == AcaoCombate.MP,
                "MP neutro deve manter o golpe normal");
        for (var anti : new AcaoCombate[]{AcaoCombate.ANTI_AIR_MP, AcaoCombate.ANTI_AIR_CROUCH_MP}) {
            var frames = animacoes.get(anti).frames();
            verificar(frames.size() == 6, "Anti-air precisa de seis fases");
            try (var extra = AnimacoesTeste.class.getResourceAsStream(anti.caminhoFrame(7))) {
                verificar(extra == null, "O carregamento de sprites deve terminar após seis fases");
            }
            var alvoNoAr = new Caixa(18, -55, 29, 70);
            for (int i = 0; i < frames.size(); i++) {
                verificar(!frames.get(i).caixasDoTipo(TipoCaixa.HIT).isEmpty() == (i == 3),
                        "Anti-air não pode acertar durante arranque/recuperação");
            }
            var hit = frames.get(3).caixasDoTipo(TipoCaixa.HIT).getFirst().caixa();
            verificar(hit.interceta(alvoNoAr), "Anti-air deve alcançar o adversário acima");
            verificar(hit.espelhada(64).interceta(alvoNoAr.espelhada(64)),
                    "Anti-air deve funcionar virado para a esquerda");
        }
        verificar(pt.aepombal.tpi.ruinas.componentes.ComponenteLutador.VIDA_MAXIMA == 10_000,
                "A vida máxima deve ser 10 000");
        // Danos base dos normais do Ryu (SF6), sem counter ou redução de combo.
        for (var acao : AcaoCombate.values()) {
            if (!acao.eAtaque()) continue;
            int danoEsperado = switch (acao) {
                case LP, LK, CROUCH_LP, AIR_LP, AIR_LK -> 300;
                case MP, CROUCH_MP, ANTI_AIR_MP, ANTI_AIR_CROUCH_MP -> 600;
                case MK, AIR_MP -> 700;
                case HP, CROUCH_HP, AIR_HP, AIR_HK -> 800;
                case HK, CROUCH_HK -> 900;
                case CROUCH_LK -> 200;
                case CROUCH_MK, AIR_MK -> 500;
                default -> throw new AssertionError("Ataque inválido: " + acao);
            };
            var hitboxes = animacoes.get(acao).frames().stream()
                    .flatMap(frame -> frame.caixasDoTipo(TipoCaixa.HIT).stream()).toList();
            verificar(!hitboxes.isEmpty(), "Ataque sem impacto: " + acao);
            for (var hitbox : hitboxes) {
                verificar(hitbox.dano() == danoEsperado,
                        "Dano excessivo ou inconsistente: " + acao + " / " + hitbox.dano());
            }
        }
        verificar(animacoes.get(AcaoCombate.LP).frames().stream().mapToInt(FrameCombate::duracaoFrames).sum() == 24,
                "Duração LP alterada");
        verificar(animacoes.get(AcaoCombate.MK).frames().stream().mapToInt(FrameCombate::duracaoFrames).sum() == 33,
                "Duração MK alterada");
        for (var base : new AcaoCombate[]{AcaoCombate.LP, AcaoCombate.MP, AcaoCombate.HP,
                AcaoCombate.LK, AcaoCombate.MK, AcaoCombate.HK}) {
            var baixo = base.agachado();
            verificar(baixo != base && baixo.eAtaque() && baixo.nivelForca() == base.nivelForca(),
                    "Variante agachada inválida: " + base);
            verificar(RegraDefesa.bloqueia(AcaoCombate.LOW_BLOCK, baixo, true, true, true, true),
                    "Guarda baixa deve defender " + baixo);
            verificar(!RegraDefesa.bloqueia(AcaoCombate.CROUCH, baixo, false, true, true, true),
                    "Agachar sem recuar não deve defender");
            if (baixo.exigeBloqueioBaixo()) {
                verificar(!RegraDefesa.bloqueia(AcaoCombate.BLOCK, baixo, true, false, true, true),
                        "Guarda alta não deve defender " + baixo);
            }
        }
        System.out.println("Animações, recursos, impactos e ataques agachados: OK");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
