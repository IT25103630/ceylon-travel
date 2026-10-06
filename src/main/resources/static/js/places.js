/**
 * Function: Places Management
 * Member Name: Fernando M. G. D. W.
 * Student ID: IT25101548
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */

'use strict';

function placeCard(p) {
  return `
    <article class="place-card">
      <div class="place-photo">
        <a class="photo-link" href="#place/${p.id}">
          <img src="${esc(p.image_url)}" alt="${esc(p.name)}" loading="lazy">
        </a>
        <span class="tag">${esc(p.category)}</span>
      </div>
      <div class="card-body">
        <h3><a href="#place/${p.id}">${esc(p.name)}</a></h3>
        <div class="location">${icon('map-pin')}${esc(p.location)}</div>
        <p class="card-desc">${esc(p.description)}</p>
        <div class="card-bottom">
          ${rating(p.rating)}
          <a class="text-button" href="#place/${p.id}">Explore place ${icon('arrow-up-right')}</a>
        </div>
      </div>
    </article>
  `;
}

function guideCard(g) {
  return `
    <article class="guide-card">
      <div class="guide-top">
        <span class="avatar">${esc(initials(g.name))}</span>
        <div>
          <h3><a href="#guide/${g.user_id}">${esc(g.name)}</a></h3>
          <div class="guide-meta">${icon('badge-check')}Verified local guide</div>
        </div>
      </div>
      <div class="location">${icon('map-pin')}${esc(g.location)}</div>
      <p class="card-desc">${esc(g.bio)}</p>
      <div class="location">${icon('languages')}${esc(g.languages)}</div>
      <div class="card-bottom">
        <span class="price">${money(g.daily_rate)} <small>/ day</small></span>
        <a class="text-button" href="#guide/${g.user_id}">View guide ${icon('arrow-up-right')}</a>
      </div>
    </article>
  `;
}

async function discover() {
  const [places, guides] = await Promise.all([
    api(`/api/public/places?q=${encodeURIComponent(state.q)}&category=${encodeURIComponent(state.category)}`),
    api('/api/public/guides')
  ]);
  state.places = places;
  state.guides = guides;

  return heading('Ceylon Travel', 'Find your kind of Sri Lanka. Go a little further with a local.', `<div class="date-chip">${icon('map-pin')} An island full of possibilities</div>`) +
    `<form id="search" class="searchbar">
      ${icon('search')}
      <input name="q" aria-label="Search destinations" placeholder="Where would you like to go?" value="${esc(state.q)}">
      <select name="category" aria-label="Destination category">
        ${['', 'Heritage', 'Nature', 'Beach', 'Adventure', 'City'].map(c => `
          <option value="${c}" ${c === state.category ? 'selected' : ''}>${c || 'All categories'}</option>
        `).join('')}
      </select>
      <button type="submit" class="btn">Search ${icon('arrow-right')}</button>
    </form>
    <div class="filters" aria-label="Filter places">
      ${[
        ['', 'layout-grid', 'All places'],
        ['Heritage', 'landmark', 'Culture & heritage'],
        ['Nature', 'trees', 'Nature'],
        ['Beach', 'waves', 'Beaches'],
        ['Adventure', 'mountain', 'Adventure'],
        ['City', 'building-2', 'City life']
      ].map(([value, i, label]) => `
        <button class="filter ${state.category === value ? 'active' : ''}" data-action="category" data-value="${value}">
          ${icon(i)}${label}
        </button>
      `).join('')}
    </div>` +
    section(`Places to discover <span class="count">${places.length} destinations</span>`, 'A new perspective, around every corner.') +
    `<div class="cards">
      ${places.map(placeCard).join('') || empty('No places found', 'Try a different location or category.', button('reset-search', 'Clear filters', '', 'secondary'))}
    </div>` +
    section('Meet your local guides', 'People who know the places, and the stories behind them.', '<a class="text-button" href="#guides">All guides ' + icon('arrow-right') + '</a>') +
    `<div class="guide-list">${guides.slice(0, 3).map(guideCard).join('')}</div>` +
    `<div class="callout">
      <div>
        <h3>Know a place worth sharing?</h3>
        <p>Add your local discoveries to the Ceylon Travel community.</p>
      </div>
      ${button('new-place', icon('plus') + ' Share a place', '', 'secondary')}
    </div>`;
}

