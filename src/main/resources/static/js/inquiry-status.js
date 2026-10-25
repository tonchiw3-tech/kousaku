const form=document.querySelector('#status-form'); const result=document.querySelector('#status-result');
const labels={PRICE:'料金',AVAILABILITY:'空き状況',SOLO:'一人利用',DURATION:'所要時間',ACCESS:'アクセス',RAINY:'雨天時',FRAGRANCE:'香りについて',OTHER:'その他'}; const statuses={NEW:'未対応',IN_PROGRESS:'対応中',REPLIED:'返信済み'};
form.addEventListener('submit',async e=>{e.preventDefault(); result.hidden=true; const number=new FormData(form).get('inquiryNumber').trim(); try {const r=await fetch(`/api/inquiries/status?inquiryNumber=${encodeURIComponent(number)}`); if(!r.ok){result.textContent='お問い合わせ番号が見つかりませんでした。番号をご確認ください。';result.hidden=false;return;} const i=await r.json(); const notice=i.status==='NEW'?'お問い合わせを受け付けています。スタッフが確認するまでお待ちください。':i.status==='IN_PROGRESS'?'現在スタッフが確認・対応しています。':'';
    const createTextElement=(tag,text,className)=>{const element=document.createElement(tag); if(className) element.className=className; element.textContent=text; return element;};
    const details=document.createElement('dl');
    [['お問い合わせ番号',i.inquiryNumber],['受付日時',i.createdAt?.replace('T',' ')||'-'],['カテゴリ',labels[i.category]||i.category],['お問い合わせ内容',i.message],['対応状況',statuses[i.status]||i.status]].forEach(([label,value])=>{details.append(createTextElement('dt',label),createTextElement('dd',value));});
    const content=[details];
    if(notice) content.push(createTextElement('p',notice,'notice'));
    if(i.status==='REPLIED'){const reply=document.createElement('section'); reply.className='reply-card'; reply.append(createTextElement('h2','スタッフからの返信'),createTextElement('p',i.replyMessage||''),createTextElement('small',`返信日時：${i.repliedAt?.replace('T',' ')||'-'}`)); content.push(reply);}
    result.replaceChildren(...content); result.hidden=false;}catch(_){result.textContent='確認に失敗しました。時間をおいて再度お試しください。';result.hidden=false;}});
