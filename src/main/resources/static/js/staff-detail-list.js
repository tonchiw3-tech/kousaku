(() => {
  const list = document.querySelector('#reservation-list'), count = document.querySelector('#reservation-count'), error = document.querySelector('#error-message');
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
  const yen = (v) => v == null ? '' : `${Number(v).toLocaleString('ja-JP')}円`;
  const status = (v) => `<span class="status-badge status-${esc(String(v ?? '').toLowerCase())}">${esc(v === 'RECEIVED' ? '受付済み' : v || '未設定')}</span>`;
  fetch('/api/staff/reservations').then((r) => { if (!r.ok) throw new Error(); return r.json(); }).then((rows) => {
    count.textContent = `予約件数：${rows.length}件`;
    list.innerHTML = rows.length ? rows.map((row) => `<tr><td class="reservation-number">${esc(row.reservationNumber)}</td><td>${esc(row.reservationDate)}</td><td>${esc(row.reservationTime)}</td><td class="number-cell">${esc(row.participants)}名</td><td>${esc(row.customerName)}</td><td class="email-cell">${esc(row.email)}</td><td>${row.visitType === 'FIRST' ? '初回' : 'リピート'}</td><td class="price-cell">${yen(row.totalPrice)}</td><td>${status(row.status)}</td><td><a class="detail-button" href="/staff/reservations/${encodeURIComponent(row.id)}">詳細</a></td></tr>`).join('') : '<tr><td colspan="10">予約はありません。</td></tr>';
  }).catch(() => { list.innerHTML = ''; error.hidden = false; });
})();
