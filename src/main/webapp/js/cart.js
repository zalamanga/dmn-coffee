(() => {
    'use strict';
    const container = document.getElementById('cart-items');
    const checkout = document.getElementById('checkout-button');
    const continueLink = document.getElementById('continue-ordering');
    function render() {
        continueLink.href = DMN.table() ? 'MenuItemsServlet?tableNumber=' + DMN.table() : 'index.html';
        const items = DMN.cart();
        container.replaceChildren();
        let total = 0;
        for (const item of items) {
            const row = document.createElement('div');
            row.className = 'cart-item';
            const label = document.createElement('span');
            label.textContent = `${item.name} × ${item.quantity} · ${DMN.money(item.price * item.quantity)}`;
            const remove = document.createElement('button');
            remove.type = 'button'; remove.dataset.removeItem = item.id;
            remove.textContent = 'Remove'; remove.setAttribute('aria-label', 'Remove ' + item.name);
            remove.addEventListener('click', () => {
                if (DMN.write('cart', DMN.cart().filter(entry => entry.id !== item.id))) {
                    render(); document.getElementById('feedback').textContent = item.name + ' removed.';
                }
            });
            row.append(label, remove); container.append(row);
            total += item.price * item.quantity;
        }
        if (!items.length) container.textContent = 'Your cart is empty. Open the menu to add an item.';
        document.getElementById('total-price').textContent = 'Total: ' + DMN.money(total);
        checkout.disabled = !items.length || !DMN.table();
    }
    checkout.addEventListener('click', () => { if (DMN.cart().length && DMN.table()) location.href = 'rating.html'; });
    window.addEventListener('pageshow', render);
    window.addEventListener('storage', render);
    render();
})();
