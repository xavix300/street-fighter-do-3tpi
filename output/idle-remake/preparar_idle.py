"""Recorta a folha 6x2 fornecida pelo utilizador; requer Pillow.

Uso: python output/idle-remake/preparar_idle.py <folha.png> <pasta-destino>
Não altera a folha original. Escala comum e chão fixo; não estica cada pose.
"""

import argparse
from collections import deque
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter


def isolar_corpo(cell):
    width, height = cell.size
    alpha = cell.getchannel("A")
    values = alpha.tobytes()
    visited = bytearray(width * height)
    largest = []
    for start, value in enumerate(values):
        if value < 128 or visited[start]:
            continue
        queue = deque([start])
        visited[start] = 1
        component = []
        while queue:
            index = queue.popleft()
            component.append(index)
            x, y = index % width, index // width
            for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                if 0 <= nx < width and 0 <= ny < height:
                    other = ny * width + nx
                    if not visited[other] and values[other] >= 128:
                        visited[other] = 1
                        queue.append(other)
        if len(component) > len(largest):
            largest = component
    if len(largest) < 1000:
        raise ValueError("Não foi encontrada uma silhueta completa.")
    mask_values = bytearray(width * height)
    for index in largest:
        mask_values[index] = 255
    mask = Image.frombytes("L", cell.size, bytes(mask_values))
    # Conserva o antialiasing original junto à silhueta, não números nem grelha.
    cell.putalpha(ImageChops.multiply(alpha, mask.filter(ImageFilter.MaxFilter(5))))
    return cell


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    sheet = Image.open(args.source).convert("RGBA")
    if sheet.width % 6 or sheet.height % 2:
        raise ValueError("A folha deve ter uma grelha regular de 6x2.")
    width, height = sheet.width // 6, sheet.height // 2
    cells = []
    for index in range(12):
        x, y = index % 6 * width, index // 6 * height
        cell = sheet.crop((x + 3, y + 3, x + width - 3, y + height - 3))
        cells.append(isolar_corpo(cell))
    bounds = [cell.getbbox() for cell in cells]
    scale = 100 / max(box[3] - box[1] for box in bounds)
    args.destination.mkdir(parents=True, exist_ok=True)
    preview = Image.new("RGBA", (768, 224), "#44566b")
    draw = ImageDraw.Draw(preview)
    for index, (cell, box) in enumerate(zip(cells, bounds), 1):
        pose = cell.crop(box)
        pose = pose.resize(
            (round(pose.width * scale), round(pose.height * scale)),
            Image.Resampling.LANCZOS,
        )
        canvas = Image.new("RGBA", (128, 112))
        # Mantém o eixo da célula original e alinha apenas a linha de chão.
        x = round(64 + (box[0] - cell.width / 2) * scale)
        y = 108 - pose.height
        canvas.alpha_composite(pose, (x, y))
        assert canvas.getbbox()[3] <= 108
        canvas.save(args.destination / f"idle_{index:03}.png")
        px, py = (index - 1) % 6 * 128, (index - 1) // 6 * 112
        preview.alpha_composite(canvas, (px, py))
        draw.text((px + 4, py + 3), str(index), fill="white")
        print(f"idle_{index:03}.png: origem={box}, final={canvas.getbbox()}")
    preview.save(Path(__file__).with_name("preview.png"))


if __name__ == "__main__":
    main()