async function guidesPage() {
  state.guides = await api(`/api/public/guides?q=${encodeURIComponent(state.q)}`);
  return heading('Local guides', 'A familiar face in a new place.') +
    `<form id="guide-search" class="searchbar">
      ${icon('search')}
      <input name="q" value="${esc(state.q)}" aria-label="Search guides" placeholder="Search by name, location, or language">
      <button class="btn">Find a guide</button>
    </form>
    <div class="guide-list">
      ${state.guides.map(guideCard).join('') || empty('No guides found', 'Try a different name, language, or location.')}
    </div>`;
}

async function placePage(id) {
  const [p, reviews, gallery] = await Promise.all([
    api(`/api/public/places/${id}`),
    api(`/api/public/reviews?place=${id}`),
    api(`/api/public/gallery?place=${id}`)
  ]);
  state.currentPlace = p;

  return `<a class="text-button" href="#discover">${icon('arrow-left')} All destinations</a>` +
    heading(p.name, p.location, tag(p.category)) +
    `<img class="detail-image" src="${esc(p.image_url)}" alt="${esc(p.name)}">
    <div class="detail-grid">
      <div>
        <h2>A closer look</h2>
        <p>${esc(p.description)}</p>
        ${section('Before you go')}
        <p>${esc(p.tips || 'Ask your guide about planning your visit.')}</p>
        ${section('Traveler reviews', '', state.user?.role === 'TOURIST' ? button('place-review', 'Write a review', `data-id="${id}"`, 'secondary') : '')}
        ${typeof reviewRows === 'function' ? reviewRows(reviews) : ''}
      </div>
      <aside class="booking-panel">
        <h3>See it with a local</h3>
        <p class="subtitle">Choose a guide and make this place part of your journey.</p>
        <a class="btn" style="margin-top:18px" href="#guides">Find a guide ${icon('arrow-right')}</a>
      </aside>
    </div>
    ${section('From the community')}
    <div class="gallery-grid">
      ${gallery.map(typeof galleryCard === 'function' ? galleryCard : () => '').join('') || '<p class="muted">No photos yet.</p>'}
    </div>`;
}

async function guidePage(id) {
  const [g, reviews, slots] = await Promise.all([
    api(`/api/public/guides/${id}`),
    api(`/api/public/reviews?guide=${id}`),
    api(`/api/public/guides/${id}/availability`)
  ]);
  state.currentGuide = g;

  return `<a class="text-button" href="#guides">${icon('arrow-left')} All guides</a>` +
    heading(g.name, g.location, `<span class="tag approved">${icon('badge-check')}Verified guide</span>`) +
    `<div class="detail-grid">
      <div>
        <img class="detail-image" src="${esc(g.image_url)}" alt="${esc(g.location)}">
        <h2>Meet ${esc(g.name.split(' ')[0])}</h2>
        <p>${esc(g.bio)}</p>
        <div class="location">${icon('languages')} ${esc(g.languages)}</div>
        ${section('Upcoming availability')}
        <div class="filters">
          ${slots.filter(s => s.status === 'AVAILABLE').slice(0, 14).map(s => `
            <span class="tag approved">${niceDate(s.available_date)}</span>
          `).join('') || '<span class="muted">No available dates listed.</span>'}
        </div>
        ${section('Traveler reviews', `${reviews.length} reviews`)}
        ${typeof reviewRows === 'function' ? reviewRows(reviews) : ''}
      </div>
      <aside class="booking-panel">
        <div class="price">${money(g.daily_rate)} <small class="muted small">/ day</small></div>
        ${rating(g.rating)}
        <div class="actions" style="margin-top:20px">
          ${button('book', 'Request a booking', `data-id="${id}"`)}
          ${button('start-chat', icon('message-circle') + ' Message guide', `data-id="${id}"`, 'secondary')}
        </div>
        <p class="note">Your guide will confirm availability before your booking is accepted.</p>
      </aside>
    </div>`;
}

