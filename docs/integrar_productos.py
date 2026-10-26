"""Exporta el inventario revisado a recursos Spring y copia fotos sin alterarlas."""
from pathlib import Path
import json, shutil, hashlib

root = Path(__file__).resolve().parents[1]
rows = json.loads((root/'Productos/catalogo-productos.json').read_text(encoding='utf-8'))
source_root=(root/'Productos').resolve()
target_root=(root/'src/main/resources/static/assets/products').resolve()
# Validar todos los elementos antes de copiar el primero.
for row in rows:
    source=(source_root/row['archivo']).resolve()
    target=(target_root/row['archivo']).resolve()
    if not source.is_relative_to(source_root) or not target.is_relative_to(target_root):
        raise ValueError('La ruta de una imagen sale de la carpeta de productos.')
    if hashlib.sha256(source.read_bytes()).hexdigest() != row['sha256']:
        raise ValueError(f'La fotografía no coincide con el inventario: {row["archivo"]}')
categories = {
 'flores-y-arreglos': ('Flores', 'Ramos y arreglos florales para expresar lo que sientes.'),
 'ramos-tematicos': ('Ramos temáticos', 'Flores, personajes y detalles reunidos en un ramo especial.'),
 'boxes-con-personajes': ('Boxes con personajes', 'Peluche y detalles inspirados en tus personajes favoritos.'),
 'regalos-motor': ('Pasión y motores', 'Autos de colección y detalles para celebrar sus pasiones.'),
 'regalos-personalizados': ('Regalos personalizados', 'Tazas, libretas y accesorios con un toque personal.'),
 'boxes-romanticos': ('Boxes románticos', 'Fotografías, dulces y pequeños gestos para compartir.'),
 'boxes-de-cuidado': ('Mimos y cuidado', 'Detalles de cuidado personal acompañados de mucha ternura.'),
}
lines=['# UTF-8. Generado desde Productos/catalogo-productos.json.',
       'products='+','.join(r['id'] for r in rows if r['tipo']=='principal')]
for key,(name,description) in categories.items():
    lines += [f'category.{key}.name={name}',f'category.{key}.description={description}']
for row in rows:
    source=root/'Productos'/row['archivo']
    target=root/'src/main/resources/static/assets/products'/row['archivo']
    target.parent.mkdir(parents=True,exist_ok=True)
    shutil.copy2(source,target)
    if row['tipo'] != 'principal':
        continue
    prefix=row['id']
    lines += [f'{prefix}.name={row["nombre"]}', f'{prefix}.description={row["descripcion"]}',
              f'{prefix}.category={row["categoria"]}',f'{prefix}.image=/assets/products/{row["archivo"]}']
    alternate=next((r for r in rows if r['id']==prefix and r['tipo']=='vista-alternativa'),None)
    if alternate:
        lines.append(f'{prefix}.secondaryImage=/assets/products/{alternate["archivo"]}')
dest=root/'src/main/resources/catalog/products.properties'
dest.parent.mkdir(exist_ok=True)
dest.write_text('\n'.join(lines)+'\n',encoding='utf-8')
print(f'{len(rows)} fotos copiadas; {sum(r["tipo"]=="principal" for r in rows)} productos exportados.')
