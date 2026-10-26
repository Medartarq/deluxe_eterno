"""Clasificación visual revisada de la entrega original. No altera los píxeles."""
from pathlib import Path
import csv
import hashlib
import html
import json
import unicodedata

ROOT = Path(__file__).resolve().parents[1]
FOLDER = ROOT / 'Productos'
INVENTORY = ROOT / 'docs/revision-productos/inventario-original.json'
# Índice de la hoja de contacto, categoría, nombre comercial propuesto, descripción visual.
DATA = [
 (1,'boxes-con-personajes','Rayito de Ternura','Box con peluche de pollito, flores amarillas y presentación con ventana.'),
 (2,'boxes-con-personajes','Snoopy Primavera','Box con peluche de Snoopy, detalle floral amarillo y lazo a juego.'),
 (3,'ramos-tematicos','Kitty Sol de Girasoles','Ramo amarillo con peluche de Hello Kitty y flores decorativas tipo girasol.'),
 (4,'boxes-con-personajes','Pollito de Mi Corazón','Box con peluche de pollito y presentación blanca con lazo amarillo.'),
 (5,'regalos-motor','Pole Position','Arreglo temático de autos con envoltura negra, lazo azul y detalle enmarcado.'),
 (6,'ramos-tematicos','Acelerando Ilusiones','Ramo azul con autos de colección, osito y rosas decorativas.'),
 (7,'regalos-motor','Parada Dulce','Box con auto de colección, bebida, sándwich y acompañamientos.'),
 (8,'ramos-tematicos','Rosas a Toda Velocidad','Ramo negro y dorado con autos de colección y rosas azules decorativas.'),
 (9,'ramos-tematicos','Gran Premio Azul','Ramo de autos de colección, rosas azules y detalles dulces.'),
 (10,'regalos-motor','Nuestra Mejor Ruta','Arreglo de autos de colección con cuadro fotográfico y lazo azul.'),
 (11,'regalos-motor','Turbo de Sonrisas','Box de autos de colección y snacks con gran lazo azul.'),
 (12,'ramos-tematicos','Estilo en Azul','Ramo con gorra de emblema NY, rosas azules y envoltura negra.'),
 (13,'regalos-motor','Pasión Crema sobre Ruedas','Arreglo de autos de colección con detalle enmarcado de temática Universitario.'),
 (14,'regalos-motor','Garra de Campeón','Bandeja temática de Universitario con peluche de león, globo, taza y acompañamientos.'),
 (15,'boxes-con-personajes','Mi Superhéroe Favorito','Box temático de Spider-Man con peluche y detalles a juego.'),
 (16,'regalos-motor','Ruta de Aventuras','Bandeja de madera con auto de colección, botella personalizada y cuadro temático.'),
 (17,'regalos-personalizados','Distinción Personal','Box cilíndrico oscuro con libreta, accesorio en estuche y detalle dulce.'),
 (18,'regalos-motor','Amor en la Parrilla de Salida','Box de temática automovilística con auto de colección, vaso y snacks.'),
 (19,'flores-y-arreglos','Pequeños Grandes Detalles','Colección de minirramos con envolturas rosadas y tarjetas.'),
 (20,'boxes-con-personajes','Mi Gran Tesoro','Box temático de One Piece con peluche de Luffy y detalles decorativos.'),
 (21,'regalos-personalizados','Inspiración Natural','Bandeja con taza y libreta personalizadas, accesorio en estuche y follaje decorativo.'),
 (22,'boxes-romanticos','Dulces Recuerdos','Box con fotografías, chocolates, bebida y corazón decorativo rojo.'),
 (23,'boxes-con-personajes','Abrazo de Koala','Box con peluche de koala, fotografía y snacks.'),
 (24,'flores-y-arreglos','Jardín de Caricias','Arreglo floral rosado en box cilíndrico con globo transparente y tarjeta.'),
 (25,'regalos-personalizados','Brindis con Estilo','Bandeja con taza personalizada y accesorios, acompañada de bolsa para botella.'),
 (26,'regalos-motor','Ruta de Aventuras','Vista alternativa de la bandeja con auto de colección, botella personalizada y cuadro temático.'),
 (27,'regalos-personalizados','Esencia Minimalista','Set en caja con botella oscura personalizada y libreta a juego.'),
 (28,'flores-y-arreglos','Susurro de Lirios','Ramo de lirios rosados con flores complementarias y envoltura blanca.'),
 (29,'flores-y-arreglos','Rosas de Aurora','Ramo de rosas rosadas con flores complementarias y envoltura rosa suave.'),
 (30,'flores-y-arreglos','Rojo que Enamora','Ramo de rosas rojas con pequeñas flores blancas y envoltura rosada.'),
 (31,'flores-y-arreglos','Cielo de Girasoles','Ramo de girasoles y flores claras con envoltura azul y tarjeta ilustrada.'),
 (32,'ramos-tematicos','Kitty Sueño Rosa','Ramo rosado con peluche de Hello Kitty, flores decorativas y pequeños detalles.'),
 (33,'boxes-de-cuidado','Mimos de Pingüino','Box rosado con peluche de pingüino y productos de cuidado personal.'),
 (34,'boxes-de-cuidado','Dulzura de Algodón','Box con peluche de ovejita, tulipán decorativo y pequeños detalles de cuidado.'),
 (35,'boxes-de-cuidado','Un Abrazo Rosa','Box rosado con peluche de oso y productos de cuidado personal.'),
 (36,'boxes-de-cuidado','Kitty Día Bonito','Box con peluche de Hello Kitty, accesorios y detalles de cuidado personal.'),
 (37,'boxes-de-cuidado','Kitty Love','Box compacto de temática Hello Kitty con pequeños detalles y productos de cuidado.'),
]

