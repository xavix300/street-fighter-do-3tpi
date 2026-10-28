package pt.aepombal.tpi.ruinas.modelo;

import pt.aepombal.tpi.ruinas.componentes.ComportamentoOponente;
import pt.aepombal.tpi.ruinas.componentes.OponenteAgressivo;
import pt.aepombal.tpi.ruinas.componentes.OponenteDefensivo;
import pt.aepombal.tpi.ruinas.componentes.OponenteEquilibrado;
import pt.aepombal.tpi.ruinas.logica.GestorCombate;

import java.util.function.Function;

// [IA] Enum com comportamento configurável, sem if/switch por tipo de adversário.
/** Define os três estilos de IA usados polimorficamente pelo oponente. */
public enum Dificuldade {
    EQUILIBRADO("Equilibrado", OponenteEquilibrado::new),
    AGRESSIVO("Agressivo", OponenteAgressivo::new),
    DEFENSIVO("Defensivo", OponenteDefensivo::new);

    private final String nome;
    private final Function<GestorCombate, ComportamentoOponente> criador;

    Dificuldade(String nome, Function<GestorCombate, ComportamentoOponente> criador) {
        this.nome = nome;
        this.criador = criador;
    }

    /** Cria o componente de IA associado a esta dificuldade. */
    public ComportamentoOponente criarComportamento(GestorCombate gestor) {
        return criador.apply(gestor);
    }

    /** Nome legível para mostrar na interface. */
    public String nome() {
        return nome;
    }

    /** Aumenta a dificuldade após uma vitória do jogador. */
    public Dificuldade seguinte() {
        int seguinte = Math.min(ordinal() + 1, values().length - 1);
        return values()[seguinte];
    }
}
