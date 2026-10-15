(() => {
  const match = location.pathname.match(/^\/staff\/reservations\/([^/]+)\/?$/);
  const id = match && decodeURIComponent(match[1]);
  if (!id) return;
  const button = document.querySelector('#revert-status-button');
  const message = document.querySelector('#revert-status-message');
  const previous = { CONFIRMED: 'RECEIVED', READY: 'CONFIRMED', COMPLETED: 'READY' };
  let status;
  const update = value => {
    status = value;
    const canRevert = Object.hasOwn(previous, value);
    if (button) { button.disabled = !canRevert; button.hidden = !canRevert; }
  };
  fetch(`/api/staff/reservations/${encodeURIComponent(id)}`).then(r => { if (!r.ok) throw Error(); return r.json(); }).then(r => update(r.status));
  button?.addEventListener('click', () => {
    if (!previous[status] || !confirm('対応状況を1つ前に戻しますか？')) return;
    button.disabled = true;
    fetch(`/api/staff/reservations/${encodeURIComponent(id)}/revert-status`, { method: 'PATCH' })
      .then(r => { if (!r.ok) throw Error(); return r.json(); })
      .then(r => { update(r.status); message.textContent = '対応状況を修正しました。'; setTimeout(() => location.reload(), 500); })
      .catch(() => { message.textContent = '対応状況の修正に失敗しました。'; button.disabled = false; });
  });
})();
