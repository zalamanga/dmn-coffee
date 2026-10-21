(() => {
    'use strict';
    const form = document.getElementById('table-form');
    const input = document.getElementById('table-number');
    if (DMN.table()) input.value = DMN.table();
    if (new URLSearchParams(location.search).has('invalidTable')) document.getElementById('table-error').hidden = false;
    form.addEventListener('submit', event => {
        if (!DMN.validTable(input.value) || !DMN.setTable(input.value)) { event.preventDefault(); return; }
        input.value = String(Number(input.value));
        document.getElementById('loading-message').hidden = false;
        form.querySelector('button').disabled = true;
    });
    window.addEventListener('pageshow', () => {
        form.querySelector('button').disabled = false;
        document.getElementById('loading-message').hidden = true;
    });
})();
