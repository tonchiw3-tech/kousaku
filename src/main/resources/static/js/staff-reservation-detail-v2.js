(() => {
  const id = decodeURIComponent(location.pathname.split('/').filter(Boolean).pop() || '');
  const labels = { RECEIVED: '申込受付', CONFIRMED: '予約確認済み', READY: '受け入れ準備完了', COMPLETED: '体験完了', CANCELED: 'キャンセル' };
  const detail = document.querySelector('#reservation-detail'), message = document.querySelector('#status-message');
  const adminBox = document.createElement('section'); adminBox.id = 'admin-correction'; adminBox.className = 'danger-zone'; adminBox.hidden = true; adminBox.innerHTML = '<h2>管理者専用：ステータス訂正</h2><p>誤って「体験完了」になった予約を「受け入れ準備完了」に戻します。</p><label>訂正理由<span class="required">必須</span><textarea id="correction-reason" rows="3" maxlength="1000" required></textarea></label><button id="correction-button" class="danger-button" type="button">訂正内容を確認</button><p id="correction-message" class="action-message" role="status"></p>'; document.querySelector('.reservation-actions')?.after(adminBox);
  const esc = v => String(v ?? '-').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
  const isFutureReservation = r => r.reservationDate && r.reservationTime && new Date(`${r.reservationDate}T${String(r.reservationTime).slice(0, 8)}+09:00`).getTime() > Date.now();
  const render = r => {
    const status = r.status, count = { RECEIVED: 0, CONFIRMED: 1, READY: 2, COMPLETED: 2, CANCELED: 0 }[status] ?? 0, future = isFutureReservation(r);
    document.querySelector('#current-status').textContent = labels[status] || status;
    detail.innerHTML = `<div class="detail-field"><dt>予約番号</dt><dd>${esc(r.reservationNumber)}</dd></div><div class="detail-field"><dt>予約日</dt><dd>${esc(r.reservationDate)}</dd></div><div class="detail-field"><dt>お客様</dt><dd>${esc(r.customerName)}</dd></div><div class="detail-field"><dt>ステータス</dt><dd>${esc(labels[status] || status)}</dd></div>`;
    document.querySelectorAll('.progress-checkbox').forEach((box, i) => { box.checked = i < count; box.disabled = status === 'CANCELED' || status === 'COMPLETED' || i !== count; box.closest('li').classList.toggle('is-checked', box.checked); });
    const action = document.querySelector('#progress-action');
    action.innerHTML = status === 'READY' ? `<button id="complete-button" class="primary-button" type="button"${future ? ' disabled title="予約開始時刻までは完了できません"' : ''}>体験完了</button>${future ? '<p class="action-message">予約開始時刻を過ぎてから、実際の体験終了を確認して完了してください。</p>' : ''}` : status === 'COMPLETED' ? '<p class="progress-complete">体験が完了しています</p>' : status === 'CANCELED' ? '<p class="progress-canceled">キャンセル済み</p>' : '';
    document.querySelector('#complete-button')?.addEventListener('click', () => { if (confirm('体験を完了して、予約ステータスを「体験完了」に更新しますか？')) update('COMPLETED'); });
    const cancel = document.querySelector('#cancel-button');
    if (cancel) cancel.hidden = status === 'CANCELED' || status === 'COMPLETED';
    adminBox.hidden = status !== 'COMPLETED';
    document.querySelectorAll('button,.progress-checkbox').forEach(e => { if (!e.hidden) e.disabled = false; });
  };
  const update = next => { document.querySelectorAll('button,.progress-checkbox').forEach(e => e.disabled = true); fetch(`/api/staff/reservations/${encodeURIComponent(id)}/status`, { method:'PATCH', headers:{'Content-Type':'application/json'}, body:JSON.stringify({status:next}) }).then(r => { if (!r.ok) throw Error(); return r.json(); }).then(render).catch(() => { message.textContent = 'ステータスの更新に失敗しました。'; }); };
  Promise.all([
    fetch('/api/staff/reservations/admin-capability').then(r => r.ok ? r.json() : {admin:false}),
    fetch(`/api/staff/reservations/${encodeURIComponent(id)}`).then(r => { if (!r.ok) throw Error(); return r.json(); })
  ]).then(([capability, reservation]) => { if (!capability.admin) adminBox.remove(); render(reservation); })
    .catch(() => { message.textContent = '予約対応を取得できませんでした。'; });
  document.querySelectorAll('.progress-checkbox').forEach(box => box.addEventListener('change', () => { if (box.checked) update(box.dataset.nextStatus); else box.checked = true; }));
  document.querySelector('#cancel-button')?.addEventListener('click', () => { if (!confirm('この予約をキャンセルしますか？')) return; fetch(`/api/staff/reservations/${encodeURIComponent(id)}/cancel`, {method:'PATCH'}).then(r => { if (!r.ok) throw Error(); return r.json(); }).then(render).catch(() => document.querySelector('#cancel-message').textContent = 'キャンセルに失敗しました。'); });
  document.querySelector('#delete-button')?.addEventListener('click', () => { if (!confirm('予約データを完全に削除しますか？')) return; fetch(`/api/staff/reservations/${encodeURIComponent(id)}`, {method:'DELETE'}).then(r => { if (r.status !== 204) throw Error(); location.href='/staff/reservations'; }).catch(() => document.querySelector('#delete-message').textContent = '削除に失敗しました。'); });
  adminBox.querySelector('#correction-button').addEventListener('click', () => { const reason = adminBox.querySelector('#correction-reason').value.trim(), msg = adminBox.querySelector('#correction-message'); if (!reason) { msg.textContent = '訂正理由を入力してください。'; return; } if (!confirm('訂正理由を確認しました。COMPLETEDからREADYへ訂正しますか？')) return; fetch(`/api/staff/reservations/${encodeURIComponent(id)}/admin-correction`, {method:'PATCH',headers:{'Content-Type':'application/json'},body:JSON.stringify({reason})}).then(r => { if (r.status === 403) throw Error('管理者権限が必要です。'); if (!r.ok) throw Error(); return r.json(); }).then(render).catch(e => msg.textContent = e.message || '訂正に失敗しました。'); });
})();
