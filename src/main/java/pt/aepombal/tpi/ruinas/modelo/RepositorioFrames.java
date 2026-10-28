package pt.aepombal.tpi.ruinas.modelo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// [IA] Leitura UTF-8 do ficheiro externo que define os dados de hitbox por frame.
/**
 * Lê os dados de combate de um CSV. Alterar este ficheiro muda as caixas,
 * dano e tempos sem recompilar o jogo.
 */
public final class RepositorioFrames {

    private static final int COLUNAS = 12;

    /** Carrega as animações definidas no caminho indicado. */
    public Map<AcaoCombate, DefinicaoAnimacao> carregar(Path ficheiro)
            throws DadosCombateException {
        try {
            List<String> linhas = Files.readAllLines(ficheiro, StandardCharsets.UTF_8);
            Map<AcaoCombate, TreeMap<Integer, ConstrutorFrame>> porAcao = new EnumMap<>(AcaoCombate.class);
            for (String linha : linhas) {
                lerLinha(linha, porAcao);
            }
            return criarDefinicoes(porAcao);
        } catch (IOException e) {
            throw new DadosCombateException("Não foi possível ler " + ficheiro, e);
        }
    }

    private void lerLinha(String linha, Map<AcaoCombate, TreeMap<Integer, ConstrutorFrame>> porAcao)
            throws DadosCombateException {
        if (linha.isBlank() || linha.strip().startsWith("#")) {
            return;
        }
        String[] dados = linha.split(";", -1);
        if (dados.length != COLUNAS) {
            throw new DadosCombateException("Linha de frames inválida: " + linha);
        }
        try {
            AcaoCombate acao = AcaoCombate.porCodigo(dados[0]);
            int indice = inteiro(dados[1]);
            int duracao = inteiro(dados[2]);
            CaixaFrame caixa = new CaixaFrame(TipoCaixa.valueOf(dados[3].trim()), dados[4].trim(),
                    caixa(dados, 5), inteiro(dados[9]), inteiro(dados[10]), inteiro(dados[11]));
            porAcao.computeIfAbsent(acao, chave -> new TreeMap<>())
                    .computeIfAbsent(indice, chave -> new ConstrutorFrame(duracao))
                    .adicionar(caixa);
        } catch (IllegalArgumentException e) {
            throw new DadosCombateException("Dados de frame inválidos: " + linha, e);
        }
    }

    private Map<AcaoCombate, DefinicaoAnimacao> criarDefinicoes(
            Map<AcaoCombate, TreeMap<Integer, ConstrutorFrame>> porAcao) {
        Map<AcaoCombate, DefinicaoAnimacao> resultado = new EnumMap<>(AcaoCombate.class);
        porAcao.forEach((acao, frames) -> resultado.put(acao,
                new DefinicaoAnimacao(acao, frames.values().stream().map(ConstrutorFrame::construir).toList())));
        return Map.copyOf(resultado);
    }

    private Caixa caixa(String[] dados, int inicio) {
        return new Caixa(inteiro(dados[inicio]), inteiro(dados[inicio + 1]),
                inteiro(dados[inicio + 2]), inteiro(dados[inicio + 3]));
    }

    private int inteiro(String texto) {
        return Integer.parseInt(texto.trim());
    }

    /** Acumula todas as caixas que pertencem ao mesmo frame visual. */
    private static final class ConstrutorFrame {
        private final int duracao;
        private final List<CaixaFrame> caixas = new ArrayList<>();

        private ConstrutorFrame(int duracao) {
            this.duracao = duracao;
        }

        private void adicionar(CaixaFrame caixa) {
            caixas.add(caixa);
        }

        private FrameCombate construir() {
            return new FrameCombate(duracao, caixas);
        }
    }
}
