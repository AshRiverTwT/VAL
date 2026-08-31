(function () {
  function weaponRow(weapon) {
    return `
      <tr>
        <td><b>${weapon.name}</b></td>
        <td>${weapon.category}</td>
        <td>${weapon.price === 0 ? 'Free' : weapon.price}</td>
        <td>${weapon.damage}</td>
        <td>${weapon.fireRate}</td>
        <td>${weapon.magazineSize}</td>
      </tr>`;
  }

  async function loadWeapons() {
    const tbody = document.getElementById('weaponTableBody');
    try {
      const res = await fetch('/api/weapons');
      if (!res.ok) throw new Error(`status ${res.status}`);
      const weapons = await res.json();
      tbody.innerHTML = weapons.map(weaponRow).join('');
    } catch (err) {
      tbody.innerHTML = `<tr><td colspan="6" class="error-text">Could not load weapons: ${err.message}</td></tr>`;
    }
  }

  document.addEventListener('DOMContentLoaded', loadWeapons);
})();
