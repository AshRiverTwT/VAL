(function () {
  function infoItem(label, value, pending) {
    return `
      <div class="info-item">
        <div class="info-label">${label}</div>
        <div class="info-value ${pending ? 'pending' : ''}">${value}</div>
      </div>`;
  }

  async function loadInfo() {
    const grid = document.getElementById('infoGrid');
    try {
      const [infoRes, healthRes] = await Promise.all([fetch('/info'), fetch('/health')]);
      if (!infoRes.ok) throw new Error(`/info returned status ${infoRes.status}`);

      const info = await infoRes.json();
      const health = healthRes.ok ? await healthRes.json() : { status: 'DOWN' };
      const healthPill = `<span class="status-pill ${health.status === 'UP' ? 'up' : 'down'}">${health.status}</span>`;

      grid.innerHTML = [
        infoItem('Application Version', info.applicationVersion),
        infoItem('Java Version', `${info.javaVersion} (${info.javaVendor})`),
        infoItem('Environment', info.environment),
        infoItem('Build Number', info.buildNumber),
        infoItem('Git Commit', info.gitCommit),
        infoItem('Build Time', info.buildTime),
        infoItem('Hostname', info.hostname),
        infoItem('Container Status', info.containerStatus),
        infoItem('Health', healthPill)
      ].join('');
    } catch (err) {
      grid.innerHTML = `<p class="error-text">Could not load runtime info: ${err.message}</p>`;
    }
  }

  document.addEventListener('DOMContentLoaded', loadInfo);
})();
