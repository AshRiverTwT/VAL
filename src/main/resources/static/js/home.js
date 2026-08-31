/**
 * Home page: pulls a few agents/maps/patch notes from the JSON APIs and
 * renders a "featured" preview of each, plus the latest patch summary.
 */
(function () {
  async function fetchJson(url) {
    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`Request to ${url} failed with status ${res.status}`);
    }
    return res.json();
  }

  function agentCard(agent) {
    return `
      <div class="card">
        <div class="avatar" style="background:${agent.avatarColor}">${agent.avatarInitials}</div>
        <span class="badge">${agent.role}</span>
        <h3>${agent.name}</h3>
        <p>${agent.description}</p>
        <div class="meta-row">
          <span>Region: ${agent.region}</span>
          <span>Difficulty: ${agent.difficulty}</span>
        </div>
      </div>`;
  }

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

  function patchSummary(patch) {
    return `
      <div class="patch-entry">
        <h3>Patch ${patch.version}</h3>
        <p class="patch-date">${patch.date}</p>
        <div class="patch-section">
          <h4>Highlights</h4>
          <ul>${patch.highlights.map(h => `<li>${h}</li>`).join('')}</ul>
        </div>
      </div>`;
  }

  async function loadFeatured() {
    const agentsEl = document.getElementById('featuredAgents');
    const mapsEl = document.getElementById('featuredMaps');
    const patchEl = document.getElementById('latestPatch');

    try {
      const agents = await fetchJson('/api/agents');
      agentsEl.innerHTML = agents.slice(0, 3).map(agentCard).join('');
    } catch (err) {
      agentsEl.innerHTML = `<p class="error-text">Could not load agents: ${err.message}</p>`;
    }

    try {
      const maps = await fetchJson('/api/maps');
      mapsEl.innerHTML = maps.slice(0, 3).map(mapCard).join('');
    } catch (err) {
      mapsEl.innerHTML = `<p class="error-text">Could not load maps: ${err.message}</p>`;
    }

    try {
      const patches = await fetchJson('/api/patch-notes');
      patchEl.innerHTML = patches.length ? patchSummary(patches[0]) : '<p>No patch notes available.</p>';
    } catch (err) {
      patchEl.innerHTML = `<p class="error-text">Could not load patch notes: ${err.message}</p>`;
    }
  }

  document.addEventListener('DOMContentLoaded', loadFeatured);
})();
