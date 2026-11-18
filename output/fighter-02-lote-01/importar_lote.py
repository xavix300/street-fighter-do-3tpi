"""Importa as dez folhas independentes do lote 01. Requer Pillow, NumPy e OpenCV."""

import argparse
import hashlib
import json
from pathlib import Path
import sys
import zipfile

import numpy as np
from PIL import Image, ImageDraw

try:
    import cv2 as cv
except ImportError:
    sys.path.insert(0, str(Path.home() / ".local/share/fighter-image-tools"))
    import cv2 as cv


GRIDS = {
    "idle": (4, 3), "guard": (4, 3), "walk_forward": (4, 4),
    "walk_backward": (4, 4), "crouch": (4, 3), "block": (4, 3),
    "low_block": (4, 3), "jump": (6, 4), "jump_side": (6, 4), "lp": (5, 4),
}


def clean_cell(cell, background):
    pixels = np.array(cell.convert("RGBA"))
    rgb = pixels[:, :, :3].astype(np.float32)
    alpha = pixels[:, :, 3].copy()
    red = (rgb[:, :, 0] > 110) & (rgb[:, :, 0] > 1.8 * rgb[:, :, 1]) & (rgb[:, :, 0] > 1.8 * rgb[:, :, 2])
    yellow = (np.minimum(rgb[:, :, 0], rgb[:, :, 1]) > 125) \
        & (rgb[:, :, 2] < .45 * np.minimum(rgb[:, :, 0], rgb[:, :, 1]))
    alpha[red | yellow] = 0
    if background == "alpha_threshold":
        # Esta folha já distingue o corpo (~251) do fundo (~26–90), mas não o torna transparente.
        alpha[alpha < 128] = 0
    if background == "GrabCut":
        height, width = alpha.shape
        mask = np.full((height, width), cv.GC_PR_BGD, np.uint8)
        inside = np.zeros_like(alpha, dtype=bool)
        inside[1:height - 1, round(width * .09):round(width * .95)] = True
        mask[inside] = cv.GC_PR_FGD
        warm = (rgb[:, :, 0] - rgb[:, :, 1] > 10) & (rgb[:, :, 1] - rgb[:, :, 2] > 12)
        dark = rgb.max(axis=2) < 110
        mask[inside & (warm | dark)] = cv.GC_FGD
        mask[~inside] = cv.GC_BGD
        mask[rgb.min(axis=2) > 243] = cv.GC_BGD
        cv.setRNGSeed(12345)
        cv.grabCut(pixels[:, :, :3][:, :, ::-1].copy(), mask, None,
                   np.zeros((1, 65), np.float64), np.zeros((1, 65), np.float64),
                   4, cv.GC_INIT_WITH_MASK)
        alpha = np.where((mask == cv.GC_FGD) | (mask == cv.GC_PR_FGD), 255, 0).astype(np.uint8)
    count, labels, stats, _ = cv.connectedComponentsWithStats((alpha >= 64).astype(np.uint8), 8)
    if count < 2:
        raise ValueError("Não foi encontrada a personagem")
    largest = 1 + int(np.argmax(stats[1:, cv.CC_STAT_AREA]))
    if stats[largest, cv.CC_STAT_AREA] < 500:
        raise ValueError("Silhueta demasiado pequena")
    keep = cv.dilate((labels == largest).astype(np.uint8), np.ones((3, 3), np.uint8))
    alpha[keep == 0] = 0
    pixels[:, :, 3] = alpha
    return Image.fromarray(pixels)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    cv.setNumThreads(2)
    report = {"source": args.archive.name, "animations": {}}
    with zipfile.ZipFile(args.archive) as archive:
        manifest = {item["animation"]: item for item in json.loads(archive.read("manifest.json"))}
        for name, (columns, rows) in GRIDS.items():
            item = manifest[name]
            data = archive.read(f"fighter/{name}/{name}_sheet.png")
            if hashlib.sha256(data).hexdigest() != item["sha256"]:
                raise ValueError("Checksum inválido: " + name)
            with archive.open(f"fighter/{name}/{name}_sheet.png") as stream:
                sheet = Image.open(stream).convert("RGBA")
            background = "GrabCut" if name == "jump_side" else "alpha_threshold" if name == "walk_forward" else "alpha"
            cells = []
            for index in range(columns * rows):
                x, y = index % columns, index // columns
                box = (round(x * sheet.width / columns), round(y * sheet.height / rows),
                       round((x + 1) * sheet.width / columns), round((y + 1) * sheet.height / rows))
                cells.append(clean_cell(sheet.crop(box), background))
            bounds = [cell.getbbox() for cell in cells]
            root = (bounds[0][0] + bounds[0][2]) / 2 / cells[0].width
            scale = 400 / max(box[3] - box[1] for box in bounds)
            # A escala é comum à ação; baixar o corpo não aumenta o tamanho do sprite.
            folder = args.destination / name
            folder.mkdir(parents=True, exist_ok=True)
            hd_folder = Path(__file__).parent / "frames-hd" / name
            hd_folder.mkdir(parents=True, exist_ok=True)
            preview = Image.new("RGBA", (columns * 256, rows * 224), "#44566b")
            draw = ImageDraw.Draw(preview)
            for index, (cell, box) in enumerate(zip(cells, bounds), 1):
                pose = cell.crop(box)
                pose = pose.resize((round(pose.width * scale), round(pose.height * scale)), Image.Resampling.LANCZOS)
                canvas = Image.new("RGBA", (512, 448))
                px = round(256 + (box[0] - root * cell.width) * scale)
                py = 432 - pose.height
                if px < 0 or px + pose.width > 512:
                    raise ValueError("Membro fora da tela: " + name + "/" + str(index))
                canvas.alpha_composite(pose, (px, py))
                canvas.save(hd_folder / f"{name}_{index:03}.png")
                frame = canvas.resize((128, 112), Image.Resampling.LANCZOS)
                # Elimina apenas o extravasamento do filtro nas margens lógicas.
                frame.paste((0, 0, 0, 0), (0, 0, 128, 8))
                frame.paste((0, 0, 0, 0), (0, 108, 128, 112))
                frame.save(folder / f"{name}_{index:03}.png")
                x, y = (index - 1) % columns * 256, (index - 1) // columns * 224
                preview.alpha_composite(canvas.resize((256, 224), Image.Resampling.LANCZOS), (x, y))
                draw.text((x + 5, y + 5), str(index), fill="white")
            if name != "idle":
                # LP: a extensão/contacto está nas poses 9–10 desta folha, não nas primeiras poses.
                third = len(cells) // 3
                preparation, active, recovery = (8, 2, 10) if name == "lp" else (third, third, len(cells) - third * 2)
                (folder / "animacao.properties").write_text(
                    f"poses={len(cells)}\npreparacao={preparation}\nativos={active}\nrecuperacao={recovery}\n",
                    encoding="ascii")
            preview.save(Path(__file__).with_name(f"{name}_preview.png"))
            report["animations"][name] = {
                "poses": len(cells), "grid": [columns, rows], "source_size": list(sheet.size),
                "source_sha256": item["sha256"], "scale": scale, "root_ratio": root,
                "background_cleanup": background,
            }
            print(f"{name}: {len(cells)} poses, fundo {background}")
    Path(__file__).with_name("importacao.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
