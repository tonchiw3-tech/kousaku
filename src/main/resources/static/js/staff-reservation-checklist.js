(() => {
  const id = decodeURIComponent(location.pathname.split('/').filter(Boolean).pop() || '');
  const labels = { RECEIVED: '申込受付', CONFIRMED: '予約確認済み', READY: '受け入れ準備完了', COMPLETED: 'ワークショップ実施済み', CANCELED: 'キャンセル済み' };
  const detail = document.querySelector('#reservation-detail');
  const message = document.querySelector('#status-message');
  const cancelButton = document.querySelector('#cancel-button');
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
  const headers = () => ({ 'Content-Type': 'application/json', ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {}) });
  let reservation;
  let busy = false;
  let messageTimer;

  const escapeHtml = value => String(value ?? '-').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
  const future = value => value.reservationDate && value.reservationTime && new Date(`${value.reservationDate}T${String(value.reservationTime).slice(0, 8)}+09:00`).getTime() > Date.now();

  function showMessage(text, type, clearAfter) {
    window.clearTimeout(messageTimer);
    message.textContent = text;
    message.className = `action-message save-message${type ? ` is-${type}` : ''}`;
    if (clearAfter) {
      messageTimer = window.setTimeout(() => {
        message.textContent = '';
        message.className = 'action-message save-message';
      }, clearAfter);
    }
  }

  function notifyReservationUpdated(value) {
    const update = JSON.stringify({ id: value.id, status: value.status, updatedAt: Date.now() });
    try { localStorage.setItem('kousaku-reservation-updated', update); } catch (_) { /* Storage can be unavailable. */ }
    window.dispatchEvent(new CustomEvent('kousaku:reservation-updated', { detail: value }));
  }

  function render(value) {
    reservation = value;
    const status = value.status;
    const checks = [['CONFIRMED', 'READY', 'COMPLETED'].includes(status), ['READY', 'COMPLETED'].includes(status), status === 'COMPLETED'];
    document.querySelector('#current-status').textContent = labels[status] || status;
    detail.innerHTML = `<div class="detail-field"><dt>予約番号</dt><dd>${escapeHtml(value.reservationNumber)}</dd></div><div class="detail-field"><dt>予約日</dt><dd>${escapeHtml(value.reservationDate)}</dd></div><div class="detail-field"><dt>お客様</dt><dd>${escapeHtml(value.customerName)}</dd></div><div class="detail-field"><dt>ステータス</dt><dd>${escapeHtml(labels[status] || status)}</dd></div>`;
    document.querySelectorAll('.progress-checkbox').forEach((box, index) => {
      box.checked = checks[index];
      const disabled = busy || status === 'CANCELED' || (index === 1 && !checks[0]) || (index === 2 && (!checks[1] || future(value)));
      box.disabled = disabled;
      box.closest('li').classList.toggle('is-checked', box.checked);
      box.closest('li').classList.toggle('is-disabled', disabled);
    });
    const hint = document.querySelector('#completed-hint');
    if (hint) hint.hidden = !(future(value) && status !== 'COMPLETED');
    const actions = document.querySelector('#reservation-actions');
    if (actions) actions.hidden = status === 'CANCELED' || status === 'COMPLETED';
  }

  async function patch(endpoint, body) {
    const response = await fetch(`/api/staff/reservations/${encodeURIComponent(id)}/${endpoint}`, { method: 'PATCH', headers: headers(), body: JSON.stringify(body) });
    if (!response.ok) throw new Error();
    return response.json();
  }

  async function save(status) {
    const previous = reservation;
    const optimistic = { ...previous, status };
    busy = true;
    render(optimistic);
    showMessage('保存中…', 'saving');
    try {
      const saved = await patch('status', { status });
      render(saved);
      notifyReservationUpdated(saved);
      showMessage('✓ 保存しました', 'success', 3000);
    } catch (_) {
      render(previous);
      showMessage('保存できませんでした', 'error');
    } finally {
      busy = false;
      render(reservation);
    }
  }

  async function correctCompletion() {
    const reason = window.prompt('実施済みを取り消す訂正理由を入力してください（必須）');
    if (reason === null) { render(reservation); return; }
    if (!reason.trim()) { message.textContent = '訂正理由は必須です。'; render(reservation); return; }
    const previous = reservation;
    busy = true; render(reservation); showMessage('保存中…', 'saving');
    try {
      const saved = await patch('correction', { reason: reason.trim() });
      render(saved); notifyReservationUpdated(saved); showMessage('✓ 保存しました', 'success', 3000);
    }
    catch (_) { render(previous); showMessage('保存できませんでした', 'error'); }
    finally { busy = false; render(reservation); }
  }

  document.querySelectorAll('.progress-checkbox').forEach((box, index) => box.addEventListener('change', () => {
    if (busy || !reservation) return;
    if (index === 2 && box.checked) { save('COMPLETED'); return; }
    if (index === 2 && !box.checked && reservation.status === 'COMPLETED') { correctCompletion(); return; }
    save(index === 0 ? (box.checked ? 'CONFIRMED' : 'RECEIVED') : (box.checked ? 'READY' : 'CONFIRMED'));
  }));

  cancelButton?.addEventListener('click', async () => {
    if (!window.confirm('この予約をキャンセルしますか？')) return;
    busy = true; render(reservation);
    try { render(await patch('cancel', {})); }
    catch (_) { document.querySelector('#cancel-message').textContent = 'キャンセルに失敗しました。'; }
    finally { busy = false; render(reservation); }
  });

  fetch(`/api/staff/reservations/${encodeURIComponent(id)}`).then(response => {
    if (!response.ok) throw new Error();
    return response.json();
  }).then(render).catch(() => { message.textContent = '予約詳細を取得できませんでした。'; });
})();
