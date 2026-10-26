(() => {
  const message = document.getElementById('message');
  const count = document.getElementById('messageCount');
  if (message && count) {
    const update = () => { count.textContent = `${message.value.length} / 2000`; };
    message.addEventListener('input',update); update();
  }
  const invalid = document.querySelector('.contact-message-form [aria-invalid="true"]');
  if (invalid) invalid.focus();
  else document.getElementById('contactConfirmation')?.focus();
})();
