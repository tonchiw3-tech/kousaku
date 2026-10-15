const form = document.querySelector('#inquiry-form');
const result = document.querySelector('#result');

form.addEventListener('submit', async event => {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(form));

  try {
    const response = await fetch('/api/inquiries', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        customerName: data.customerName,
        email: data.email,
        category: data.category,
        message: data.message
      })
    });

    if (!response.ok) {
      const errorBody = await response.text();
      console.error('お問い合わせの保存に失敗しました', {
        status: response.status,
        body: errorBody
      });
      alert('送信に失敗しました。時間をおいて再度お試しください。');
      return;
    }

    const saved = await response.json();
    form.hidden = true;
    result.innerHTML = '<strong>お問い合わせを受け付けました。</strong><p>お問い合わせ番号：</p><code>' + saved.inquiryNumber + '</code><p>この番号は返信確認時に必要です。<br>大切に保管してください。</p><a class="button-link" href="/inquiries/status">返信を確認する</a>';
    result.hidden = false;
  } catch (error) {
    console.error('お問い合わせAPIへの接続に失敗しました', error);
    alert('送信に失敗しました。時間をおいて再度お試しください。');
  }
});
