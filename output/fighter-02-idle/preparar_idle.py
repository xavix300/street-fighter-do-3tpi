"""Prepara o idle do segundo lutador, sem o ativar no jogo. Requer Pillow."""

import argparse
from pathlib import Path
from PIL import Image, ImageDraw

# Nome diferente para não importar este próprio preparar_idle.py.
import importlib.util

helper_path = Path(__file__).resolve().parents[1] / "idle-remake" / "preparar_idle.py"
spec = importlib.util.spec_from_file_location("recorte_idle_original", helper_path)
helper = importlib.util.module_from_spec(spec)
spec.loader.exec_module(helper)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    sheet = Image.open(args.source).convert("RGBA")
    cells = []
    for index in range(12):
        column, row = index % 4, index // 4
        left, right = round(column * sheet.width / 4), round((column + 1) * sheet.width / 4)
        top, bottom = round(row * sheet.height / 3), round((row + 1) * sheet.height / 3)
        cell = sheet.crop((left + 2, top + 2, right - 2, bottom - 2))
        pixels = cell.load()
        for y in range(cell.height):
            for x in range(cell.width):
                r, g, b, a = pixels[x, y]
                red_artifact = r > 110 and r > 1.8 * g and r > 1.8 * b
                yellow_artifact = min(r, g) > 125 and b < 0.45 * min(r, g)
                if red_artifact or yellow_artifact:
                    pixels[x, y] = (r, g, b, 0)
        cells.append(helper.isolar_corpo(cell))
    bounds = [cell.getbbox() for cell in cells]
    scale = 400 / max(box[3] - box[1] for box in bounds)
    args.destination.mkdir(parents=True, exist_ok=True)
    hd_folder = Path(__file__).with_name("frames-hd")
    hd_folder.mkdir(exist_ok=True)
    preview = Image.new("RGBA", (1024, 672), "#44566b")
    draw = ImageDraw.Draw(preview)
    for index, (cell, box) in enumerate(zip(cells, bounds), 1):
        pose = cell.crop(box)
        pose = pose.resize((round(pose.width * scale), round(pose.height * scale)), Image.Resampling.LANCZOS)
        canvas = Image.new("RGBA", (512, 448))
        x = round(256 + (box[0] - cell.width / 2) * scale)
        canvas.alpha_composite(pose, (x, 432 - pose.height))
        canvas.save(hd_folder / f"idle_{index:03}.png")
        frame = canvas.resize((128, 112), Image.Resampling.LANCZOS)
        # O filtro pode produzir uma margem quase invisível abaixo do chão.
        frame.paste((0, 0, 0, 0), (0, 108, 128, 112))
        frame.save(args.destination / f"idle_{index:03}.png")
        px, py = (index - 1) % 4 * 256, (index - 1) // 4 * 224
        preview.alpha_composite(canvas.resize((256, 224), Image.Resampling.LANCZOS), (px, py))
        draw.text((px + 6, py + 6), str(index), fill="white")
        print(f"idle_{index:03}.png: {frame.getbbox()}")
    preview.save(Path(__file__).with_name("preview.png"))


if __name__ == "__main__":
    main()
