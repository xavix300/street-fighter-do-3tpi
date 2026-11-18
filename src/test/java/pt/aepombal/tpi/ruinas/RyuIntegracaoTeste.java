package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import pt.aepombal.tpi.ruinas.componentes.*;
import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.*;
import java.nio.file.Path;
import java.util.Map;

/** Teste no motor real: abre uma janela breve, não grava recordes, termina com código de saída. */
public class RyuIntegracaoTeste extends GameApplication {
    protected void initSettings(GameSettings s) {
        s.setWidth(960); s.setHeight(560); s.setTitle("Ryu mechanics verification");
        s.setMainMenuEnabled(false); s.setCloseConfirmation(false);
    }
    protected void initGameVars(Map<String,Object> vars) {
        new StreetFrameApp().initGameVars(vars);
    }
    private record Par(GestorCombate gestor, ComponenteLutador p1, ComponenteLutador p2) {}
    private Par par(double x1, double x2) throws Exception {
        var dados = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
        var gestor = new GestorCombate(dados, Path.of("target", "unused-smoke-records.csv"), new ProgressoCombate());
        ComponenteLutador[] cs = new ComponenteLutador[2];
        for (int i = 0; i < 2; i++) {
            var lado = LadoLutador.values()[i];
            cs[i] = new ComponenteLutador(lado, gestor, dados, new VistaLutador(lado));
            FXGL.entityBuilder().at(i == 0 ? x1 : x2, ComponenteLutador.CHAO_Y).with(cs[i]).build();
            gestor.registar(lado, cs[i]);
        }
        return new Par(gestor, cs[0], cs[1]);
    }
    private void tick(Par p, int n) {
        for (int i = 0; i < n; i++) p.gestor.atualizar(1.0 / 60);
    }
    private void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
    }
    protected void initGame() {
        try {
            var p = par(300, 750);
            p.p1.moverParaFrente(); tick(p, 60);
            check(Math.abs(p.p1.getEntity().getX() - 535) < 1e-5, "Forward walk 235px/s");
            p = par(300,750);
            p.p1.moverParaTras(); tick(p,60);
            check(Math.abs(p.p1.getEntity().getX() - 140) < 1e-5, "Backward walk 160px/s");
            p = par(300,380);
            p.p1.atacar(AcaoCombate.LP); tick(p,3);
            check(p.p2.vida() == 10000, "LP must not hit before frame 4");
            tick(p,1); check(p.p2.vida() == 9700, "LP must hit frame 4");
            tick(p,60); check(p.p2.vida() == 9700, "Single hit must not repeat");
            p = par(300,380);
            p.p1.atacar(AcaoCombate.LP); p.p2.atacar(AcaoCombate.LP); tick(p,4);
            check(p.p1.vida() == 9640 && p.p2.vida() == 9640, "Same-frame counter trade");
            p = par(300,750);
            p.p1.saltar(1); tick(p,3);
            check(p.p1.getEntity().getY() + ComponenteLutador.ALTURA == 502,
                    "Prejump keeps feet on the arena floor at the new scale");
            tick(p,2); check(p.p1.getEntity().getY() < ComponenteLutador.CHAO_Y, "Airborne after prejump");
            tick(p,37);
            check(Math.abs(p.p1.getEntity().getY() + ComponenteLutador.ALTURA - 502) < 1e-6,
                    "Landing keeps feet on the arena floor at the new scale");
            check(Math.abs(p.p1.getEntity().getX() - (300 + MovimentoRyu.DISTANCIA_SALTO)) < 1e-6, "Jump distance");
            System.out.println("REAL FXGL PASS: walk, LP frame 4, single-hit, simultaneous trade, prejump and landing");
            System.exit(0);
        } catch (Throwable e) {
            e.printStackTrace(); System.exit(1);
        }
    }
    public static void main(String[] args) { launch(args); }
}