async function contributionsPage() {
  state.contributions = await api('/api/places/mine');
  return heading('My places', 'Your local discoveries, shared with the community.', button('new-place', icon('plus') + ' Add a place')) +
    (state.contributions.length ? `
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Place</th>
              <th>Category</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${state.contributions.map(p => `
              <tr>
                <td>
                  <strong>${esc(p.name)}</strong>
                  <small>${esc(p.location)}</small>
                </td>
                <td>${esc(p.category)}</td>
                <td>${tag(p.status)}</td>
                <td>
                  <div class="actions">
                    ${p.status !== 'ARCHIVED' ? tool('edit-place', 'pencil', 'Edit place', `data-id="${p.id}"`) + tool('delete-place', 'trash-2', 'Remove place', `data-id="${p.id}"`) : ''}
                  </div>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    ` : empty('Share your first place', 'Add a destination with a photo, description, and local tips.'));
}

async function placeModal(place) {
  if (!needRole()) return;
  const p = place || {};
  modal(place ? 'Edit place' : 'Share a place', `
    <form id="place-form" data-id="${p.id || ''}">
      <div class="form-grid">
        ${field('name', 'Place name', p.name, 'text', 'required maxlength="150"')}
        ${field('location', 'Location', p.location, 'text', 'required maxlength="150"')}
        <div>
          <label for="category">Category</label>
          <select name="category" id="category">
            ${['Heritage', 'Nature', 'Beach', 'Adventure', 'City'].map(c => `
              <option ${c === p.category ? 'selected' : ''}>${c}</option>
            `).join('')}
          </select>
        </div>
        <div class="full">
          <label for="description">Description</label>
          <textarea name="description" id="description" required maxlength="4000">${esc(p.description)}</textarea>
        </div>
        <div class="full">
          <label for="tips">Local tips</label>
          <textarea name="tips" id="tips" maxlength="1500">${esc(p.tips)}</textarea>
        </div>
        ${uploadField(p.image_url)}
      </div>
      <p class="note">Submissions and edits are reviewed by an admin before publication.</p>
      <p class="form-error"></p>
      <button class="btn" type="submit">Submit for approval</button>
    </form>
  `);
}

// Places Action Listeners
document.addEventListener('click', async event => {
  const el = event.target.closest('[data-action]');
  if (!el) return;
  const action = el.dataset.action;
  const id = Number(el.dataset.id);
  const value = el.dataset.value;

  if (action === 'menu') {
    $('#sidebar').classList.toggle('open');
  } else if (action === 'category') {
    state.category = value;
    await render();
  } else if (action === 'reset-search') {
    state.q = '';
    state.category = '';
    await render();
  } else if (action === 'retry') {
    await render();
  } else if (action === 'new-place') {
    await placeModal();
  } else if (action === 'edit-place') {
    await placeModal(state.contributions.find(p => p.id === id));
  } else if (action === 'delete-place') {
    confirmAction('Remove this place?', 'It will be removed from public listings. Existing booking records are kept.', async () => {
      await api(`/api/places/${id}`, { method: 'DELETE' });
      await render();
    });
  }
});

document.addEventListener('submit', async event => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;

  if (form.id === 'search') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    state.q = data.q;
    state.category = data.category;
    await render();
  } else if (form.id === 'guide-search') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    state.q = data.q;
    await render();
  } else if (form.id === 'place-form') {
    event.preventDefault();
    let data = Object.fromEntries(new FormData(form));
    data = await uploaded(form, data);
    await api('/api/places' + (form.dataset.id ? '/' + form.dataset.id : ''), {
      method: form.dataset.id ? 'PUT' : 'POST',
      body: data
    });
    closeModal();
    location.hash = 'contributions';
    await render();
    toast('Submitted for approval');
  }
});
