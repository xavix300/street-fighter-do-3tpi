package pt.aepombal.tpi.ruinas.modelo;

// [IA] Exceção própria para falhas reais de persistência da tabela de recordes.
/** Lançada quando os recordes não podem ser lidos ou gravados de forma segura. */
public class RecordesException extends Exception {

    public RecordesException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
