(() => {
  const id = decodeURIComponent(location.pathname.split('/').filter(Boolean).pop() || '');
  const labels = { RECEIVED: '申込受付', CONFIRMED: '予約確認済み', READY: '受け入れ準備完了', COMPLETED: '体験完了', CANCELED: 'キャンセル' };
  const detail = document.querySelector('#reservation-detail');
  const message = document.querySelector('#status-message');
  const adminBox = document.querySelector('#admin-correction');
  const cancelButton = document.querySelector('#cancel-button');
  const correctionButton = document.querySelector('#correction-button');
  let isAdmin = false;
  let currentReservation;

  const esc = value => String(value ?? '-').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
  const isFuture = reservation => reservation.reservationDate && reservation.reservationTime && new Date(`${reservation.reservationDate}T${String(reservation.reservationTime).slice(0, 8)}+09:00`).getTime() > Date.now();
  function setBusy(busy) {
    if (busy) {
      document.querySelectorAll('.progress-checkbox, button').forEach(element => { element.disabled = true; });
    } else if (currentReservation) {
      render(currentReservation);
    }
  }

  function render(reservation) {
    currentReservation = reservation;
    const status = reservation.status;
    const completedSteps = { RECEIVED: 0, CONFIRMED: 1, READY: 2, COMPLETED: 2, CANCELED: 0 }[status] ?? 0;
    document.querySelector('#current-status').textContent = labels[status] || status;
    detail.innerHTML = `<div class="detail-field"><dt>予約番号</dt><dd>${esc(reservation.reservationNumber)}</dd></div><div class="detail-field"><dt>予約日</dt><dd>${esc(reservation.reservationDate)}</dd></div><div class="detail-field"><dt>お客様</dt><dd>${esc(reservation.customerName)}</dd></div><div class="detail-field"><dt>ステータス</dt><dd>${esc(labels[status] || status)}</dd></div>`;
    document.querySelectorAll('.progress-checkbox').forEach((box, index) => {
      box.checked = index < completedSteps;
      box.disabled = status === 'CANCELED' || status === 'COMPLETED' || index !== completedSteps;
      box.closest('li').classList.toggle('is-checked', box.checked);
    });
    const action = document.querySelector('#progress-action');
    action.innerHTML = status === 'READY'
      ? `<button id="complete-button" class="primary-button" type="button"${isFuture(reservation) ? ' disabled title="予約開始時刻までは完了できません"' : ''}>体験完了</button>${isFuture(reservation) ? '<p class="action-message">予約開始時刻を過ぎてから、実際の体験終了を確認して完了してください。</p>' : ''}`
      : status === 'COMPLETED' ? '<p class="progress-complete">体験が完了しています</p>'
      : status === 'CANCELED' ? '<p class="progress-canceled">キャンセル済みです</p>' : '';
    document.querySelector('#complete-button')?.addEventListener('click', () => {
      if (confirm('体験を完了して、予約ステータスを「体験完了」に更新しますか？')) updateStatus('COMPLETED');
    });
    const cancellationUnavailable = status === 'CANCELED' || status === 'COMPLETED';
    cancelButton.hidden = cancellationUnavailable;
    document.querySelector('#reservation-actions').hidden = cancellationUnavailable;
    adminBox.hidden = !(isAdmin && status === 'COMPLETED');
  }

  function updateStatus(status) {
    setBusy(true);
    fetch(`/api/staff/reservations/${encodeURIComponent(id)}/status`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status }) })
      .then(response => { if (!response.ok) throw new Error(); return response.json(); })
      .then(render).catch(() => { message.textContent = 'ステータスの更新に失敗しました。'; }).finally(() => setBusy(false));
  }

  Promise.all([
    fetch('/api/staff/reservations/admin-capability').then(response => response.ok ? response.json() : { admin: false }),
    fetch(`/api/staff/reservations/${encodeURIComponent(id)}`).then(response => { if (!response.ok) throw new Error(); return response.json(); })
  ]).then(([capability, reservation]) => { isAdmin = capability.admin === true; render(reservation); })
    .catch(() => { message.textContent = '予約内容を取得できませんでした。'; });

  document.querySelectorAll('.progress-checkbox').forEach(box => box.addEventListener('change', () => {
    if (box.checked) updateStatus(box.dataset.nextStatus); else box.checked = true;
  }));
  cancelButton.addEventListener('click', () => {
    if (!confirm('この予約をキャンセルしますか？')) return;
    setBusy(true);
    fetch(`/api/staff/reservations/${encodeURIComponent(id)}/cancel`, { method: 'PATCH' })
      .then(response => { if (!response.ok) throw new Error(); return response.json(); })
      .then(render).catch(() => { document.querySelector('#cancel-message').textContent = 'キャンセルに失敗しました。'; }).finally(() => setBusy(false));
  });
  correctionButton.addEventListener('click', () => {
    const reason = document.querySelector('#correction-reason').value.trim();
    const correctionMessage = document.querySelector('#correction-message');
    if (!reason) { correctionMessage.textContent = '訂正理由を入力してください。'; return; }
    if (!confirm('訂正理由を確認しました。COMPLETEDからREADYへ訂正しますか？')) return;
    setBusy(true);
    fetch(`/api/staff/reservations/${encodeURIComponent(id)}/admin-correction`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ reason }) })
      .then(response => {
        if (response.status === 403) throw new Error('管理者権限が必要です。');
        if (!response.ok) throw new Error('訂正に失敗しました。');
        return response.json();
      }).then(reservation => { document.querySelector('#correction-reason').value = ''; render(reservation); })
      .catch(error => { correctionMessage.textContent = error.message; }).finally(() => setBusy(false));
  });
})();
