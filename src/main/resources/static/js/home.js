(() => {
  if (!document.body.classList.contains('home-page')) return;
  const root = document.documentElement;
  const navbar = document.getElementById('mainNav');
  const banner = document.querySelector('body > .demo-banner');
  const sections = [...document.querySelectorAll('main > .home-section')];
  const links = [...document.querySelectorAll('.home-section-nav a')];
  const measure = () => {
    root.style.setProperty('--home-nav-height', `${Math.ceil(navbar?.getBoundingClientRect().height || 78)}px`);
    root.style.setProperty('--home-demo-height', `${Math.ceil(banner?.getBoundingClientRect().height || 0)}px`);
  };
  const update = () => {
    const marker = (navbar?.getBoundingClientRect().height || 78) + window.innerHeight * .3;
    let current = sections[0];
    sections.forEach(section => { if (section.getBoundingClientRect().top <= marker) current = section; });
    links.forEach(link => {
      if (link.hash === `#${current?.id}`) link.setAttribute('aria-current', 'location');
      else link.removeAttribute('aria-current');
    });
  };
  measure(); root.classList.add('home-snap'); update();
  const observer = new ResizeObserver(() => { measure(); update(); });
  if (navbar) observer.observe(navbar);
  if (banner) observer.observe(banner);
  let queued = false;
  window.addEventListener('scroll', () => {
    if (queued) return;
    queued = true;
    requestAnimationFrame(() => { update(); queued = false; });
  }, { passive: true });
  window.addEventListener('resize', () => { measure(); update(); });
  document.addEventListener('show.bs.modal', () => root.classList.add('home-modal-open'));
  document.addEventListener('hidden.bs.modal', () => root.classList.remove('home-modal-open'));
})();
