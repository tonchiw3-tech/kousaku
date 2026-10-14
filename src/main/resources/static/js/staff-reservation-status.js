(() => {
  const match = location.pathname.match(/^\/staff\/reservations\/([^/]+)\/?$/);
  const id = match ? decodeURIComponent(match[1]) : null;
  const select = document.querySelector('#status-select');
  const button = document.querySelector('#status-update-button');
  const message = document.querySelector('#status-message');
  if (!id || !select || !button || !message) return;
  fetch(`/api/staff/reservations/${encodeURIComponent(id)}`).then(r => { if (!r.ok) throw new Error(`HTTP ${r.status}`); return r.json(); }).then(r => { select.value = r.status; }).catch(() => { message.textContent = 'ステータスを取得できませんでした。'; });
  button.addEventListener('click', () => {
    button.disabled = true;
    fetch(`/api/staff/reservations/${encodeURIComponent(id)}/status`, { method: 'PATCH', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ status: select.value }) })
      .then(r => { if (!r.ok) throw new Error(`HTTP ${r.status}`); return r.json(); })
      .then(() => { message.textContent = 'ステータスを変更しました。'; location.reload(); })
      .catch(() => { message.textContent = 'ステータスの変更に失敗しました。'; })
      .finally(() => { button.disabled = false; });
  });
})();
