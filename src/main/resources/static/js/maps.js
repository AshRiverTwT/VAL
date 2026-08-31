(function () {
  function mapCard(map) {
    return `
      <div class="card">
        <div class="map-tile" style="background:linear-gradient(135deg, ${map.accentColor}, #0f1216)"></div>
        <span class="badge alt">${map.type}</span>
        <h3>${map.name}</h3>
        <p>${map.description}</p>
        <div class="meta-row"><span>Sites: ${map.sites.join(', ')}</span></div>
      </div>`;
  }

  async function loadMaps() {
    const grid = document.getElementById('mapGrid');
    try {
      const res = await fetch('/api/maps');
      if (!res.ok) throw new Error(`status ${res.status}`);
      const maps = await res.json();
      grid.innerHTML = maps.map(mapCard).join('');
    } catch (err) {
      grid.innerHTML = `<p class="error-text">Could not load maps: ${err.message}</p>`;
    }
  }

  document.addEventListener('DOMContentLoaded', loadMaps);
})();
