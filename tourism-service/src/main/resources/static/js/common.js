/* Shared Ampara Tourism UI shell, i18n and lightweight UX animations. */
(function () {
  const SUPPORTED = ['en', 'ta', 'si'];
  const LABELS = { en: 'English', ta: 'தமிழ்', si: 'සිංහල' };
  const PAGES = [
    { href: '/map.html', key: 'nav.places', fallback: 'Explore' },
    { href: '/town.html?town=Pottuvil', key: 'nav.townGuide', fallback: 'Town Guide' },
    { href: '/events.html', key: 'nav.events', fallback: 'Events' },
    { href: '/analytics.html', key: 'nav.analytics', fallback: 'Analytics' },
  ];
  function currentLang(){ return localStorage.getItem('lang') || 'en'; }
  function setLang(lang){ localStorage.setItem('lang',lang); applyTranslations(); document.dispatchEvent(new CustomEvent('ampara:langchange',{detail:lang})); }
  let dictCache={};
  async function applyTranslations(){
    const lang=currentLang();
    try{const res=await fetch('/api/i18n/'+lang);dictCache=res.ok?await res.json():{};}catch(e){dictCache={};}
    document.querySelectorAll('[data-i18n]').forEach(el=>{const key=el.getAttribute('data-i18n');if(dictCache[key])el.textContent=dictCache[key];});
    document.querySelectorAll('.lang-btn').forEach(btn=>btn.classList.toggle('active',btn.dataset.lang===lang));
  }
  function renderNav(activeHref){
    const el=document.getElementById('app-nav');if(!el)return;
    const lang=currentLang();
    const links=PAGES.map(p=>`<a href="${p.href}" data-i18n="${p.key}" class="${p.href===activeHref?'active':''}">${p.fallback}</a>`).join('');
    const switcher=SUPPORTED.map(l=>`<button class="lang-btn${l===lang?' active':''}" data-lang="${l}">${LABELS[l]}</button>`).join('');
    el.innerHTML=`<span class="brand" data-i18n="app.name">Ampara Tourism</span><div class="nav-links">${links}</div><div class="lang-switcher">${switcher}</div>`;
    el.querySelectorAll('.lang-btn').forEach(btn=>btn.addEventListener('click',()=>setLang(btn.dataset.lang)));
    el.querySelectorAll('.nav-links a').forEach(a=>a.addEventListener('click',()=>{document.body.classList.add('page-leaving');setTimeout(()=>document.body.classList.remove('page-leaving'),260)}));
  }
  function initMotion(){
    const els=document.querySelectorAll('.item,.card,.stat,.chart-box,.place-card,.section');
    if(!('IntersectionObserver' in window)){els.forEach(e=>e.classList.add('in-view'));return;}
    const io=new IntersectionObserver(entries=>entries.forEach(entry=>{if(entry.isIntersecting){entry.target.classList.add('in-view');io.unobserve(entry.target)}}),{threshold:.08});
    els.forEach(e=>io.observe(e));
  }
  window.AmparaCommon={currentLang,setLang,applyTranslations,renderNav,t:k=>dictCache[k]};
  document.addEventListener('DOMContentLoaded',()=>{applyTranslations();initMotion();});
})();
