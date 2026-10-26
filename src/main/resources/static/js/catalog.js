(() => {
  const grid = document.getElementById('catalogGrid');
  if (!grid) return;
  const cards = [...grid.querySelectorAll('.catalog-card')];
  const search = document.getElementById('catalogSearch');
  const sort = document.getElementById('catalogSort');
  const clear = document.getElementById('catalogClear');
  const empty = document.getElementById('catalogNoResults');
  const summary = document.getElementById('catalogResultSummary');
  const normalize = value => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('es').trim();
  const collator = new Intl.Collator('es', { sensitivity: 'base', numeric: true });
  const update = (saveUrl = true) => {
    const query = search.value.trim().slice(0, 120);
    const terms = normalize(query).split(/\s+/).filter(Boolean);
    let visible = 0;
    const ordered = [...cards].sort((a,b) => collator.compare(a.dataset.name,b.dataset.name) * (sort.value === 'desc' ? -1 : 1));
    ordered.forEach(card => {
      const text = normalize(card.dataset.search);
      card.hidden = !terms.every(term => text.includes(term));
      if (!card.hidden) visible++;
      grid.append(card);
    });
    empty.hidden = visible !== 0;
    clear.hidden = !query;
    summary.textContent = `${visible} ${visible === 1 ? 'detalle' : 'detalles'} ${query ? 'encontrados de ' + cards.length : 'para descubrir'}`;
    if (visible === 1 && query) summary.textContent = `1 detalle encontrado de ${cards.length}`;
    document.querySelectorAll('.catalog-categories a').forEach(link => {
      const target = new URL(link.href);
      if (query) target.searchParams.set('q',query); else target.searchParams.delete('q');
      if (sort.value === 'desc') target.searchParams.set('orden','desc'); else target.searchParams.delete('orden');
      link.href = target.pathname + target.search;
    });
    if (saveUrl) {
      const url = new URL(location.href);
      if (query) url.searchParams.set('q', query); else url.searchParams.delete('q');
      if (sort.value === 'desc') url.searchParams.set('orden', 'desc'); else url.searchParams.delete('orden');
      history.replaceState(null,'',url.pathname + url.search + url.hash);
    }
  };
  const restore = () => {
    const params = new URLSearchParams(location.search);
    search.value = (params.get('q') || '').slice(0,120);
    sort.value = params.get('orden') === 'desc' ? 'desc' : 'asc';
    update(false);
  };
  document.querySelector('[data-catalog-enhanced]').hidden = false;
  document.querySelector('.catalog-search-row').addEventListener('submit',event => { event.preventDefault(); update(); });
  search.addEventListener('input', () => update());
  sort.addEventListener('change', () => update());
  const reset = () => { search.value = ''; update(); search.focus(); };
  clear.addEventListener('click',reset);
  document.getElementById('catalogResetSearch').addEventListener('click',reset);
  window.addEventListener('popstate', restore);
  cards.forEach(card => {
    const image = card.querySelector('.product-photo img');
    const failed = () => { image.hidden = true; card.querySelector('.catalog-photo-fallback').hidden = false; };
    image.addEventListener('error',failed);
    if (image.complete && !image.naturalWidth) failed();
  });
  restore();
})();
