---
name: FXGL Fighting Game
description: Develop, debug, profile, or refactor this Java 21 and FXGL 21.1 fighting game, including combat frame data, ECS, input, collision boxes, and photographic animation.
---

# Project guide

Resolve project paths below from the repository root, not this skill directory.
Read the relevant implementation and tests before editing. Preserve Portuguese
class names and the existing separation between model, components, and combat logic.

## Architecture and sources of truth

- `pom.xml`: Java 21, FXGL 21.1 and JNA. Keep the game on Java 21.
- Java package root: `src/main/java/pt/aepombal/tpi/ruinas/`.
- `StreetFrameApp`: FXGL application lifecycle, input and game orchestration.
- `FabricaEntidades`: entity creation and component attachment.
- `componentes/ComponenteLutador`: fighter simulation, state and cancel rules.
- `componentes/VistaLutador`: photographic rendering; keep presentation separate
  from combat decisions.
- `logica/GestorCombate` and `logica/ReacaoContacto`: combat/contact resolution,
  hitstop and displacement.
- `modelo/RepositorioFrames`, `FrameCombate`, `CaixaFrame`, `Caixa`:
  loaded frame data and collision geometry.
- `dados/frames.csv`: source attack boxes and damage. Check the runtime resource
  loading path before changing data; do not assume editing only this CSV updates
  the packaged resource.
- `modelo/TemposRyu`: hardcoded startup, active, recovery and stun timing.
- `modelo/PlanoVisualAtaque`: visual-to-combat frame mapping.
- `modelo/NormaisDirecionaisRyu`: five SF6 directional normals, adapting existing boxes/photos.
- `modelo/MovimentoRyu`, `SaltoLutador`: spatial conversion, asymmetric walk, prejump and jump.
- `PERFIL_RYU.md`: current user-approved SF6 Ryu target, sources and explicit fidelity gaps.
- `AUDITORIA_SF6.md`: broader goals and known gaps, not proof of implementation.

FXGL entities own components. Keep state transitions in simulation components
and model classes, JavaFX node/image work in views, and orchestration in the
application/manager. Verify FXGL APIs against dependency version 21.1.

## Implemented behavior

- 25 attack definitions (20 legacy, including two compatibility anti-airs, plus five directional
  normals) with startup, active and recovery phases; hitstun, blockstun,
  counters/punishes, guard break, hitstop and knockback.
- Hitboxes, hurtboxes and pushboxes. Frame data can contain multiple boxes;
  preserve all boxes rather than reducing an attack to one rectangle. Verify
  facing mirroring, local/world conversion, and per-attack hit bookkeeping.
- Walking, facing, crouching, vertical/lateral jumps and aerial attacks.
- SF6 directional inputs: forward+MP/HP/HK and back+HP/HK; down takes priority.
  Back+MP no longer selects the legacy anti-air. No specials or supers.
- 4f prejump; neutral/diagonal airtime 39f/38f from secondary reference values.
  Forward/back walk 235/160 px/s; jump height/distance adapted to this arena.
- Axe Kick has two separately registered hit groups. Collarbone Breaker is
  currently simplified to one overhead hit; do not claim its conditional two-hit rule is exact.
- Standing/crouching guard rules in `RegraDefesa`; down-back currently displays
  `LOW_BLOCK` immediately. Treat this as existing behavior, not a new requirement.
- Seven-frame input buffer and limited light-attack cancels. Read actual cancel
  predicates before extending combos; distinguish hit-confirm, block and whiff.
- Keyboard plus Windows XInput through `ComandoWindows` already exist.
  Broader/cross-platform controller support remains future work.
- CPU behavior styles, HUD, scoring and score persistence.
- Photographic PNG animation: shared fighter imagery, LP's 40-pose set, and
  MP's separate 20-pose visual pipeline. Assets are not complete for all moves.

## Timing and planned work

Combat now uses one `RelogioCombate` at 60 Hz owned by `GestorCombate`.
`StreetFrameApp.onUpdate` feeds it; fighter components must not independently
advance simulation through their entity updates. Both fighters advance before
contact snapshots are applied, permitting simultaneous trades. Animation advances
after contact testing to preserve startup frame counts. CPU decision scheduling
still follows render updates, so full replay determinism is not guaranteed.

24 FPS visual animation is a target, not an implemented global scheduler.
When adding it, derive visual frames from elapsed simulation time with a separate
24 Hz presentation clock; 60/24 is 2.5, so do not use a fixed integer tick divisor.
Combat startup/active/recovery must remain 60 Hz regardless of displayed pose.
Define how hitstop freezes simulation and presentation explicitly.

General cancel tables, robust combo/juggle rules and damage scaling are planned.
Do not describe limited light cancels as a complete combo system. Future juggle
work must specify airborne state, gravity, launch/landing, repeat-hit limits,
scaling and reset conditions before implementation.

Video-based heavy attacks are planned: no runtime video combat system exists.
Future video playback must not drive hit detection or combat timing. Specify
preloading, synchronization, cancellation, fallback images and disposal.

## Debugging and performance

- Use LSP diagnostics, symbols and references when available; compile after
  API-affecting changes. Inspect all callers before refactoring timing contracts.
- For collision issues, inspect frame number, phase, facing, all box coordinates,
  attack identity and contact result. Treat any new overlay/frame-step tool as
  proposed until its implementation is confirmed.
- Exercise both facing directions, same-frame trades, buffered inputs, hitstop,
  crouch/standing guard and landing transitions.
- Cache decoded images and avoid image decoding, file IO or excessive allocation
  in update loops. Profile before optimizing; JavaFX scene updates belong on its
  application thread. Keep future media loading away from the combat loop.
- Use Context7 `/almasb/fxgl` for documentation. Results may cite FXGL 11;
  cross-check with FXGL 21.1 source/JARs and https://github.com/AlmasB/FXGL.

## Verification

On Windows use `mvn.cmd`, with Java 21:

```text
mvn.cmd -B -ntp test-compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
```

The programs under `src/test/java` are standalone `main` tests; Maven
test compilation alone does not execute them. Run with `java -ea -cp` using
`target/test-classes;target/classes;` plus `target/classpath.txt` contents.

On 2026-11-17 compilation and the functional standalone tests passed, including
directional/movement regressions and a real-engine `RyuIntegracaoTeste`.
The integration test opens FXGL briefly and exits its own process: run separately.
`AnimacoesTeste` fails because JUMP PNG resources are missing.
`Ataques40Teste` fails at MP because it expects the uniform 40-pose attack layout;
MP instead has a separately verified 20-PNG pipeline. Do not fabricate assets or
weaken tests to make tooling verification pass.