def slug(text):
    return ''.join(c for c in unicodedata.normalize('NFKD',text.lower()) if not unicodedata.combining(c)).replace(' ','-')

def main():
    if INVENTORY.exists():
        inventory = json.loads(INVENTORY.read_text(encoding='utf-8'))
    else:
        # El inventario público permite repetir la verificación en otro checkout.
        current = json.loads((FOLDER/'catalogo-productos.json').read_text(encoding='utf-8'))
        inventory = [dict(original=r['original'], size=[r['ancho'], r['alto']], sha256=r['sha256'])
                     for r in sorted(current, key=lambda r: r['original'])]
    assert len(inventory) == len(DATA) == 37
    records, moves = [], []
    for index, category, name, description in DATA:
        old = inventory[index-1]
        product_id = f'DE-{(16 if index == 26 else index):03}'
        role = 'vista-alternativa' if index == 26 else ('coleccion' if index == 19 else 'principal')
        filename = slug(name) + ('-vista-02' if index == 26 else '') + '.jpg'
        relative = f'{category}/{filename}'
        source, target = FOLDER/old['original'], FOLDER/relative
        assert target.resolve().is_relative_to(FOLDER.resolve())
        current = source if source.exists() else target
        assert current.is_file(), f'Falta imagen: {current}'
        assert hashlib.sha256(current.read_bytes()).hexdigest() == old['sha256'], 'Contenido cambiado'
        if source.exists():
            assert not target.exists(), f'Destino ocupado: {target}'
            moves.append((source,target))
        records.append(dict(id=product_id,nombre=name,categoria=category,descripcion=description,
                            alt=description,archivo=relative,original=old['original'],tipo=role,
                            ancho=old['size'][0],alto=old['size'][1],sha256=old['sha256']))
    assert len({r['archivo'] for r in records}) == 37
    # Todas las rutas y huellas se verifican antes de renombrar el primer archivo.
    (FOLDER/'catalogo-productos.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
    with (FOLDER/'catalogo-productos.csv').open('w',encoding='utf-8-sig',newline='') as stream:
        writer=csv.DictWriter(stream,fieldnames=list(records[0]))
        writer.writeheader(); writer.writerows(records)
    for source,target in moves:
        target.parent.mkdir(exist_ok=True)
        source.rename(target)
    for row in records:
        assert hashlib.sha256((FOLDER/row['archivo']).read_bytes()).hexdigest() == row['sha256']
    cards=[]
    for row in records:
        cards.append(f'<article data-category="{row["categoria"]}"><a href="{row["archivo"]}"><img src="{row["archivo"]}" loading="lazy" alt="{html.escape(row["alt"])}"></a><div><small>{row["id"]} · {row["tipo"]}</small><h2>{html.escape(row["nombre"])}</h2><p>{html.escape(row["descripcion"])}</p><code>{row["archivo"]}</code></div></article>')
    categories=sorted({r['categoria'] for r in records})
    options=''.join(f'<option value="{c}">{c.replace("-"," ").capitalize()}</option>' for c in categories)
    page='''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Productos · Deluxe Eterno</title>
<style>body{margin:0;background:#fff6f8;color:#292326;font:16px system-ui}main{max-width:1280px;margin:auto;padding:40px 24px}h1{font-size:38px;margin-bottom:12px}header p{max-width:800px;line-height:1.6;color:#756168}select{padding:12px;border:1px solid #d6a0aa;border-radius:12px;background:white;color:#a83240;font:inherit;margin:16px 0 28px;max-width:100%}section{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:24px}article{background:white;border:1px solid #ecdde1;border-radius:18px;overflow:hidden}article img{width:100%;height:360px;object-fit:contain;background:#f6f2f2}article div{padding:20px}h2{font-size:21px}small{color:#a83240}p{line-height:1.6}code{font-size:11px;overflow-wrap:anywhere;color:#756168}[hidden]{display:none!important}</style>
<main><header><h1>Detalles con nombre propio</h1><p>37 fotografías revisadas · 35 propuestas individuales, 1 vista alternativa y 1 colección de minirramos. Los nombres son propuestas editoriales para la galería; la composición definitiva y disponibilidad se confirman con la tienda.</p></header><label for="category">Categoría </label><select id="category"><option value="all">Ver todas</option>'''+options+'''</select><section>'''+''.join(cards)+'''</section></main><script>document.getElementById('category').addEventListener('change',e=>document.querySelectorAll('article').forEach(card=>card.hidden=e.target.value!=='all'&&card.dataset.category!==e.target.value));</script></html>'''
    (FOLDER/'index.html').write_text(page,encoding='utf-8')
    print(json.dumps(dict(imagenes=len(records),renombradas=len(moves),productos_individuales=35,
                         vistas_alternativas=1,colecciones=1,
                         categorias={c:sum(r['categoria']==c for r in records) for c in categories},
                         duplicados_exactos=len(records)-len({r['sha256'] for r in records})),ensure_ascii=False))

if __name__ == '__main__':
    main()
