// patched
/* (() => {
  const detail = document.querySelector('#reservation-detail'), id = location.pathname.split('/').filter(Boolean).pop();
  const labels = { RECEIVED: '受付済み', CONFIRMED: '予約確定', COMPLETED: '対応完了', CANCELED: 'キャンセル' };
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
  const yen = v => v == null ? '-' : `${Number(v).toLocaleString('ja-JP')}円`, dt = v => v ? String(v).replace('T', ' ') : '-';
  fetch(`/api/staff/reservations/${encodeURIComponent(id)}`).then(r => { if (!r.ok) throw Error(`HTTP ${r.status}`); return r.json(); }).then(r => {
    const rows = [['予約番号',r.reservationNumber],['予約日',r.reservationDate],['予約時間',r.reservationTime],['人数',`${r.participants}名`],['名前',r.customerName],['メール',r.email],['年代',r.ageGroup],['初回 / リピート',r.visitType === 'FIRST' ? '初回' : 'リピート'],['単価',yen(r.unitPrice)],['合計料金',yen(r.totalPrice)],['ステータス',labels[r.status] || r.status],['登録日時',dt(r.createdAt)],['更新日時',dt(r.updatedAt)]];
    detail.innerHTML = rows.map(([l,v]) => `<div class="detail-field"><dt>${l}</dt><dd>${esc(v)}</dd></div>`).join('');
  }).catch(() => { detail.innerHTML = '<div class="no-data">予約詳細を取得できませんでした。</div>'; });
})(); */
(() => {
  const id = location.pathname.split('/').filter(Boolean).pop();
  const d = document.querySelector('#reservation-detail');
  if (!id || !d) return;
  fetch('/api/staff/reservations/' + encodeURIComponent(id)).then(r => r.json()).then(r => {
    const prep = ['READY','COMPLETED'].includes(r.status) ? '完了' : '未完了';
    d.innerHTML = '<div class="detail-field"><dt>予約ステータス</dt><dd>' + r.status + '</dd></div><div class="detail-field"><dt>準備状況</dt><dd>' + prep + '</dd></div>';
  });
})();
