/**
 * Function: Booking Management
 * Member Name: Samarawickrama S. J. D.
 * Student ID: IT25102542
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */

'use strict';

async function bookingsPage() {
  state.bookings = await api('/api/bookings');
  const guide = state.user.role === 'GUIDE';
  const b = state.bookings;

  return heading(
    guide ? 'Your bookings' : 'My journeys',
    guide ? 'Review requests and keep every trip on track.' : 'Your plans, from the first request to the last memory.',
    `<a class="btn secondary" href="#guides">${icon('plus')} Plan a trip</a>`
  ) +
  `<div class="stats">
    ${[
      ['All bookings', b.length],
      ['Awaiting confirmation', b.filter(x => x.status === 'PENDING').length],
      ['Confirmed', b.filter(x => x.status === 'CONFIRMED').length],
      ['Completed', b.filter(x => x.status === 'COMPLETED').length]
    ].map(([l, n]) => `<div class="stat"><strong>${n}</strong><span>${l}</span></div>`).join('')}
  </div>` +
  (b.length ? `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Journey</th>
            <th>${guide ? 'Traveler' : 'Guide'}</th>
            <th>Dates</th>
            <th>Total</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${b.map(x => `
            <tr>
              <td>
                <strong>${esc(x.place_name)}</strong>
                <small>#CT-${String(x.id).padStart(4, '0')} / ${x.guests} guest${x.guests > 1 ? 's' : ''}</small>
              </td>
              <td>${esc(guide ? x.tourist_name : x.guide_name)}</td>
              <td>${niceDate(x.start_date)}${x.start_date !== x.end_date ? '<br>' + niceDate(x.end_date) : ''}</td>
              <td>${money(x.total_price)}</td>
              <td>${tag(x.status)}</td>
              <td>
                <div class="actions">
                  ${tool('booking-detail', 'eye', 'View booking', `data-id="${x.id}"`)}
                  ${guide && x.status === 'PENDING' ? button('booking-status', 'Accept', `data-id="${x.id}" data-status="CONFIRMED"`, 'compact') + button('booking-status', 'Decline', `data-id="${x.id}" data-status="REJECTED"`, 'compact secondary') : ''}
                  ${['PENDING', 'CONFIRMED'].includes(x.status) ? tool('booking-status', 'x', 'Cancel booking', `data-id="${x.id}" data-status="CANCELLED"`) : ''}
                  ${guide && x.status === 'CONFIRMED' ? button('booking-status', 'Complete', `data-id="${x.id}" data-status="COMPLETED"`, 'compact secondary') : ''}
                  ${!guide && x.status === 'COMPLETED' && !x.review_id ? button('booking-review', 'Review', `data-id="${x.id}"`, 'compact secondary') : ''}
                </div>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  ` : empty('Your next journey is waiting', 'Find a local guide to plan your first trip.'));
}

async function bookingModal(id) {
  if (!needRole('TOURIST')) return;
  const [g, places, slots] = await Promise.all([
    api(`/api/public/guides/${id}`),
    api('/api/public/places'),
    api(`/api/public/guides/${id}/availability`)
  ]);

  const available = slots.filter(s => s.status === 'AVAILABLE').map(s => s.available_date);
  if (!available.length) {
    toast('This guide has no available dates yet.');
    return;
  }

  modal('Plan your journey', `
    <p class="subtitle">With ${esc(g.name)} / ${money(g.daily_rate)} per day</p>
    <form id="booking-form" data-guide="${id}">
      <div class="form-grid">
        <div class="full">
          <label for="place_id">Destination</label>
          <select name="place_id" id="place_id" required>
            ${places.map(p => `<option value="${p.id}">${esc(p.name)}</option>`).join('')}
          </select>
        </div>
        ${field('start_date', 'Start date', available[0], 'date', `required min="${today()}"`)}
        ${field('end_date', 'End date', available[0], 'date', `required min="${today()}"`)}
        ${field('guests', 'Guests', 1, 'number', 'required min="1" max="20"')}
        <div class="full">
          <label for="details">Trip details</label>
          <textarea name="details" id="details" maxlength="2000" placeholder="Your interests, meeting point, or anything your guide should know"></textarea>
        </div>
      </div>
      <p class="note">Available dates: ${available.slice(0, 15).map(niceDate).join(', ')}${available.length > 15 ? ' and more.' : ''}</p>
      <p class="note">A request does not reserve dates until the guide accepts. Guide fees only; no online payment is collected.</p>
      <p class="form-error"></p>
      <button class="btn" type="submit">Send booking request</button>
    </form>
  `);
}

// Booking Event Listeners
document.addEventListener('click', async event => {
  const el = event.target.closest('[data-action]');
  if (!el) return;
  const action = el.dataset.action;
  const id = Number(el.dataset.id);

  if (action === 'book') {
    await bookingModal(id);
  } else if (action === 'booking-detail') {
    const b = state.bookings.find(b => b.id === id);
    if (!b) return;
    modal('Booking #CT-' + String(id).padStart(4, '0'), `
      <div class="actions">${tag(b.status)}</div>
      ${section(esc(b.place_name))}
      <p>${esc(b.tourist_name)} with ${esc(b.guide_name)}</p>
      <p>${niceDate(b.start_date)} to ${niceDate(b.end_date)} / ${b.guests} guests</p>
      <p style="margin-top:15px">${esc(b.details || 'No additional details.')}</p>
      <p style="margin-top:15px"><strong>${money(b.total_price)}</strong></p>
      <p class="note">Guide fees only. No payment has been collected through this application.</p>
    `);
  } else if (action === 'booking-status') {
    const nextStatus = el.dataset.status;
    confirmAction('Update booking?', `Set booking #${id} to ${nextStatus.toLowerCase()}?`, async () => {
      await api(`/api/bookings/${id}`, { method: 'PATCH', body: { status: nextStatus } });
      await render();
    });
  }
});

document.addEventListener('submit', async event => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;

  if (form.id === 'booking-form') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    data.guide_id = Number(form.dataset.guide);
    data.place_id = Number(data.place_id);
    data.guests = Number(data.guests);

    await api('/api/bookings', { method: 'POST', body: data });
    closeModal();
    location.hash = 'bookings';
    await render();
    toast('Booking request sent');
  }
});
