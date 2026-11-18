"""Importa apenas as cinco folhas aprovadas do lote 02, preservando os outros recursos."""

import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import zipfile

from PIL import Image, ImageDraw

helper_path = Path(__file__).resolve().parents[1] / "fighter-02-lote-01/importar_lote.py"
spec = importlib.util.spec_from_file_location("lote_01", helper_path)
helper = importlib.util.module_from_spec(spec)
spec.loader.exec_module(helper)

# Grelhas contadas nas folhas reais, não os números pretendidos no pack antigo.
# As fases são apenas o alinhamento visual aproximado das poses com o relógio existente.
APPROVED = {
    "hp": ((9, 2), (6, 4, 8)),
    "lk": ((5, 2), (4, 2, 4)),
    "mk": ((5, 4), (7, 3, 10)),
    "crouch_lp": ((10, 2), (7, 4, 9)),
    "crouch_lk": ((8, 2), (6, 3, 7)),
}
EXCLUDED = ("hk", "crouch_mp", "crouch_hp", "crouch_mk", "crouch_hk")


def row_cuts(row, columns):
    # As poses não ocupam células de largura uniforme, apesar da dimensão do PNG.
    pixels = helper.np.array(row)
    dark = (pixels[:, :, :3].max(axis=2) < 90) & (pixels[:, :, 3] > 200)
    dark[round(row.height * .65):] = False
    count, _, stats, centers = helper.cv.connectedComponentsWithStats(dark.astype("uint8"), 8)
    heads = []
    for index in sorted(range(1, count), key=lambda i: stats[i, helper.cv.CC_STAT_TOP]):
        if stats[index, helper.cv.CC_STAT_AREA] > 60 \
                and stats[index, helper.cv.CC_STAT_WIDTH] > 10 \
                and stats[index, helper.cv.CC_STAT_HEIGHT] > 10 \
                and all(abs(centers[index, 0] - x) > row.width / columns * .55 for x in heads):
            heads.append(float(centers[index, 0]))
    if len(heads) != columns:
        raise ValueError(f"Esperadas {columns} cabeças distintas, encontradas {len(heads)}")
    heads.sort()
    return [0] + [round((a + b) / 2) for a, b in zip(heads, heads[1:])] + [row.width]


def protected_digest(root):
    digest = hashlib.sha256()
    count = 0
    for file in sorted(root.rglob("*")):
        if file.is_file() and file.relative_to(root).parts[0] not in APPROVED:
            digest.update(file.relative_to(root).as_posix().encode("utf-8") + b"\0")
            digest.update(file.read_bytes())
            count += 1
    return count, digest.hexdigest()


