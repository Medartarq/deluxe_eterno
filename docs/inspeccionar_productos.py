from PIL import Image, ImageOps, ImageDraw
from pathlib import Path
import json, hashlib

root = Path(__file__).resolve().parents[1]
files = sorted((root/'Productos').glob('*.jpg'))
out = root/'docs/revision-productos'
if not files:
    raise SystemExit('Las fotografías ya fueron clasificadas; se conserva el inventario original sin sobrescribirlo.')
if (out/'inventario-original.json').exists():
    raise SystemExit('El inventario original ya existe; no se sobrescribe automáticamente.')
out.mkdir(exist_ok=True)
rows = []
for i, path in enumerate(files):
    with Image.open(path) as im:
        rows.append(dict(index=i+1, original=path.name, size=im.size,
                         sha256=hashlib.sha256(path.read_bytes()).hexdigest()))
(out/'inventario-original.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding='utf-8')
for start in range(0, len(files), 9):
    sheet = Image.new('RGB', (1200, 1350), 'white')
    draw = ImageDraw.Draw(sheet)
    for j, path in enumerate(files[start:start+9]):
        with Image.open(path) as source:
            im = ImageOps.exif_transpose(source).convert('RGB')
            im.thumbnail((388, 405))
            x, y = (j % 3)*400, (j // 3)*450
            sheet.paste(im, (x+(400-im.width)//2, y))
            draw.text((x+12, y+410), f'{start+j+1} - {path.name[:9]}', fill='black')
    sheet.save(out/f'contacto-{start//9+1}.jpg')
print(f'{len(files)} imágenes inventariadas')
