package pt.aepombal.tpi.ruinas.modelo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.Properties;

/** Só as cinco folhas aprovadas podem substituir recursos do jogo. */
public final class LoteSegundoSpritesTeste {
    private static final String ROOT = "/assets/textures/fighters/fighter_02/";

    public static void main(String[] args) throws Exception {
        var approved = Map.of("hp", 18, "lk", 10, "mk", 20, "crouch_lp", 20, "crouch_lk", 16);
        int total = 0;
        for (var entry : approved.entrySet()) {
            Properties data = new Properties();
            try (var stream = LoteSegundoSpritesTeste.class.getResourceAsStream(ROOT + entry.getKey() + "/animacao.properties")) {
                check(stream != null, "Falta configuração");
                data.load(stream);
            }
            check(Integer.parseInt(data.getProperty("poses")) == entry.getValue(), "Contagem incorreta");
            int phases = Integer.parseInt(data.getProperty("preparacao"))
                    + Integer.parseInt(data.getProperty("ativos"))
                    + Integer.parseInt(data.getProperty("recuperacao"));
            check(phases == entry.getValue(), "Fases não correspondem aos PNGs");
            for (int i = 1; i <= entry.getValue(); i++) {
                check(LoteSegundoSpritesTeste.class.getResource(
                        ROOT + entry.getKey() + "/" + entry.getKey() + "_%03d.png".formatted(i)) != null,
                        "Falta pose aprovada");
                total++;
            }
            check(LoteSegundoSpritesTeste.class.getResource(ROOT + entry.getKey() + "/"
                    + entry.getKey() + "_%03d.png".formatted(entry.getValue() + 1)) == null,
                    "Ficou uma pose antiga excedente");
        }
        var protectedHashes = Map.of(
                "hk", "61c40e1c7f227a10e9f674bc52a3e637df72aea04b9637eb9b16ac1d2a11c53c",
                "crouch_mp", "84b5523d542d04a64361e6d3da57d9fde819617e5d9b48921089174fc9d210fc",
                "crouch_hp", "6d496f3bb048abb4d04debb8b178b55307516d2c31e4b513d55f8bd1b519d698",
                "crouch_mk", "138757fecf3cf85f811ea17cd285844b344fbde1927803340d0e1f937d6883f1",
                "crouch_hk", "3348d5ede3784ed09c68ba3165c3e2b4435d8a830f8cf4d7c5abe57e10a3f6bd");
        for (var entry : protectedHashes.entrySet()) {
            var digest = MessageDigest.getInstance("SHA-256");
            update(digest, entry.getKey(), "animacao.properties");
            for (int i = 1; i <= 12; i++) {
                update(digest, entry.getKey(), entry.getKey() + "_%03d.png".formatted(i));
            }
            check(HexFormat.of().formatHex(digest.digest()).equals(entry.getValue()),
                    "Uma animação excluída foi alterada: " + entry.getKey());
        }
        check(total == 84, "Devem existir 84 poses aprovadas");
        System.out.println("Lote 02: cinco animações, 84 poses; cinco excluídas intactas: OK");
    }

    private static void update(MessageDigest digest, String folder, String file) throws Exception {
        digest.update((file + "\0").getBytes(StandardCharsets.UTF_8));
        try (var stream = LoteSegundoSpritesTeste.class.getResourceAsStream(ROOT + folder + "/" + file)) {
            check(stream != null, "Falta recurso protegido: " + folder + "/" + file);
            digest.update(stream.readAllBytes());
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
