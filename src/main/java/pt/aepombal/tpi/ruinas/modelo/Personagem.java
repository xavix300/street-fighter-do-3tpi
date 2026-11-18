package pt.aepombal.tpi.ruinas.modelo;

/** Identidade visual independente do lado e das regras de combate. */
public enum Personagem {
    LUTADOR_01("Lutador 01", "/assets/textures/fighter/idle/", false),
    LUTADOR_02("Lutador 02", "/assets/textures/fighters/fighter_02/idle/", true);

    private final String nome;
    private final String pastaIdle;
    private final boolean animacoesProvisorias;

    Personagem(String nome, String pastaIdle, boolean animacoesProvisorias) {
        this.nome = nome;
        this.pastaIdle = pastaIdle;
        this.animacoesProvisorias = animacoesProvisorias;
    }

    public String nome() { return nome; }
    public boolean animacoesProvisorias() { return animacoesProvisorias; }
    public String caminhoIdle(int indice) {
        if (indice < 1 || indice > 12) throw new IllegalArgumentException("Pose fora de 1–12");
        return pastaIdle + "idle_%03d.png".formatted(indice);
    }
}
