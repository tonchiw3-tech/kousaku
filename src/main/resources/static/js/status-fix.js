(() => {
  const m = location.pathname.match(/^\/staff\/reservations\/([^/]+)\/?$/);
  if (!m) return;
  const id = decodeURIComponent(m[1]);
  document.querySelectorAll('.progress-checkbox').forEach(box => box.addEventListener('change', async () => {
    if (!box.checked) { box.checked = true; return; }
    box.disabled = true;
    const response = await fetch('/api/staff/reservations/' + encodeURIComponent(id) + '/status', { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: box.dataset.nextStatus }) });
    if (!response.ok) { box.checked = false; box.disabled = false; return; }
    const message = document.querySelector('#status-message');
    if (message) message.textContent = '対応状況を更新しました。';
    setTimeout(() => location.reload(), 300);
  }));
})();
