(function(){
  document.addEventListener('click',function(e){
    const b=e.target.closest('button,.btn,.primary-btn,.ghost-btn');
    if(!b || b.disabled) return;
    b.classList.remove('tap-feedback'); void b.offsetWidth; b.classList.add('tap-feedback');
  });
})();
