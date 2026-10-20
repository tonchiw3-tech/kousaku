(() => {
  const list = document.querySelector('#reservation-list');
  const cards = document.querySelector('#reservation-cards');
  const count = document.querySelector('#reservation-count');
  const error = document.querySelector('#error-message');
  const emailHeader = document.querySelector('.staff-table thead th:nth-child(6)');
  if (emailHeader) emailHeader.remove();
  const loadingCell = list.querySelector('td[colspan="10"]');
  if (loadingCell) loadingCell.colSpan = 9;
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
  const yen = (v) => v == null ? '' : `${Number(v).toLocaleString('ja-JP')}円`;
  const status = (v, r) => { const raw = String(v ?? ''); const labels = { RECEIVED:'申込受付', CONFIRMED:'予約確認済み', READY:'受け入れ準備完了', COMPLETED:'体験完了', CANCELED:'キャンセル' }; const future = r && new Date(`${r.reservationDate}T${String(r.reservationTime).slice(0, 8)}+09:00`).getTime() > Date.now(); const next = { RECEIVED:'次：予約内容を確認', CONFIRMED:'次：受け入れ準備', READY:future?'予約開始後に体験完了':'次：体験完了', COMPLETED:'対応完了', CANCELED:'対応不要' }; return `<span class="status-stack" style="display:inline-flex;flex-direction:column;align-items:flex-start;gap:4px"><span class="status-badge status-${esc(raw.toLowerCase())}">${esc(labels[raw] || '未設定')}</span><small class="status-next" style="color:var(--muted);font-size:11px;line-height:1.3;white-space:nowrap">${esc(next[raw] || '')}</small></span>`; };
  const url = (id) => `/staff/reservations/${encodeURIComponent(id)}`;
  const field = (label, value, cls = '') => `<div class="reservation-field ${cls}"><dt>${label}</dt><dd>${value}</dd></div>`;
  fetch('/api/staff/reservations', { cache: 'no-store' }).then((r) => { if (!r.ok) throw new Error(); return r.json(); }).then((rows) => {
    count.textContent = `予約件数：${rows.length}件`;
    list.innerHTML = rows.length ? rows.map((r) => `<tr><td class="reservation-number">${esc(r.reservationNumber)}</td><td>${esc(r.reservationDate)}</td><td>${esc(r.reservationTime)}</td><td class="number-cell">${esc(r.participants)}名</td><td>${esc(r.customerName)}</td><td>${r.visitType === 'FIRST' ? '初回' : 'リピート'}</td><td class="price-cell">${yen(r.totalPrice)}</td><td>${status(r.status, r)}</td><td><a class="detail-button" href="${url(r.id)}">詳細を見る</a></td></tr>`).join('') : '<tr><td colspan="9">予約はありません。</td></tr>';
    cards.innerHTML = rows.length ? rows.map((r) => `<article class="reservation-card"><dl>${field('予約番号', esc(r.reservationNumber), 'reservation-number-field')}${field('予約日', esc(r.reservationDate))}${field('予約時間', esc(r.reservationTime))}${field('名前', esc(r.customerName))}${field('人数', `${esc(r.participants)}名`)}${field('ステータス', status(r.status))}</dl><a class="detail-button" href="${url(r.id)}">詳細を見る</a></article>`).join('') : '<p class="no-data">予約はありません。</p>';
  }).catch(() => { list.innerHTML = ''; cards.innerHTML = ''; error.hidden = false; });
  window.addEventListener('pageshow', event => { if (event.persisted) window.location.reload(); });
  window.addEventListener('storage', event => { if (event.key === 'kousaku-reservation-updated') window.location.reload(); });
})();
