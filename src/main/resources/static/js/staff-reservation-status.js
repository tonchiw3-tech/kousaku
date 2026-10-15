(() => {
  const detailStyle = document.createElement('link'); detailStyle.rel = 'stylesheet'; detailStyle.href = '/css/staff-detail.css'; document.head.appendChild(detailStyle);
  const match = location.pathname.match(/^\/staff\/reservations\/([^/]+)\/?$/), id = match ? decodeURIComponent(match[1]) : null;
  if (!id) return;
  const message = document.querySelector('#status-message');
  const nativeFetch = window.fetch.bind(window);
  window.fetch = async (...args) => {
    const response = await nativeFetch(...args);
    if (!response.ok) {
      const body = await response.clone().text();
      console.error(`予約対応API failed: HTTP ${response.status}`, body);
      if (message) message.dataset.errorDetail = `HTTP ${response.status}: ${body}`;
    }
    return response;
  };
  const counts = { RECEIVED: 0, CONFIRMED: 1, READY: 2, COMPLETED: 3 };
  const render = status => {
    ['confirmed', 'ready', 'completed'].forEach((name, index) => { const item = document.querySelector(`#check-${name}`), checkbox = item?.querySelector('.progress-checkbox'), checked = index < (counts[status] ?? 0); if (checkbox) { checkbox.checked = checked; checkbox.disabled = status === 'CANCELED' || status === 'COMPLETED' || index !== (counts[status] ?? 0); item.classList.toggle('is-checked', checked); } });
    const action = document.querySelector('#progress-action');
    if (status === 'COMPLETED') action.innerHTML = '<p class="progress-complete">すべての予約対応が完了しています</p>';
    else if (status === 'CANCELED') action.innerHTML = '<p class="progress-canceled">キャンセル済み</p>';
    else action.innerHTML = '';
  };
  document.querySelectorAll('.progress-checkbox').forEach(checkbox => checkbox.addEventListener('change', () => { if (!checkbox.checked) { checkbox.checked = true; return; } checkbox.disabled = true; fetch(`/api/staff/reservations/${encodeURIComponent(id)}/status`, { method: 'PATCH', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ status: checkbox.dataset.nextStatus }) }).then(r => { if (!r.ok) throw Error(); return r.json(); }).then(r => render(r.status)).catch(() => { checkbox.checked = false; if (message) message.textContent = '予約対応の更新に失敗しました。'; }); }));
  fetch(`/api/staff/reservations/${encodeURIComponent(id)}`).then(r => { if (!r.ok) throw Error(); return r.json(); }).then(r => { const detail = document.querySelector('#reservation-detail'); if (detail) detail.innerHTML = `<div class="detail-field"><dt>予約番号</dt><dd>${r.reservationNumber ?? '-'}</dd></div><div class="detail-field"><dt>予約日</dt><dd>${r.reservationDate ?? '-'}</dd></div><div class="detail-field"><dt>お客様</dt><dd>${r.customerName ?? '-'}</dd></div><div class="detail-field"><dt>ステータス</dt><dd>${r.status ?? '-'}</dd></div>`; render(r.status); if (r.status === 'CANCELED' || r.status === 'COMPLETED') document.querySelector('#cancel-button')?.remove(); }).catch(() => { if (message) message.textContent = '予約対応を取得できませんでした。'; });
  const cancel = document.querySelector('#cancel-button'), cancelMessage = document.querySelector('#cancel-message'); cancel?.addEventListener('click', () => { if (!confirm('この予約をキャンセルしますか？')) return; cancel.disabled = true; fetch(`/api/staff/reservations/${encodeURIComponent(id)}/cancel`, { method: 'PATCH' }).then(r => { if (!r.ok) throw Error(); }).then(() => location.reload()).catch(() => { cancelMessage.textContent = 'キャンセルに失敗しました。'; cancel.disabled = false; }); });
  const remove = document.querySelector('#delete-button'), deleteMessage = document.querySelector('#delete-message'); remove?.addEventListener('click', () => { if (!confirm('予約データを完全に削除しますか？この操作は元に戻せません。')) return; remove.disabled = true; fetch(`/api/staff/reservations/${encodeURIComponent(id)}`, { method: 'DELETE' }).then(r => { if (r.status !== 204) throw Error(); }).then(() => { location.href = '/staff/reservations'; }).catch(() => { deleteMessage.textContent = '削除に失敗しました。'; remove.disabled = false; }); });
  /* The legacy status-select implementation below was unreachable and has
     intentionally been removed. The checklist above is driven by DB status. */
  return;
  /*
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
  const cancel = document.querySelector('#cancel-button'), cancelMessage = document.querySelector('#cancel-message');
  cancel?.addEventListener('click', () => { if (!confirm('この予約をキャンセルしますか？')) return; cancel.disabled = true; fetch(`/api/staff/reservations/${encodeURIComponent(id)}/cancel`, { method: 'PATCH' }).then(r => { if (!r.ok) throw Error(); }).then(() => { cancelMessage.textContent = '予約をキャンセルしました。'; location.reload(); }).catch(() => { cancelMessage.textContent = 'キャンセルに失敗しました。'; cancel.disabled = false; }); });
  const remove = document.querySelector('#delete-button'), deleteMessage = document.querySelector('#delete-message');
  remove?.addEventListener('click', () => { if (!confirm('この予約データを完全に削除します。この操作は元に戻せません。削除しますか？')) return; remove.disabled = true; fetch(`/api/staff/reservations/${encodeURIComponent(id)}`, { method: 'DELETE' }).then(r => { if (r.status !== 204) throw Error(); }).then(() => { location.href = '/staff/reservations'; }).catch(() => { deleteMessage.textContent = '削除に失敗しました。'; remove.disabled = false; }); });
  */
})();
