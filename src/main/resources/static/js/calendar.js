/**
 * Function: Guide Calendar Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */

'use strict';

async function calendarPage() {
  state.slots = await api(`/api/public/guides/${state.user.id}/availability`);
  const month = state.month;
  const first = new Date(month.getFullYear(), month.getMonth(), 1);
  const days = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate();
  const cells = [];

  for (let i = 0; i < (first.getDay() + 6) % 7; i++) {
    cells.push('<div class="day past"></div>');
  }

  for (let d = 1; d <= days; d++) {
    const date = dateISO(new Date(month.getFullYear(), month.getMonth(), d));
    const slot = state.slots.find(s => s.available_date === date);
    const status = slot?.status || '';
    cells.push(`
      <button class="day ${status.toLowerCase()} ${date < today() ? 'past' : ''}"
        data-action="slot" data-date="${date}" data-value="${status}"
        ${date < today() || status === 'BOOKED' ? 'disabled' : ''}
        aria-label="${date}: ${status || 'Unavailable'}">
        <span>${d}</span>
        ${status ? tag(status) : ''}
      </button>
    `);
  }

  return heading('My availability', 'Keep your calendar ready for the next journey.', button('add-availability', icon('plus') + ' Add dates')) +
    `<div class="calendar-toolbar">
      <h2>${month.toLocaleDateString('en-GB', { month: 'long', year: 'numeric' })}</h2>
      <div class="actions">
        ${tool('month-prev', 'chevron-left', 'Previous month')}
        ${button('month-today', 'Today', '', 'compact secondary')}
        ${tool('month-next', 'chevron-right', 'Next month')}
      </div>
    </div>
    <div class="calendar">
      ${['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(d => `<div class="weekday">${d}</div>`).join('')}
      ${cells.join('')}
    </div>
    <div class="legend">
      <span><b></b>Available</span>
      <span><b class="booked"></b>Confirmed booking</span>
      <span>Unmarked dates are unavailable</span>
    </div>`;
}

// Calendar Action Listeners
document.addEventListener('click', async event => {
  const el = event.target.closest('[data-action]');
  if (!el) return;
  const action = el.dataset.action;

  if (action === 'month-prev') {
    state.month = new Date(state.month.getFullYear(), state.month.getMonth() - 1, 1);
    await render();
  } else if (action === 'month-next') {
    state.month = new Date(state.month.getFullYear(), state.month.getMonth() + 1, 1);
    await render();
  } else if (action === 'month-today') {
    state.month = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
    await render();
  } else if (action === 'slot') {
    const date = el.dataset.date;
    const value = el.dataset.value;
    await api(value === 'AVAILABLE' ? `/api/availability/${date}` : '/api/availability', {
      method: value === 'AVAILABLE' ? 'DELETE' : 'POST',
      body: value === 'AVAILABLE' ? undefined : { date }
    });
    await render();
  } else if (action === 'add-availability') {
    modal('Add available dates', `
      <form id="availability-form">
        <div class="form-grid">
          ${field('start', 'From', today(), 'date', `required min="${today()}"`)}
          ${field('end', 'To', today(), 'date', `required min="${today()}"`)}
        </div>
        <p class="form-error"></p>
        <button class="btn" type="submit">Add dates</button>
      </form>
    `);
  }
});

document.addEventListener('submit', async event => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;

  if (form.id === 'availability-form') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    const start = new Date(data.start + 'T12:00:00');
    const end = new Date(data.end + 'T12:00:00');

    if (end < start || (end - start) / 86400000 > 89) {
      throw new Error('Choose a range of 1 to 90 days.');
    }

    for (let date = new Date(start); date <= end; date.setDate(date.getDate() + 1)) {
      await api('/api/availability', { method: 'POST', body: { date: dateISO(date) } });
    }
    closeModal();
    await render();
    toast('Availability updated');
  }
});
