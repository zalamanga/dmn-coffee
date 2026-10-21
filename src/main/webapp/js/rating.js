(() => {
    'use strict';
    const form = document.getElementById('rating-form');
    const feedback = document.getElementById('rating-feedback');
    const ratingTable = DMN.table();
    const saved = DMN.read('rating', null);
    if (saved && saved.table === DMN.table() && Number.isInteger(saved.rating) && saved.rating >= 1 && saved.rating <= 5) {
        form.querySelector(`input[value="${saved.rating}"]`).checked = true;
        feedback.textContent = `Your saved rating on this browser: ${saved.rating}/5.`;
    }
    form.addEventListener('submit', event => {
        event.preventDefault();
        if (!DMN.table()) { feedback.textContent = 'Choose a table before saving a rating.'; return; }
        if (ratingTable !== DMN.table()) { feedback.textContent = 'Your table changed in another page. Return to the cart before rating the new table.'; return; }
        const rating = Number(new FormData(form).get('rating'));
        if (!Number.isInteger(rating) || rating < 1 || rating > 5) return;
        if (DMN.write('rating', { table: DMN.table(), rating }) && DMN.write('cart', [])) {
            feedback.textContent = `Thank you. Your ${rating}/5 rating is saved on this browser. Your demo cart is now empty.`;
        }
    });
})();
