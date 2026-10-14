(() => {
  const list = document.querySelector('#reservation-list');
  const count = document.querySelector('#reservation-count');
  const error = document.querySelector('#error-message');
  const esc = (value) => String(value ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const yen = (value) => value == null ? '' : `${Number(value).toLocaleString('ja-JP')}円`;
  const statusLabels = { RECEIVED: '受付済み' };
  const status = (value) => {
    const raw = String(value ?? '');
    return `<span class="status-badge status-${esc(raw.toLowerCase())}">${esc(statusLabels[raw] || raw || '未設定')}</span>`;
  };
  fetch('/api/staff/reservations')
    .then((response) => { if (!response.ok) throw new Error(); return response.json(); })
    .then((rows) => {
      count.textContent = `予約件数：${rows.length}件`;
      list.innerHTML = rows.length ? rows.map((row) => `<tr><td class="reservation-number">${esc(row.reservationNumber)}</td><td class="nowrap">${esc(row.reservationDate)}</td><td class="nowrap">${esc(row.reservationTime)}</td><td class="number-cell">${esc(row.participants)}名</td><td>${esc(row.customerName)}</td><td class="email-cell">${esc(row.email)}</td><td class="nowrap">${row.visitType === 'FIRST' ? '初回' : 'リピート'}</td><td class="price-cell">${yen(row.totalPrice)}</td><td>${status(row.status)}</td></tr>`).join('') : '<tr><td colspan="9">予約はありません。</td></tr>';
    })
    .catch(() => { list.innerHTML = ''; error.hidden = false; });
})();
