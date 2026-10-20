(() => {
  const id = decodeURIComponent(location.pathname.split('/').filter(Boolean).pop() || '');
  const labels = { RECEIVED: '申込受付', CONFIRMED: '予約内容を確認済み', READY: '受け入れ準備完了', COMPLETED: '体験完了', CANCELED: 'キャンセル' };
  const detail = document.querySelector('#reservation-detail');
  const message = document.querySelector('#status-message');
  const correctionBox = document.querySelector('#staff-correction');
  const cancelButton = document.querySelector('#cancel-button');
  const correctionButton = document.querySelector('#correction-button');
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
  const patchHeaders = () => ({ 'Content-Type': 'application/json', ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {}) });
  let currentReservation;
  let progressBusy = false;
  const esc = value => String(value ?? '-').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
  const isFuture = r => r.reservationDate && r.reservationTime && new Date(`${r.reservationDate}T${String(r.reservationTime).slice(0, 8)}+09:00`).getTime() > Date.now();
  const setBusy = busy => { document.querySelectorAll('.progress-checkbox, button').forEach(e => e.disabled = busy); if (!busy && currentReservation) render(currentReservation); };
  function render(r) {
    currentReservation = r;
    const status = r.status;
    const confirmed = ['CONFIRMED', 'READY', 'COMPLETED'].includes(status);
    const ready = ['READY', 'COMPLETED'].includes(status);
    document.querySelector('#current-status').textContent = labels[status] || status;
    detail.innerHTML = `<div class="detail-field"><dt>予約番号</dt><dd>${esc(r.reservationNumber)}</dd></div><div class="detail-field"><dt>予約日</dt><dd>${esc(r.reservationDate)}</dd></div><div class="detail-field"><dt>お客様</dt><dd>${esc(r.customerName)}</dd></div><div class="detail-field"><dt>ステータス</dt><dd>${esc(labels[status] || status)}</dd></div>`;
    document.querySelectorAll('.progress-checkbox').forEach((box, index) => {
      const checked = index === 0 ? confirmed : ready;
      box.checked = checked;
      box.disabled = progressBusy || status === 'CANCELED' || status === 'COMPLETED' || (index === 1 && !confirmed);
      box.closest('li').classList.toggle('is-checked', checked);
      box.closest('li').classList.toggle('is-disabled', box.disabled);
    });
    const action = document.querySelector('#progress-action');
    action.innerHTML = status === 'READY' ? `<button id="complete-button" class="primary-button" type="button"${isFuture(r) ? ' disabled title="予約開始時刻までは完了できません"' : ''}>体験完了</button>${isFuture(r) ? '<p class="action-message">予約開始時刻を過ぎてから完了してください。</p>' : ''}` : status === 'COMPLETED' ? '<p class="progress-complete">体験が完了しています。</p>' : status === 'CANCELED' ? '<p class="progress-canceled">キャンセル済みです。</p>' : '';
    document.querySelector('#complete-button')?.addEventListener('click', () => { if (confirm('体験を完了として登録しますか？')) updateStatus('COMPLETED'); });
    const unavailable = status === 'CANCELED' || status === 'COMPLETED';
    cancelButton.hidden = unavailable;
    document.querySelector('#reservation-actions').hidden = unavailable;
    if (correctionBox) correctionBox.hidden = status !== 'COMPLETED';
  }
  function updateStatus(status) { setBusy(true); fetch(`/api/staff/reservations/${encodeURIComponent(id)}/status`, { method: 'PATCH', headers: patchHeaders(), body: JSON.stringify({ status }) }).then(r => { if (!r.ok) throw Error(); return r.json(); }).then(render).catch(() => { message.textContent = 'ステータスの更新に失敗しました。'; }).finally(() => setBusy(false)); }
  fetch(`/api/staff/reservations/${encodeURIComponent(id)}`).then(r => { if (!r.ok) throw Error(); return r.json(); }).then(render).catch(() => { message.textContent = '予約詳細を取得できませんでした。'; });
  function toggleProgress(status, savingMessage) {
    if (progressBusy) return;
    progressBusy = true;
    message.textContent = savingMessage;
    render(currentReservation);
    fetch(`/api/staff/reservations/${encodeURIComponent(id)}/status`, { method: 'PATCH', headers: patchHeaders(), body: JSON.stringify({ status }) })
      .then(r => { if (!r.ok) throw Error(); return r.json(); })
      .then(r => { render(r); message.textContent = '保存しました。'; })
      .catch(() => { render(currentReservation); message.textContent = '保存に失敗しました。表示を元に戻しました。'; })
      .finally(() => { progressBusy = false; render(currentReservation); });
  }
  document.querySelectorAll('.progress-checkbox').forEach((box, index) => box.addEventListener('change', () => {
    if (progressBusy || !currentReservation) return;
    const wasReady = currentReservation.status === 'READY';
    const target = index === 0 ? (box.checked ? 'CONFIRMED' : 'RECEIVED') : (box.checked ? 'READY' : 'CONFIRMED');
    const notice = index === 0 && !box.checked && wasReady
      ? '予約内容の確認を解除すると、受け入れ準備完了も解除されます。保存中…'
      : '保存中…';
    toggleProgress(target, notice);
  }));
  cancelButton.addEventListener('click', () => { if (!confirm('この予約をキャンセルしますか？')) return; setBusy(true); fetch(`/api/staff/reservations/${encodeURIComponent(id)}/cancel`, { method: 'PATCH', headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {} }).then(r => { if (!r.ok) throw Error(); return r.json(); }).then(render).catch(() => { document.querySelector('#cancel-message').textContent = 'キャンセルに失敗しました。'; }).finally(() => setBusy(false)); });
  correctionButton?.addEventListener('click', () => { const reason = document.querySelector('#correction-reason').value.trim(); const correctionMessage = document.querySelector('#correction-message'); if (!reason) { correctionMessage.textContent = '訂正理由を入力してください。'; return; } if (!confirm('訂正理由を確認しました。COMPLETEDからREADYへ訂正しますか？')) return; setBusy(true); fetch(`/api/staff/reservations/${encodeURIComponent(id)}/correction`, { method: 'PATCH', headers: patchHeaders(), body: JSON.stringify({ reason }) }).then(r => { if (r.status === 403) throw Error('スタッフ認証が必要です。'); if (!r.ok) throw Error('訂正に失敗しました。'); return r.json(); }).then(r => { document.querySelector('#correction-reason').value = ''; render(r); }).catch(e => { correctionMessage.textContent = e.message; }).finally(() => setBusy(false)); });
})();