def crouch_kick_cells(row, columns):
    # Os pés atravessam os limites entre colunas. Separar silhuetas inteiras,
    # não cortar a meio entre cabeças, o que amputaria os pontapés.
    pixels = helper.np.array(row)
    rgb = pixels[:, :, :3].astype(float)
    alpha = pixels[:, :, 3].copy()
    red = (rgb[:, :, 0] > 110) & (rgb[:, :, 0] > 1.8 * rgb[:, :, 1]) & (rgb[:, :, 0] > 1.8 * rgb[:, :, 2])
    yellow = (helper.np.minimum(rgb[:, :, 0], rgb[:, :, 1]) > 125) \
        & (rgb[:, :, 2] < .45 * helper.np.minimum(rgb[:, :, 0], rgb[:, :, 1]))
    alpha[red | yellow] = 0
    count, labels, stats, _ = helper.cv.connectedComponentsWithStats((alpha >= 64).astype("uint8"), 8)
    bodies = sorted((i for i in range(1, count) if stats[i, helper.cv.CC_STAT_AREA] > 5000),
                    key=lambda i: stats[i, helper.cv.CC_STAT_LEFT])
    if len(bodies) != columns:
        raise ValueError("Não foi possível separar todos os pontapés agachados")
    cells, windows = [], []
    for index in bodies:
        keep = helper.cv.dilate((labels == index).astype("uint8"), helper.np.ones((3, 3), "uint8"))
        isolated = pixels.copy()
        isolated[:, :, 3] = helper.np.where(keep, alpha, 0)
        left = max(0, int(stats[index, helper.cv.CC_STAT_LEFT]) - 2)
        right = min(row.width, int(stats[index, helper.cv.CC_STAT_LEFT] + stats[index, helper.cv.CC_STAT_WIDTH]) + 2)
        cells.append(Image.fromarray(isolated).crop((left, 0, right, row.height)))
        windows.append([left, right])
    return cells, windows


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    helper.cv.setNumThreads(2)
    args.destination.mkdir(parents=True, exist_ok=True)
    protected_before = protected_digest(args.destination)
    report = {"source": args.archive.name, "excluded": list(EXCLUDED), "animations": {}}
    standing_ratio = None
    with zipfile.ZipFile(args.archive) as archive:
        manifest = {item["animation"]: item for item in json.loads(archive.read("manifest.json"))["files"]}
        for name, ((columns, rows), phases) in APPROVED.items():
            item = manifest[name]
            if item["status"] != "aproximado":
                raise ValueError("A folha aprovada mudou de estado: " + name)
            if hashlib.sha256(archive.read(item["file"])).hexdigest() != item["sha256"]:
                raise ValueError("Checksum inválido: " + name)
            with archive.open(item["file"]) as stream:
                sheet = Image.open(stream).convert("RGBA")
            cells, cuts_by_row = [], []
            for y in range(rows):
                row = sheet.crop((0, round(y * sheet.height / rows),
                                  sheet.width, round((y + 1) * sheet.height / rows)))
                if name == "crouch_lk":
                    isolated, windows = crouch_kick_cells(row, columns)
                    cells.extend(isolated)
                    cuts_by_row.append(windows)
                    continue
                cuts = row_cuts(row, columns)
                cuts_by_row.append([[a, b] for a, b in zip(cuts, cuts[1:])])
                for x in range(columns):
                    cell = row.crop((cuts[x], 0, cuts[x + 1], row.height))
                    if name == "hp":
                        # O fundo branco tem alpha ~95–150; a personagem está perto de 250.
                        pixels = helper.np.array(cell)
                        pixels[:, :, 3][pixels[:, :, 3] < 200] = 0
                        cell = Image.fromarray(pixels)
                    cells.append(helper.clean_cell(cell, "alpha"))
            bounds = [cell.getbbox() for cell in cells]
            if name == "hp":
                standing_ratio = (bounds[0][3] - bounds[0][1]) / cells[0].height
            height = max(box[3] - box[1] for box in bounds)
            if name == "crouch_lp":
                # Todas as poses já estão agachadas: não as ampliar à altura de um corpo em pé.
                height = max(height, standing_ratio * cells[0].height)
            scale = min(400 / height, 448 / max(box[2] - box[0] for box in bounds))
            folder = args.destination / name
            folder.mkdir(exist_ok=True)
            hd_folder = Path(__file__).parent / "frames-hd" / name
            hd_folder.mkdir(parents=True, exist_ok=True)
            preview = Image.new("RGBA", (columns * 256, rows * 224), "#44566b")
            draw = ImageDraw.Draw(preview)
            for index, (cell, box) in enumerate(zip(cells, bounds), 1):
                pose = cell.crop(box)
                pose = pose.resize((round(pose.width * scale), round(pose.height * scale)), Image.Resampling.LANCZOS)
                canvas = Image.new("RGBA", (512, 448))
                # Sem âncoras do autor, centra-se a silhueta; não inventar deslocamento de combate.
                px = (512 - pose.width) // 2
                canvas.alpha_composite(pose, (px, 432 - pose.height))
                canvas.save(hd_folder / f"{name}_{index:03}.png")
                frame = canvas.resize((128, 112), Image.Resampling.LANCZOS)
                frame.paste((0, 0, 0, 0), (0, 0, 128, 8))
                frame.paste((0, 0, 0, 0), (0, 108, 128, 112))
                frame.save(folder / f"{name}_{index:03}.png")
                x, y = (index - 1) % columns * 256, (index - 1) // columns * 224
                preview.alpha_composite(canvas.resize((256, 224), Image.Resampling.LANCZOS), (x, y))
                draw.text((x + 5, y + 5), str(index), fill="white")
            # LK tem dez poses nesta folha; não deixar as poses 11–12 do preview antigo.
            for old in folder.glob(f"{name}_*.png"):
                match = re.fullmatch(re.escape(name) + r"_(\d{3})\.png", old.name)
                if match and int(match[1]) > len(cells):
                    old.unlink()
            preparation, active, recovery = phases
            if sum(phases) != len(cells):
                raise ValueError("Plano visual não corresponde às poses: " + name)
            (folder / "animacao.properties").write_text(
                f"poses={len(cells)}\npreparacao={preparation}\nativos={active}\nrecuperacao={recovery}\n",
                encoding="ascii")
            preview.save(Path(__file__).with_name(f"{name}_preview.png"))
            report["animations"][name] = {
                "poses": len(cells), "grid": [columns, rows], "source_size": list(sheet.size),
                "source_sha256": item["sha256"], "scale": scale,
                "source_columns_by_row": cuts_by_row, "anchor_policy": "per_pose_bbox_center",
                "visual_phases": list(phases),
                "background_cleanup": "alpha_threshold_200" if name == "hp" else "alpha_and_color_artifacts",
            }
            print(f"{name}: {len(cells)} poses; fases {phases}")
    protected_after = protected_digest(args.destination)
    if protected_before != protected_after:
        raise RuntimeError("Foram alterados recursos fora das cinco animações aprovadas")
    report["protected_files"] = protected_before[0]
    report["protected_sha256_before"] = protected_before[1]
    report["protected_sha256_after"] = protected_after[1]
    Path(__file__).with_name("importacao.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Preservados: {protected_before[0]} ficheiros fora das cinco animações aprovadas")


if __name__ == "__main__":
    main()
