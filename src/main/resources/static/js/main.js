document.addEventListener("DOMContentLoaded", () => {
  const navLinks = document.querySelectorAll("#navbarContent .nav-link, #adminNavbarContent .nav-link");

  navLinks.forEach(link => {
    const path = new URL(link.href).pathname;
    const currentPath = window.location.pathname;
    const selected = currentPath === path ||
      ((path === "/productos" || /^\/admin\/(products|categories|inquiries)$/.test(path)) && currentPath.startsWith(path + "/"));
    link.classList.toggle("active", selected);
    if (selected) link.setAttribute("aria-current", "page");
    else link.removeAttribute("aria-current");
  });

  navLinks.forEach(link => {
    link.addEventListener("click", () => {
      const target = link.closest(".navbar-collapse");
      if (target?.classList.contains("show")) {
        window.bootstrap?.Collapse?.getOrCreateInstance(target).hide();
      }
    });
  });

  document.querySelectorAll("[data-confirm]").forEach(form => {
    form.addEventListener("submit", event => {
      if (!window.confirm(form.dataset.confirm)) {
        event.preventDefault();
      }
    });
  });

});
