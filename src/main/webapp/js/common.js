(() => {
    'use strict';
    const prefix = 'dmnCoffee.';
    let storageError = '';
    function warnStorage() {
        storageError = 'Browser storage is unavailable. Enable site storage to keep your table, cart and rating.';
        const message = document.getElementById('storage-error');
        if (message) { message.textContent = storageError; message.hidden = false; }
    }
    function read(key, fallback) {
        let raw;
        try { raw = localStorage.getItem(prefix + key); }
        catch { warnStorage(); return fallback; }
        if (raw === null) return fallback;
        try { return JSON.parse(raw); }
        catch {
            try { localStorage.removeItem(prefix + key); } catch { warnStorage(); }
            const message = document.getElementById('storage-error');
            if (message) { message.textContent = 'Saved demo data was unreadable and has been reset. Add your items again.'; message.hidden = false; }
            return fallback;
        }
    }
    function write(key, value) {
        try { localStorage.setItem(prefix + key, JSON.stringify(value)); return true; }
        catch { warnStorage(); return false; }
    }
    const validTable = value => Number.isInteger(Number(value)) && Number(value) >= 1 && Number(value) <= 999;
    function table() { const value = read('table', null); return validTable(value) ? Number(value) : null; }
    function cart() {
        const items = read('cart', []);
        if (!Array.isArray(items)) { write('cart', []); return []; }
        const valid = items.filter(item => item && Number.isInteger(item.id) && item.id > 0 &&
            typeof item.name === 'string' && item.name.length > 0 && item.name.length <= 120 &&
            Number.isFinite(item.price) && item.price >= 0 && Number.isInteger(item.quantity) && item.quantity > 0 && item.quantity <= 99);
        if (valid.length !== items.length) write('cart', valid);
        return valid;
    }
    function setTable(value) {
        if (!validTable(value)) return false;
        const oldTable = table();
        if (oldTable !== Number(value) && !write('cart', [])) return false;
        return write('table', Number(value));
    }
    function updateCount() {
        const count = document.getElementById('cart-count');
        if (count) count.textContent = cart().reduce((sum, item) => sum + item.quantity, 0);
    }
    window.DMN = { read, write, table, cart, setTable, updateCount, validTable,
        money: value => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 2 }).format(value) };
    const serverTable = document.body.dataset.tableNumber;
    if (serverTable) setTable(serverTable);
    function refreshHeader() {
        const selectedTable = validTable(serverTable) ? Number(serverTable) : table();
        document.querySelectorAll('.table-number').forEach(node => { node.textContent = selectedTable ? 'Table ' + selectedTable : 'No table selected'; });
        updateCount();
    }
    window.addEventListener('pageshow', refreshHeader);
    window.addEventListener('storage', refreshHeader);
    refreshHeader();
    if (storageError) warnStorage();
})();
