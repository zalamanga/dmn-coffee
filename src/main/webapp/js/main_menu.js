(() => {
    'use strict';
    document.querySelectorAll('[data-add-item]').forEach(button => {
        button.addEventListener('click', () => {
            if (DMN.table() !== Number(document.body.dataset.tableNumber)) {
                document.getElementById('feedback').textContent = 'Your table changed in another page. Choose your table again before adding items.';
                return;
            }
            const items = DMN.cart();
            const id = Number(button.dataset.id);
            const item = items.find(entry => entry.id === id);
            if (item) item.quantity = Math.min(99, item.quantity + 1);
            else items.push({ id, name: button.dataset.name, price: Number(button.dataset.price), quantity: 1 });
            if (DMN.write('cart', items)) {
                DMN.updateCount();
                document.getElementById('feedback').textContent = button.dataset.name + ' added to your cart.';
            }
        });
    });
})();
