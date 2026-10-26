// Exportaciones raster del favicon vectorial; no modifica los logos originales.
const sharp = require(process.env.SHARP_MODULE_PATH || 'sharp');
const path = require('node:path');
const root = path.resolve(__dirname, '..', 'src/main/resources/static/assets/brand');
(async () => {
  for (const [name, size] of [['favicon-32.png',32], ['favicon-192.png',192], ['apple-touch-icon.png',180], ['favicon-preview.png',512]]) {
    await sharp(path.join(root, 'favicon.svg')).resize(size,size).png().toFile(path.join(root,name));
  }
})().catch(error => { console.error(error.message); process.exitCode = 1; });
