package pt.aepombal.tpi.ruinas.modelo;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import javax.imageio.ImageIO;

/** Verifica as 800 imagens e a sincronização com as hitboxes reais. */
public final class Ataques40Teste {
    public static void main(String[] args) throws Exception {
        var animacoes = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
        int ataques = 0;
        for (var acao : AcaoCombate.values()) {
            if (!acao.eAtaque()) continue;
            ataques++;
            var hashes = new HashSet<String>();
            for (int i = 1; i <= 40; i++) {
                try (var input = Ataques40Teste.class.getResourceAsStream(acao.caminhoAtaque40(i))) {
                    verificar(input != null, "Falta pose: " + acao + " / " + i);
                    var bytes = input.readAllBytes();
                    hashes.add(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
                    var imagem = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
                    verificar(imagem != null && imagem.getWidth() == 128 && imagem.getHeight() == 112,
                            "Tela inválida: " + acao + " / " + i);
                    verificar(imagem.getColorModel().hasAlpha(), "Falta canal alpha: " + acao);
                    int visiveis = 0, transparentes = 0;
                    for (int y = 0; y < 112; y++) for (int x = 0; x < 128; x++) {
                        int alpha = imagem.getRGB(x, y) >>> 24;
                        if (alpha > 128) visiveis++;
                        if (alpha == 0) transparentes++;
                        if (x == 0 || x == 127 || y == 0 || y == 111) {
                            verificar(alpha < 32, "Pose cortada na margem: " + acao + " / " + i);
                        }
                    }
                    verificar(visiveis > 150 && transparentes > 5000, "Recorte vazio ou sem fundo transparente: " + acao);
                }
            }
            verificar(hashes.size() == 40, "Frames repetidos: " + acao);
            var animacao = animacoes.get(acao);
            int anterior = -1;
            for (int i = 0; i < animacao.frames().size(); i++) {
                var frame = animacao.frame(i);
                for (int amostra = 0; amostra < 100; amostra++) {
                    int visual = animacao.indiceAtaque40(i, frame.duracaoFrames() * amostra / 100.0);
                    verificar(visual >= anterior && visual < 40, "Salto de fase: " + acao);
                    verificar(!frame.temHitbox() || visual >= 16 && visual <= 23,
                            "Impacto visual fora da hitbox: " + acao);
                    verificar(frame.temHitbox() || visual < 16 || visual > 23,
                            "Pose de impacto sem hitbox: " + acao);
                    anterior = visual;
                }
            }
            verificar(animacao.indiceAtaque40(0, 0) == 0 && anterior == 39, "Sequência incompleta: " + acao);
        }
        verificar(ataques == 20, "São esperados vinte ataques");
        System.out.println("20 ataques, 800 poses únicas, transparência e impacto sincronizado: OK");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
