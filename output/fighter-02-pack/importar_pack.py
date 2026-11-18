"""Importa previews de baixa resolução por autorização explícita do utilizador.

Não inventa os frames anunciados nos cabeçalhos: extrai só as poses existentes.
Requer Pillow. Não executa nem extrai ficheiros arbitrários do ZIP.
"""

import argparse
from collections import deque
import json
from pathlib import Path
from statistics import median
import zipfile

from PIL import Image, ImageDraw


def components(mask, width, height, minimum=60):
    seen = bytearray(width * height)
    result = []
    for start, value in enumerate(mask):
        if not value or seen[start]:
            continue
        queue = deque([start])
        seen[start] = 1
        group = []
        while queue:
            index = queue.popleft()
            group.append(index)
            x, y = index % width, index // width
            for nx in range(max(0, x - 1), min(width, x + 2)):
                for ny in range(max(0, y - 1), min(height, y + 2)):
                    other = ny * width + nx
                    if mask[other] and not seen[other]:
                        seen[other] = 1
                        queue.append(other)
        if len(group) >= minimum:
            result.append(group)
    return sorted(result, key=lambda group: min(i % width for i in group))


def read_preview(image, name):
    image = image.convert("RGB")
    width, height = image.size
    pixels = list(image.getdata())
    dark_rows = [y for y in range(height // 2)
                 if sum(max(pixels[y * width + x]) < 80 for x in range(width)) > width * 0.65]
    top = max(dark_rows) + 3
    image = image.crop((4, top, width - 4, height - 3))
    width, height = image.size
    pixels = list(image.getdata())
    mask = bytearray(int(min(p) < 190 or max(p) - min(p) > 22) for p in pixels)
    # Retira os contornos dos painéis e restos de outras linhas do overview.
    for y in range(height):
        if (y < 3 or y >= height - 3) and sum(mask[y * width:(y + 1) * width]) > width * 0.7:
            mask[y * width:(y + 1) * width] = bytes(width)
    for x in range(width):
        if (x < 3 or x >= width - 3) and sum(mask[y * width + x] for y in range(height)) > height * 0.8:
            for y in range(height):
                mask[y * width + x] = 0
    groups = components(mask, width, height, 10)
    clean = bytearray(width * height)
    for group in groups:
        xs, ys = [i % width for i in group], [i // width for i in group]
        if max(xs) - min(xs) >= 3 and max(ys) - min(ys) >= 2:
            for index in group:
                clean[index] = 1
    mask = clean
    if name in ("anti_air_mp", "anti_air_crouch_mp"):
        bodies = [group for group in components(mask, width, height)
                  if max(i // width for i in group) - min(i // width for i in group) > 20]
        floor = round(median(max(i // width for i in group) for group in bodies)) + 3
        for y in range(min(height, floor + 1), height):
            mask[y * width:(y + 1) * width] = bytes(width)
    dark = bytearray(int(max(p) < 85 and mask[i]) for i, p in enumerate(pixels))
    heads = []
    for group in components(dark, width, height, 10):
        xs, ys = [i % width for i in group], [i // width for i in group]
        if 3 <= max(xs) - min(xs) + 1 <= 12 and 2 <= max(ys) - min(ys) + 1 <= 12 \
                and min(ys) < height * 0.65:
            heads.append((round(sum(xs) / len(xs)), min(ys), len(group)))
    candidates = sorted(heads, key=lambda h: (h[1], -h[2]))
    chosen = []
    for x, y, size in candidates:
        if all(abs(x - other) >= 22 for other in chosen):
            chosen.append(x)
    return image, mask, sorted(chosen)


def cut_frames(image, mask, centers):
    width, height = image.size
    edges = [0] + [round((a + b) / 2) for a, b in zip(centers, centers[1:])] + [width]
    frames = []
    for left, right in zip(edges, edges[1:]):
        cell = image.crop((left, 0, right, height)).convert("RGBA")
        alpha = Image.frombytes("L", (width, height), bytes(v * 255 for v in mask)).crop((left, 0, right, height))
        # Preenche apenas buracos fechados dentro da roupa, não os espaços entre membros.
        values = bytearray(alpha.tobytes())
        cw, ch = cell.size
        outside = bytearray(cw * ch)
        queue = deque(i for i in range(cw * ch) if not values[i]
                      and (i % cw in (0, cw - 1) or i // cw in (0, ch - 1)))
        for i in queue:
            outside[i] = 1
        while queue:
            i = queue.popleft()
            x, y = i % cw, i // cw
            for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                if 0 <= nx < cw and 0 <= ny < ch:
                    j = ny * cw + nx
                    if not values[j] and not outside[j]:
                        outside[j] = 1
                        queue.append(j)
        for i in range(len(values)):
            if not outside[i]:
                values[i] = 255
        # Só conserva componentes grandes; remove restos de letras e de contornos.
        groups = components(values, cw, ch, 10)
        clean = bytearray(cw * ch)
        main = max(groups, key=len)
        mx = [i % cw for i in main]
        my = [i // cw for i in main]
        for group in groups:
            xs, ys = [i % cw for i in group], [i // cw for i in group]
            near_body = min(xs) <= max(mx) + 3 and max(xs) >= min(mx) - 3 \
                and min(ys) <= max(my) + 3 and max(ys) >= min(my) - 3
            if len(group) >= 25 and near_body and max(ys) - min(ys) >= 2 and max(xs) - min(xs) >= 1:
                for i in group:
                    clean[i] = 255
        cell.putalpha(Image.frombytes("L", cell.size, bytes(clean)))
        box = cell.getbbox()
        if box is None:
            raise ValueError("Pose vazia")
        frames.append(cell.crop(box))
    return frames


def cut_knockout(image, mask):
    # Não cortar as primeiras poses a meio só para seguir uma grelha imaginária.
    width, height = image.size
    frames = []
    for group in components(mask, width, height):
        xs, ys = [i % width for i in group], [i // width for i in group]
        box = min(xs), min(ys), max(xs) + 1, max(ys) + 1
        if box[2] - box[0] < 6 or box[3] - box[1] < 6:
            continue
        alpha = bytearray(width * height)
        for i in group:
            alpha[i] = 255
        pose = image.convert("RGBA")
        pose.putalpha(Image.frombytes("L", image.size, bytes(alpha)))
        if box[2] - box[0] > 80:
            # As últimas poses sobrepõem-se no preview: conserva apenas a final.
            box = (box[2] - 44, box[1], box[2], box[3])
        frames.append(pose.crop(box))
    return frames


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path)
    parser.add_argument("destination", type=Path)
    parser.add_argument("--inspect", action="store_true")
    args = parser.parse_args()
    with zipfile.ZipFile(args.archive) as archive:
        manifest = json.loads(archive.read("fighter/manifest.json"))
        actions = {}
        for item in manifest["animations"]:
            name = item["name"]
            if name == "idle":
                continue  # Conserva o idle existente, já aprovado e integrado.
            if not name.replace("_", "").isalnum():
                raise ValueError("Identificador de animação inválido")
            image = Image.open(archive.open("fighter/" + item["preview"]))
            body, mask, heads = read_preview(image, name)
            frames = cut_knockout(body, mask) if name == "knockout" else cut_frames(body, mask, heads)
            actions[name] = frames
            if args.inspect:
                print(name, len(frames), [frame.size for frame in frames])
        if args.inspect:
            return
        standing = median(actions[name][0].height for name in ("guard", "lp", "hp", "lk", "mk", "walk_forward"))
        report = {"source": args.archive.name, "quality": "LOW_RESOLUTION_PREVIEW_PLACEHOLDERS",
                  "missing": ["mp", "stance"], "animations": {}}
        previews = []
        for name, frames in actions.items():
            # Escala única dentro da ação; não estica agachamentos nem quedas.
            reference = standing if name.startswith(("crouch", "air_")) or name in ("low_block", "jump", "jump_side") \
                else frames[0].height
            scale = min(100 / reference, 100 / max(frame.height for frame in frames),
                        112 / max(frame.width for frame in frames))
            folder = args.destination / name
            folder.mkdir(parents=True, exist_ok=True)
            row = Image.new("RGBA", (12 * 128, 136), "#44566b")
            draw = ImageDraw.Draw(row)
            draw.text((5, 3), f"{name}: {len(frames)} poses reais (preview)", fill="white")
            normalized = []
            for index, frame in enumerate(frames, 1):
                pose = frame.resize((max(1, round(frame.width * scale)), max(1, round(frame.height * scale))),
                                    Image.Resampling.LANCZOS)
                canvas = Image.new("RGBA", (128, 112))
                canvas.alpha_composite(pose, ((128 - pose.width) // 2, 108 - pose.height))
                canvas.save(folder / f"{name}_{index:03}.png")
                normalized.append(canvas)
            for index in range(12):
                row.alpha_composite(normalized[round(index * (len(normalized) - 1) / 11)], (index * 128, 22))
            previews.append(row)
            preparation = max(1, len(frames) // 3)
            active = max(1, len(frames) // 3)
            recovery = len(frames) - preparation - active
            (folder / "animacao.properties").write_text(
                f"poses={len(frames)}\npreparacao={preparation}\nativos={active}\nrecuperacao={recovery}\n",
                encoding="ascii")
            report["animations"][name] = {"poses": len(frames), "reference_height": reference,
                                           "scale": scale, "source": "preview_only"}
        output = Path(__file__).parent
        overview = Image.new("RGBA", (1536, len(previews) * 136))
        for index, row in enumerate(previews):
            overview.alpha_composite(row, (0, index * 136))
        overview.save(output / "preview.png")
        (output / "importacao.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"Importados {len(actions)} movimentos, {sum(len(frames) for frames in actions.values())} poses reais.")


if __name__ == "__main__":
    main()
