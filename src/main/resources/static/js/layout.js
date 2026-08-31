/**
 * Injects the shared navbar and footer into every page (elements with
 * id="navbar" / id="footer"). Keeping this in one file avoids duplicating
 * the same markup across six static HTML pages.
 */
document.addEventListener('DOMContentLoaded', () => {
  const nav = document.getElementById('navbar');
  const footer = document.getElementById('footer');
  const path = window.location.pathname;

  const links = [
    { href: '/', label: 'Home' },
    { href: '/agents.html', label: 'Agents' },
    { href: '/maps.html', label: 'Maps' },
    { href: '/weapons.html', label: 'Weapons' },
    { href: '/patch-notes.html', label: 'Patch Notes' },
    { href: '/about.html', label: 'About' }
  ];

  function isActive(href) {
    if (href === '/') {
      return path === '/' || path === '/index.html' || path === '';
    }
    return path.endsWith(href);
  }

  if (nav) {
    nav.innerHTML = `
      <div class="navbar">
        <a class="brand" href="/"><span class="brand-mark">VT</span>VALORANT Tactical Hub</a>
        <button class="nav-toggle" id="navToggle" aria-label="Toggle navigation" aria-expanded="false">&#9776;</button>
        <ul class="nav-links" id="navLinks">
          ${links.map(l => `<li><a href="${l.href}" class="${isActive(l.href) ? 'active' : ''}">${l.label}</a></li>`).join('')}
        </ul>
      </div>`;

    const toggle = document.getElementById('navToggle');
    const navLinks = document.getElementById('navLinks');
    toggle.addEventListener('click', () => {
      const open = navLinks.classList.toggle('open');
      toggle.setAttribute('aria-expanded', String(open));
    });
  }

  if (footer) {
    footer.innerHTML = `
      <div class="footer-inner">
        <p>VALORANT Tactical Hub &mdash; a fan-made, non-commercial DevOps practice project. Not affiliated with or endorsed by Riot Games.</p>
        <p class="footer-meta">Built for hands-on DevOps L1 practice: Git &middot; Maven &middot; Docker &middot; Jenkins &middot; Nginx &middot; systemd &middot; AWS EC2.</p>
      </div>`;
  }
});
