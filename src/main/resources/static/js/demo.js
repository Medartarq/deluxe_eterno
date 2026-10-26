/* Ayudas de autollenado: completan formularios, nunca los envían. */
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-demo-kind]').forEach(tools => {
    const form = tools.closest('form');
    if (!form) return;
    const feedback = tools.querySelector('.demo-feedback');
    const scenario = tools.querySelector('[data-demo-scenario]');
    const set = (name, value) => {
      const element = form.elements.namedItem(name);
      if (!element || element instanceof RadioNodeList) return;
      if (element.type === 'checkbox') element.checked = Boolean(value);
      else element.value = value;
      element.dispatchEvent(new Event('input', { bubbles: true }));
      element.dispatchEvent(new Event('change', { bubbles: true }));
    };
    const stock = () => {
      const physical = Number(tools.dataset.physical), reserved = Number(tools.dataset.reserved);
      return { physical, reserved, available: physical - reserved };
    };
    if (tools.dataset.demoKind === 'stock') {
      const balance = stock();
      for (const option of scenario.options) {
        option.disabled = (option.value === 'IN' && balance.physical >= 1000000)
          || (['OUT', 'RESERVE'].includes(option.value) && balance.available <= 0)
          || (option.value === 'RELEASE' && balance.reserved <= 0)
          || (option.value === 'CORRECTION' && balance.physical === 1000000 && balance.reserved === balance.physical);
      }
      const first = [...scenario.options].find(option => !option.disabled);
      if (first) scenario.value = first.value;
    }
    tools.querySelector('[data-demo-fill]').addEventListener('click', () => {
      const occupied = [...form.querySelectorAll('input:not([type]), input[type=text], input[type=email], input[type=tel], textarea, input[type=number]')]
        .some(element => element.name !== 'lowStockThreshold' && element.value.trim() && !(element.type === 'number' && Number(element.value) === 0));
      if (occupied && !window.confirm('Se reemplazarán los campos de ejemplo de este formulario. No se guardará nada hasta que lo confirmes con el botón habitual. ¿Continuar?')) return;
      const suffix = crypto.randomUUID().slice(0, 6).toUpperCase();
      switch (tools.dataset.demoKind) {
        case 'category':
          set('name', `DEMO · Detalles especiales ${suffix}`);
          set('description', 'Detalles personalizados para celebrar ocasiones especiales. Categoría de ejemplo.');
          break;
        case 'contact':
          set('name', 'DEMO · Cliente de ejemplo');
          set('email', 'cliente@example.test');
          set('phone', '');
          set('message', '[DEMO] Consulta de ejemplo: quisiera conocer las opciones de personalización de un regalo para aniversario.');
          break;
        case 'product': {
          const units = scenario.value === 'stock';
          set('name', `DEMO · ${units ? 'Box de Ilusiones' : 'Ramo de Cariño'} ${suffix}`);
          set('description', 'Detalle de ejemplo: un regalo preparado con dedicación, flores y una presentación especial para celebrar momentos inolvidables.');
          set('includedItems', 'Composición floral de referencia\nTarjeta con dedicatoria\nPresentación con lazo');
          set('personalization', 'Nombre y dedicatoria breve; colores sujetos a coordinación. Ejemplo ficticio.');
          set('occasions', 'Aniversario, cumpleaños y agradecimiento');
          set('preparationTime', 'Ejemplo: coordinar con 24 horas de anticipación');
          set('supplyMode', units ? 'STOCK' : 'MADE_TO_ORDER');
          set('priceMode', units ? 'FIXED' : 'CONSULT');
          set('price', units ? '89.90' : '');
          set('lowStockThreshold', 3);
          set('temporarilyUnavailable', false);
          set('publicationStatus', 'DRAFT');
          set('internalNotes', '[DEMO] Producto de ejemplo. Revisar precio y disponibilidad antes de publicar.');
          const category = form.elements.namedItem('categoryId');
          const options = [...category.options].filter(option => option.value);
          const selected = options.find(option => option.textContent.startsWith('DEMO'))
            || options.find(option => option.textContent === (units ? 'Regalos personalizados' : 'Flores')) || options[0];
          if (selected) set('categoryId', selected.value);
          form.dispatchEvent(new CustomEvent('demo:product-image', { detail: { image: '/assets/products/flores-y-arreglos/cielo-de-girasoles.jpg' } }));
          break;
        }
        case 'stock': {
          const { physical, reserved, available } = stock();
          const type = scenario.value;
          if (!type || scenario.selectedOptions[0].disabled) { feedback.textContent = 'No hay un ejemplo válido para este saldo. Revisa el inventario.'; return; }
          let quantity = 0;
          if (type === 'IN') quantity = Math.min(10, 1000000 - physical);
          if (type === 'RESERVE' || type === 'OUT') quantity = Math.min(2, available);
          if (type === 'RELEASE') quantity = Math.min(2, reserved);
          if (type === 'CORRECTION') quantity = physical < 1000000 ? Math.min(1000000, physical + 2) : physical - 1;
          set('type', type); set('quantity', quantity);
          set('reason', `[DEMO] ${scenario.selectedOptions[0].textContent} de ejemplo; revisado manualmente.`);
          break;
        }
      }
      feedback.textContent = 'Ejemplo completado. Revisa los datos y usa el botón habitual para guardar o enviar.';
    });
  });
});
