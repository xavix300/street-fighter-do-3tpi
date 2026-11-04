package pt.aepombal.tpi.ruinas;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/** Leitura XInput opcional com PowerShell e a DLL incluída no Windows; sem dependências Maven. */
final class ComandoWindows {
    private final AtomicInteger botoes = new AtomicInteger();
    private Process processo;

    void iniciar() {
        if (processo != null && processo.isAlive()) return;
        if (!System.getProperty("os.name").startsWith("Windows")) return;
        try {
            processo = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive",
                    "-ExecutionPolicy", "Bypass", "-File", Path.of("comando-xinput.ps1").toAbsolutePath().toString())
                    .redirectError(ProcessBuilder.Redirect.DISCARD).start();
            Runtime.getRuntime().addShutdownHook(new Thread(this::fechar));
            Thread leitor = new Thread(() -> {
                try (BufferedReader entrada = new BufferedReader(new InputStreamReader(processo.getInputStream()))) {
                    String linha;
                    while ((linha = entrada.readLine()) != null) {
                        try { botoes.set(Integer.parseInt(linha.trim())); }
                        catch (NumberFormatException ignorada) { botoes.set(0); }
                    }
                } catch (java.io.IOException ignorada) {
                    // Sem comando, o teclado continua disponível.
                } finally { botoes.set(0); }
            }, "leitor-xinput");
            leitor.setDaemon(true);
            leitor.start();
        } catch (java.io.IOException ignorada) {
            botoes.set(0);
        }
    }

    int botoes() { return botoes.get(); }

    void fechar() {
        if (processo != null) processo.destroy();
        botoes.set(0);
    }
}
