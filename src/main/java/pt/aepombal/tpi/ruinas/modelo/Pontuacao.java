package pt.aepombal.tpi.ruinas.modelo;

import java.time.LocalDate;

// [IA] Record exigido para a tabela de recordes persistente.
/** Uma entrada imutável da tabela de recordes. */
public record Pontuacao(String nome, int pontos, LocalDate data) {

    public Pontuacao {
        if (nome == null || nome.isBlank() || pontos < 0 || data == null) {
            throw new IllegalArgumentException("Pontuação inválida.");
        }
    }
}
