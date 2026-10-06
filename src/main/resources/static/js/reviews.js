/**
 * Function: Review and Feedback Management
 * Member Name: Anuththara K. G. H.
 * Student ID: IT25103422
 * Role: Product Owner
 * Course: SE2030 - Software Engineering (SLIIT)
 */

'use strict';

function reviewRows(reviews) {
  return reviews.map(r => `
    <article class="review-row">
      <div class="review-by">
        <strong>${esc(r.author_name)}</strong>
        ${rating(r.rating)}
      </div>
      <p>${esc(r.comment)}</p>
      <small class="muted">${niceDate(r.created_at)}</small>
    </article>
  `).join('') || '<p class="muted">No reviews yet.</p>';
}

async function reviewsPage() {
  state.reviews = await api('/api/reviews/mine');
  return heading('My reviews', 'The experiences you have shared with other travelers.') +
    (state.reviews.length ? state.reviews.map(r => `
      <article class="review-row">
        <div class="review-by">
          <strong>${esc(r.target_name)}</strong>
          ${tag(r.status)}
        </div>
        <p>${esc(r.comment)}</p>
        <div class="actions">
          ${rating(r.rating)}
          ${tool('edit-review', 'pencil', 'Edit review', `data-id="${r.id}"`)}
          ${tool('delete-review', 'trash-2', 'Delete review', `data-id="${r.id}"`)}
        </div>
      </article>
    `).join('') : empty('Every journey has a story', 'You can review a guide after a completed booking.', '<a class="btn secondary" href="#bookings">My bookings</a>'));
}

function reviewModal({ booking, place, review } = {}) {
  if (!needRole('TOURIST')) return;
  modal(review ? 'Edit review' : 'Share your experience', `
    <form id="review-form" data-id="${review?.id || ''}">
      <input type="hidden" name="booking_id" value="${booking || review?.booking_id || ''}">
      <input type="hidden" name="place_id" value="${place || review?.place_id || ''}">
      <label>Your rating</label>
      <div class="review-stars">
        ${[1, 2, 3, 4, 5].map(n => `
          <label>
            <input type="radio" name="rating" value="${n}" ${(review?.rating || 5) === n ? 'checked' : ''} required>
            ${n}
          </label>
        `).join('')}
      </div>
      <label for="comment">Your review</label>
      <textarea name="comment" id="comment" required maxlength="2000">${esc(review?.comment)}</textarea>
      <p class="form-error"></p>
      <button class="btn" type="submit">${review ? 'Save review' : 'Submit review'}</button>
    </form>
  `);
}

// Review Action Listeners
document.addEventListener('click', async event => {
  const el = event.target.closest('[data-action]');
  if (!el) return;
  const action = el.dataset.action;
  const id = Number(el.dataset.id);

  if (action === 'booking-review') {
    reviewModal({ booking: id });
  } else if (action === 'place-review') {
    reviewModal({ place: id });
  } else if (action === 'edit-review') {
    reviewModal({ review: state.reviews.find(r => r.id === id) });
  } else if (action === 'delete-review') {
    confirmAction('Delete this review?', 'This permanently removes the review and its rating.', async () => {
      await api(`/api/reviews/${id}`, { method: 'DELETE' });
      await render();
    });
  }
});

document.addEventListener('submit', async event => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;

  if (form.id === 'review-form') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    data.rating = Number(data.rating);
    data.booking_id = data.booking_id ? Number(data.booking_id) : null;
    data.place_id = data.place_id ? Number(data.place_id) : null;

    await api('/api/reviews' + (form.dataset.id ? '/' + form.dataset.id : ''), {
      method: form.dataset.id ? 'PUT' : 'POST',
      body: data
    });
    closeModal();
    await render();
    toast('Review saved');
  }
});
