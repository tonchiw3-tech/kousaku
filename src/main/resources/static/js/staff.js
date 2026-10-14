(() => {
  const list = document.querySelector('#reservation-list');
  const cards = document.querySelector('#reservation-cards');
  const count = document.querySelector('#reservation-count');
  const error = document.querySelector('#error-message');
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
  const yen = (v) => v == null ? '' : `${Number(v).toLocaleString('ja-JP')}円`;
  const status = (v) => { const raw = String(v ?? ''); const labels = { RECEIVED:'受付済み', CONFIRMED:'予約確定', COMPLETED:'対応完了', CANCELED:'キャンセル' }; return `<span class="status-badge status-${esc(raw.toLowerCase())}">${esc(labels[raw] || '未設定')}</span>`; };
  const url = (id) => `/staff/reservations/${encodeURIComponent(id)}`;
  const field = (label, value, cls = '') => `<div class="reservation-field ${cls}"><dt>${label}</dt><dd>${value}</dd></div>`;
  fetch('/api/staff/reservations').then((r) => { if (!r.ok) throw new Error(); return r.json(); }).then((rows) => {
    count.textContent = `予約件数：${rows.length}件`;
    list.innerHTML = rows.length ? rows.map((r) => `<tr><td class="reservation-number">${esc(r.reservationNumber)}</td><td>${esc(r.reservationDate)}</td><td>${esc(r.reservationTime)}</td><td class="number-cell">${esc(r.participants)}名</td><td>${esc(r.customerName)}</td><td class="email-cell">${esc(r.email)}</td><td>${r.visitType === 'FIRST' ? '初回' : 'リピート'}</td><td class="price-cell">${yen(r.totalPrice)}</td><td>${status(r.status)}</td><td><a class="detail-button" href="${url(r.id)}">詳細を見る</a></td></tr>`).join('') : '<tr><td colspan="10">予約はありません。</td></tr>';
    cards.innerHTML = rows.length ? rows.map((r) => `<article class="reservation-card"><dl>${field('予約番号', esc(r.reservationNumber), 'reservation-number-field')}${field('予約日', esc(r.reservationDate))}${field('予約時間', esc(r.reservationTime))}${field('名前', esc(r.customerName))}${field('人数', `${esc(r.participants)}名`)}${field('ステータス', status(r.status))}</dl><a class="detail-button" href="${url(r.id)}">詳細を見る</a></article>`).join('') : '<p class="no-data">予約はありません。</p>';
  }).catch(() => { list.innerHTML = ''; cards.innerHTML = ''; error.hidden = false; });
})();
