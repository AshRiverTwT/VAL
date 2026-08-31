(function () {
  function section(title, items) {
    if (!items || items.length === 0) return '';
    return `
      <div class="patch-section">
        <h4>${title}</h4>
        <ul>${items.map(i => `<li>${i}</li>`).join('')}</ul>
      </div>`;
  }

  function patchEntry(patch) {
    return `
      <div class="patch-entry">
        <h3>Patch ${patch.version}</h3>
        <p class="patch-date">${patch.date}</p>
        ${section('Highlights', patch.highlights)}
        ${section('Agent Updates', patch.agentUpdates)}
        ${section('Map Updates', patch.mapUpdates)}
        ${section('Bug Fixes', patch.bugFixes)}
      </div>`;
  }

  async function loadPatchNotes() {
    const list = document.getElementById('patchList');
    try {
      const res = await fetch('/api/patch-notes');
      if (!res.ok) throw new Error(`status ${res.status}`);
      const patches = await res.json();
      list.innerHTML = patches.map(patchEntry).join('');
    } catch (err) {
      list.innerHTML = `<p class="error-text">Could not load patch notes: ${err.message}</p>`;
    }
  }

  document.addEventListener('DOMContentLoaded', loadPatchNotes);
})();
