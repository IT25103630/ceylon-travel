/**
 * Function: Gallery Management
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */

'use strict';

function galleryCard(g) {
  return `
    <article class="gallery-item">
      <a href="#place/${g.place_id}">
        <img src="${esc(g.image_url)}" alt="${esc(g.caption)}" loading="lazy">
      </a>
      <h3>${esc(g.caption)}</h3>
      <div class="actions">
        <small>${esc(g.place_name)} / ${esc(g.author_name)}</small>
        ${state.user && (state.user.id === g.user_id || state.user.role === 'ADMIN') ? tool('delete-photo', 'trash-2', 'Delete photo', `data-id="${g.id}"`) : ''}
      </div>
    </article>
  `;
}

async function galleryPage() {
  state.gallery = await api('/api/public/gallery');
  return heading('Through your lens', 'Little moments from journeys around Sri Lanka.', button('new-photo', icon('upload') + ' Share a photo')) +
    `<div class="gallery-grid">
      ${state.gallery.map(galleryCard).join('') || empty('The gallery is waiting', 'Share a photo from your travels.')}
    </div>`;
}

// Gallery Action Listeners
document.addEventListener('click', async event => {
  const el = event.target.closest('[data-action]');
  if (!el) return;
  const action = el.dataset.action;
  const id = Number(el.dataset.id);

  if (action === 'new-photo') {
    if (!needRole('TOURIST', 'GUIDE')) return;
    const places = await api('/api/public/places');
    modal('Share a photo', `
      <form id="photo-form">
        <div class="form-grid">
          <div class="full">
            <label for="place_id">Place</label>
            <select name="place_id" id="place_id" required>
              ${places.map(p => `<option value="${p.id}">${esc(p.name)}</option>`).join('')}
            </select>
          </div>
          <div class="full">
            ${field('caption', 'Caption', '', 'text', 'required maxlength="300"')}
          </div>
          ${uploadField()}
        </div>
        <p class="form-error"></p>
        <button class="btn" type="submit">Publish photo</button>
      </form>
    `);
  } else if (action === 'delete-photo') {
    confirmAction('Remove this photo?', 'This removes the photo from the shared gallery.', async () => {
      await api(`/api/gallery/${id}`, { method: 'DELETE' });
      await render();
    });
  }
});

document.addEventListener('submit', async event => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;

  if (form.id === 'photo-form') {
    event.preventDefault();
    let data = Object.fromEntries(new FormData(form));
    data = await uploaded(form, data);
    data.place_id = Number(data.place_id);
    await api('/api/gallery', { method: 'POST', body: data });
    closeModal();
    await render();
    toast('Photo published');
  }
});
