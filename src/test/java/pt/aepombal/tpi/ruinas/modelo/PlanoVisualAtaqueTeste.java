package pt.aepombal.tpi.ruinas.modelo;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.imageio.ImageIO;

/** Confere recursos e uma pose por frame do MP, sem modificar as caixas lógicas. */
public final class PlanoVisualAtaqueTeste {
    public static void main(String[] args) throws Exception {
        String pasta = "/assets/textures/fighter/attacks/mp/";
        Properties propriedades = new Properties();
        try (var input = PlanoVisualAtaqueTeste.class.getResourceAsStream(pasta + "animacao.properties")) {
            verificar(input != null, "Falta plano visual");
            propriedades.load(input);
        }
        var plano = new PlanoVisualAtaque(
                Integer.parseInt(propriedades.getProperty("preparacao")),
                Integer.parseInt(propriedades.getProperty("ativos")),
                Integer.parseInt(propriedades.getProperty("recuperacao")));
        verificar(plano.quantidade() == 20, "MP tem 20 poses, não exige 40");
        var hashes = new HashSet<String>();
        for (int i = 1; i <= plano.quantidade(); i++) {
            try (var input = PlanoVisualAtaqueTeste.class.getResourceAsStream(
                    pasta + "mp_" + String.format("%02d", i) + ".png")) {
                verificar(input != null, "Falta pose " + i);
                byte[] bytes = input.readAllBytes();
                hashes.add(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
                var imagem = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
                verificar(imagem != null && imagem.getWidth() == 128 && imagem.getHeight() == 112,
                        "Dimensões da pose " + i);
                verificar(imagem.getColorModel().hasAlpha(), "Transparência da pose " + i);
                int visiveis = 0, base = 0;
                for (int y = 0; y < 112; y++) for (int x = 0; x < 128; x++) {
                    int alpha = imagem.getRGB(x, y) >>> 24;
                    if (alpha > 64) {
                        visiveis++;
                        base = Math.max(base, y);
                    }
                    if (x == 0 || x == 127 || y == 0 || y == 111) {
                        verificar(alpha < 32, "Margem cortada na pose " + i);
                    }
                }
                verificar(visiveis > 150, "Pose vazia " + i);
                verificar(base >= 105 && base <= 107, "Linha de chão da pose " + i + ": " + base);
            }
        }
        verificar(hashes.size() == 20, "As 20 poses são distintas");
        var animacao = new RepositorioFrames().carregar(Path.of("dados", "frames.csv")).get(AcaoCombate.MP);
        int tick = 0;
        var originais = java.util.List.copyOf(animacao.frames());
        for (int i = 0; i < animacao.frames().size(); i++) {
            var frame = animacao.frame(i);
            for (int f = 0; f < frame.duracaoFrames(); f++) {
                verificar(plano.indice(animacao, i, f) == tick, "Pose por tick " + tick);
                for (double fracao : new double[]{0, .25, .75, .999}) {
                    int pose = plano.indice(animacao, i, f + fracao);
                    verificar(pose == tick, "Pose estável dentro do tick");
                    verificar(frame.temHitbox() == (pose >= 5 && pose <= 8), "Impacto sincronizado");
                }
                tick++;
            }
        }
        verificar(tick == 20 && animacao.frames().equals(originais), "Duração e hitboxes preservadas");
        // A mesma infraestrutura aceita quantidades diferentes, sem esticar o ataque.
        var curto = new PlanoVisualAtaque(2, 1, 3);
        verificar(curto.indice(animacao, 0, 0) == 0, "Plano alternativo começa na primeira pose");
        try {
            new PlanoVisualAtaque(0, 4, 11);
            throw new AssertionError("Plano inválido aceite");
        } catch (IllegalArgumentException esperado) { }
        System.out.println("MP: 20 PNGs únicos, margens, chão, timeline e hitboxes: OK");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
