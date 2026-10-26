(() => {
  const form = document.querySelector('#productEditor');
  if (!form) return;
  const field = id => document.getElementById(id);
  const main = field('productImagePath'), extras = field('extraImageInputs');
  let photos = [...new Set([main.value, ...[...extras.querySelectorAll('input')].map(x => x.value)].filter(Boolean))];
  const url = value => value.startsWith('/') ? form.dataset.contextPath.replace(/\/$/, '') + value : value;
  const feedback = message => { field('uploadFeedback').textContent = message; };
  function preview() {
    field('previewName').textContent = field('productName').value || 'Nombre de tu detalle';
    field('previewDescription').textContent = field('productDescription').value || 'Su historia empieza aquí.';
    field('previewCategory').textContent = field('productCategory').selectedOptions[0]?.textContent || 'Categoría';
    const mode = field('priceMode').value, price = Number(field('price').value);
    field('priceField').hidden = mode === 'CONSULT';
    field('stockThresholdField').hidden = field('supplyMode').value !== 'STOCK';
    field('previewPrice').textContent = mode === 'CONSULT' || !price ? 'Precio a consultar' : (mode === 'FROM' ? 'Desde ' : '') + 'S/ ' + price.toFixed(2);
    field('previewEmpty').hidden = !!photos.length;
    field('previewImage').hidden = !photos.length;
    if (photos.length) field('previewImage').src = url(photos[0]);
    field('previewImage').style.objectPosition = `${field('focusX').value}% ${field('focusY').value}%`;
  }
  function render() {
    main.value = photos[0] || '';
    extras.replaceChildren();
    photos.slice(1).forEach(value => { const input = document.createElement('input'); input.type = 'hidden'; input.name = 'additionalImagePaths'; input.value = value; extras.append(input); });
    field('photoGallery').replaceChildren();
    photos.forEach((value, index) => {
      const box = document.createElement('div'); box.className = 'editor-photo';
      const img = document.createElement('img'); img.src = url(value); img.alt = `Fotografía ${index + 1}`;
      const label = document.createElement('strong'); label.textContent = index === 0 ? 'Principal' : `Fotografía ${index + 1}`;
      const actions = document.createElement('div');
      function button(text, action) { const b = document.createElement('button'); b.type = 'button'; b.className = 'btn btn-sm btn-light'; b.textContent = text; b.addEventListener('click', action); actions.append(b); }
      if (index) button('Hacer principal', () => { photos.splice(index, 1); photos.unshift(value); render(); });
      if (index > 1) button('Anterior', () => { [photos[index - 1], photos[index]] = [photos[index], photos[index - 1]]; render(); });
      button('Quitar', () => { photos.splice(index, 1); render(); });
      box.append(img, label, actions); field('photoGallery').append(box);
    });
    preview();
  }
  let uploading = false;
  async function upload(files) {
    if (uploading) return;
    uploading = true;
    const buttons = [...form.querySelectorAll('[type=submit], #saveDraft')]; buttons.forEach(b => b.disabled = true);
    try {
      for (const file of files) {
        if (photos.length >= 5) throw new Error('El máximo es de cinco fotografías. Quita una para añadir otra.');
        if (!['image/jpeg', 'image/png'].includes(file.type) || file.size > 5 * 1024 * 1024) throw new Error('Selecciona JPG o PNG de hasta 5 MB.');
        feedback(`Subiendo ${file.name}…`);
        const body = new FormData(); body.append('file', file);
        const response = await fetch(form.dataset.uploadUrl, { method: 'POST', body, headers: { [document.querySelector('meta[name=_csrf_header]').content]: document.querySelector('meta[name=_csrf]').content } });
        const data = await response.json().catch(() => { throw new Error('No se pudo subir la imagen. Comprueba tu sesión y vuelve a intentarlo.'); });
        if (!response.ok) throw new Error(data.error || 'No se pudo subir la imagen.');
        photos.push(data.url); render();
      }
      feedback('Fotografías listas. Guarda el producto para conservar la selección.');
    } catch (error) { feedback(error.message); }
    finally { uploading = false; buttons.forEach(b => b.disabled = false); field('photoFiles').value = ''; }
  }
  field('photoFiles').addEventListener('change', event => upload([...event.target.files]));
  const zone = form.querySelector('.upload-zone');
  zone.addEventListener('dragover', event => event.preventDefault());
  zone.addEventListener('drop', event => { event.preventDefault(); upload([...event.dataTransfer.files]); });
  form.querySelectorAll('[data-image-value]').forEach(button => button.addEventListener('click', () => {
    const value = button.dataset.imageValue;
    if (photos.includes(value)) return feedback('Esta fotografía ya está seleccionada.');
    if (photos.length >= 5) return feedback('Puedes seleccionar hasta cinco fotografías.');
    photos.push(value); render(); feedback('Fotografía añadida. Guarda el producto para conservarla.');
  }));
  form.addEventListener('input', preview); form.addEventListener('change', preview);
  form.addEventListener('submit', event => { if (uploading) event.preventDefault(); });
  field('saveDraft').addEventListener('click', () => { field('publicationStatus').value = 'DRAFT'; form.requestSubmit(); });
  form.addEventListener('demo:product-image', event => {
    // Reutilizar una fotografía local sin subir archivos ni reemplazar la galería actual.
    if (!photos.length && event.detail?.image) { photos.push(event.detail.image); render(); }
  });
  render();
})();
