(() => {
  const grid = document.getElementById('catalogGrid');
  if (!grid) return;
  grid.querySelectorAll('.catalog-card').forEach(card => {
    const image = card.querySelector('.product-photo img');
    const failed = () => { image.hidden = true; card.querySelector('.catalog-photo-fallback').hidden = false; };
    image.addEventListener('error',failed);
    if (image.complete && !image.naturalWidth) failed();
  });
  restore();
})();
