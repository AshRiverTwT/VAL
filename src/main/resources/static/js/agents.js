(function () {
  function abilityItem(ability) {
    return `<li><b>${ability.name}</b> (${ability.type}) &mdash; ${ability.description}</li>`;
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
        <ul class="ability-list">${agent.abilities.map(abilityItem).join('')}</ul>
      </div>`;
  }

  async function loadAgents() {
    const grid = document.getElementById('agentGrid');
    try {
      const res = await fetch('/api/agents');
      if (!res.ok) throw new Error(`status ${res.status}`);
      const agents = await res.json();
      grid.innerHTML = agents.map(agentCard).join('');
    } catch (err) {
      grid.innerHTML = `<p class="error-text">Could not load agents: ${err.message}</p>`;
    }
  }

  document.addEventListener('DOMContentLoaded', loadAgents);
})();
