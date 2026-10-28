package pt.aepombal.tpi.ruinas.modelo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// [IA] Persistência UTF-8 e atómica do top 10 de recordes.
/** Guarda e lê a tabela de recordes do jogo em UTF-8. */
public final class RepositorioRecordes {

    private static final int LIMITE = 10;
    private final Path ficheiro;

    public RepositorioRecordes(Path ficheiro) {
        this.ficheiro = ficheiro;
    }

    /** Lê os recordes existentes; a primeira execução devolve uma lista vazia. */
    public List<Pontuacao> carregar() throws RecordesException {
        if (!Files.exists(ficheiro)) {
            return new ArrayList<>();
        }
        try {
            List<Pontuacao> resultado = new ArrayList<>();
            for (String linha : Files.readAllLines(ficheiro, StandardCharsets.UTF_8)) {
                if (!linha.isBlank()) {
                    resultado.add(ler(linha));
                }
            }
            return ordenar(resultado);
        } catch (IOException | IllegalArgumentException e) {
            throw new RecordesException("O ficheiro de recordes está inválido.", e);
        }
    }

    /** Acrescenta uma pontuação, mantém o top 10 e grava-o de forma atómica. */
    public List<Pontuacao> acrescentar(Pontuacao nova) throws RecordesException {
        List<Pontuacao> recordes = new ArrayList<>(carregar());
        recordes.add(nova);
        List<Pontuacao> top = ordenar(recordes);
        gravar(top);
        return top;
    }

    private Pontuacao ler(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length != 3) {
            throw new IllegalArgumentException("Linha de recordes inválida.");
        }
        return new Pontuacao(campos[0], Integer.parseInt(campos[1]), LocalDate.parse(campos[2]));
    }

    private List<Pontuacao> ordenar(List<Pontuacao> recordes) {
        return recordes.stream()
                .sorted(Comparator.comparingInt(Pontuacao::pontos).reversed())
                .limit(LIMITE)
                .toList();
    }

    private void gravar(List<Pontuacao> recordes) throws RecordesException {
        try {
            Files.createDirectories(ficheiro.getParent());
            List<String> linhas = recordes.stream()
                    .map(p -> p.nome() + ";" + p.pontos() + ";" + p.data())
                    .toList();
            Path temporario = ficheiro.resolveSibling("recordes.tmp");
            Files.write(temporario, linhas, StandardCharsets.UTF_8);
            Files.move(temporario, ficheiro, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RecordesException("Não foi possível gravar os recordes.", e);
        }
    }
}
