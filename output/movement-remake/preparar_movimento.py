"""Importa as quatro folhas do utilizador. Requer Pillow; não modifica Downloads."""

import argparse
from pathlib import Path
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "idle-remake"))
from preparar_idle import isolar_corpo


SHEETS = (
    ("walk_forward", "walk_forward_contact_sheet.png", 8, 2),
    ("walk_backward", "walk_backward_contact_sheet.png", 4, 4),
    ("guard", "Folha de sprites_ guarda de boxe.png", 6, 2),
    ("crouch", "Folha de Sprites_ Crouch de Combate.png", 6, 2),
)


def prepare(source, columns, rows, clean_red=False):
    sheet = Image.open(source).convert("RGBA")
    cells = []
    for index in range(columns * rows):
        column, row = index % columns, index // columns
        left, right = round(column * sheet.width / columns), round((column + 1) * sheet.width / columns)
        top, bottom = round(row * sheet.height / rows), round((row + 1) * sheet.height / rows)
        cell = sheet.crop((left + 2, top + 2, right - 2, bottom - 2))
        if clean_red:
            pixels = cell.load()
            # Manchas saturadas junto às mãos; não apaga o logótipo no tronco.
            for y in range(round(cell.height * 0.48)):
                for x in range(round(cell.width * 0.55), cell.width):
                    r, g, b, a = pixels[x, y]
                    if r > 110 and r > 1.8 * g and r > 1.8 * b:
                        pixels[x, y] = (r, g, b, 0)
        cells.append(isolar_corpo(cell))
    bounds = [cell.getbbox() for cell in cells]
    scale = 100 / max(box[3] - box[1] for box in bounds)
    frames = []
    for cell, box in zip(cells, bounds):
        pose = cell.crop(box)
        pose = pose.resize((round(pose.width * scale), round(pose.height * scale)), Image.Resampling.LANCZOS)
        canvas = Image.new("RGBA", (128, 112))
        canvas.alpha_composite(pose, (round(64 + (box[0] - cell.width / 2) * scale), 108 - pose.height))
        frames.append(canvas)
    return frames


def save_frames(root, name, frames):
    folder = root / ("sequences/crouch" if name == "crouch" else name)
    folder.mkdir(parents=True, exist_ok=True)
    for index, frame in enumerate(frames, 1):
        number = f"{index:02}" if name == "crouch" else f"{index:03}"
        frame.save(folder / f"{name}_{number}.png")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("downloads", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    for name, filename, columns, rows in SHEETS:
        frames = prepare(args.downloads / filename, columns, rows, clean_red=name == "guard")
        preview = Image.new("RGBA", (columns * 128, rows * 112), "#44566b")
        draw = ImageDraw.Draw(preview)
        for index, frame in enumerate(frames):
            x, y = index % columns * 128, index // columns * 112
            preview.alpha_composite(frame, (x, y))
            draw.text((x + 3, y + 3), str(index + 1), fill="white")
        preview.save(Path(__file__).with_name(f"{name}_preview.png"))
        if name == "crouch":
            # As poses 1–6 são a descida, não um ciclo para repetir agachado.
            selected = frames[6:12]
        elif name == "guard":
            # Mantém a guarda levantada em vez de repetir a descida dos braços.
            selected = frames[3:8]
        else:
            selected = frames
        save_frames(args.destination, name, selected)
        if name == "guard":
            save_frames(args.destination, "block", selected)
        print(f"{name}: {len(frames)} poses preparadas, {len(selected)} no ciclo do jogo")


if __name__ == "__main__":
    main()
