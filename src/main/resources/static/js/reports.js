/**
 * Function: Emergency and Complaint Reporting Management
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */

'use strict';

function reportModal() {
  if (!needRole()) return;
  modal('Contact the team', `
    <form id="report-form">
      <div class="form-grid">
        <div>
          <label for="type">Report type</label>
          <select name="type" id="type">
            <option value="SUPPORT">Support question</option>
            <option value="COMPLAINT">Complaint</option>
            <option value="EMERGENCY">Emergency report</option>
          </select>
        </div>
        ${field('subject', 'Subject', '', 'text', 'required maxlength="150"')}
        <div class="full">
          ${field('location', 'Location (optional)', '', 'text', 'maxlength="200"')}
        </div>
        <div class="full">
          <label for="description">What happened?</label>
          <textarea name="description" id="description" required maxlength="3000"></textarea>
        </div>
      </div>
      <p class="note">This sends a report to the platform admin, not to emergency services.</p>
      <p class="form-error"></p>
      <button  type="submit" class="btn">Send report</button>
    </form>
  `);
}

async function reportsPage() {
  state.reports = await api('/api/reports');
  return heading('Reports & support', 'Keep in touch with the Ceylon Tours team.', button('new-report', icon('plus') + ' New report')) +
    `<div class="notice">Emergency reports are sent to the platform admin. This is not a live emergency dispatch service.</div>` +
    (state.reports.length ? state.reports.map(r => `
      <article class="review-row">
        <div class="review-by">
          <strong>#${r.id} ${esc(r.subject)}</strong>
          ${tag(r.status)}
        </div>
        <p>${esc(r.description)}</p>
        <div class="location">${tag(r.type)} ${esc(r.location)}</div>
        ${r.response ? `<p><strong>Admin response:</strong> ${esc(r.response)}</p>` : ''}
        <small class="muted">${niceDate(r.created_at)}</small>
      </article>
    `).join('') : empty('No reports yet', 'Your submitted reports and replies will appear here.'));
}

async function notificationsPage() {
  const rows = await api('/api/notifications');
  return heading('Notifications', 'Updates from your guides and the Ceylon Tours team.', button('read-notifications', 'Mark all read', '', 'secondary')) +
    (rows.map(n => `
      <div class="notification ${n.is_read ? '' : 'unread'}">
        ${icon('bell')}
        <div>
          <p>${esc(n.text)}</p>
          <small>${niceDate(n.created_at)}</small>
        </div>
      </div>
    `).join('') || empty('All caught up', 'Your new updates will appear here.'));
}

function adminTable() {
  const rows = state.admin[state.adminTab];
  if (!rows.length) return empty('Nothing here yet', 'New records will appear here.');

  const body = rows.map(r => {
    switch (state.adminTab) {
      case 'places':
        return `<tr>
          <td>
            <strong>${esc(r.name)}</strong>
            <small>${esc(r.submitted_name)} / ${esc(r.location)}</small>
          </td>
          <td>${tag(r.status)}</td>
          <td>
            <div class="actions">
              ${tool('admin-place-view', 'eye', 'Inspect submission', `data-id="${r.id}"`)}
              ${r.status !== 'APPROVED' ? button('approve-place', 'Approve', `data-id="${r.id}" data-status="APPROVED"`, 'compact') : ''}
              ${r.status !== 'REJECTED' ? button('approve-place', 'Reject', `data-id="${r.id}" data-status="REJECTED"`, 'compact secondary') : ''}
            </div>
          </td>
        </tr>`;
      case 'users':
        return `<tr>
          <td>
            <strong>${esc(r.name)}</strong>
            <small>${esc(r.email)}</small>
          </td>
          <td>
            ${tag(r.role)} ${tag(r.active ? 'ACTIVE' : 'DISABLED')}
            ${r.role === 'GUIDE' ? tag(r.verified ? 'VERIFIED' : 'UNVERIFIED') : ''}
          </td>
          <td>
            <div class="actions">
              ${r.role === 'GUIDE' ? button('verify-guide', r.verified ? 'Unverify' : 'Verify guide', `data-id="${r.id}" data-value="${!r.verified}"`, 'compact secondary') : ''}
              ${r.role !== 'ADMIN' ? button('toggle-user', r.active ? 'Disable' : 'Enable', `data-id="${r.id}" data-value="${!r.active}"`, 'compact secondary') : ''}
            </div>
          </td>
        </tr>`;
      case 'reviews':
        return `<tr>
          <td>
            <strong>${esc(r.author_name)} / ${r.rating} stars</strong>
            <p>${esc(r.comment)}</p>
          </td>
          <td>${tag(r.status)}</td>
          <td>
            <div class="actions">
              ${r.status !== 'PUBLISHED' ? button('moderate-review', 'Publish', `data-id="${r.id}" data-status="PUBLISHED"`, 'compact') : ''}
              ${r.status !== 'REJECTED' ? button('moderate-review', 'Reject', `data-id="${r.id}" data-status="REJECTED"`, 'compact secondary') : ''}
              ${tool('delete-review', 'trash-2', 'Remove review', `data-id="${r.id}"`)}
            </div>
          </td>
        </tr>`;
      case 'reports':
        return `<tr>
          <td>
            <strong>${esc(r.subject)}</strong>
            <small>${esc(r.author_name)} / ${esc(r.type)}</small>
          </td>
          <td>${tag(r.status)}</td>
          <td>
            ${button('handle-report', 'View & respond', `data-id="${r.id}"`, 'compact secondary')}
          </td>
        </tr>`;
      case 'bookings':
        return `<tr>
          <td>
            <strong>Booking #${r.id}</strong>
            <small>${esc(r.tourist_name)} with ${esc(r.guide_name)} / ${niceDate(r.start_date)}</small>
          </td>
          <td>${tag(r.status)}</td>
          <td>${money(r.total_price)}</td>
        </tr>`;
    }
  }).join('');

  return `<div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>${state.adminTab === 'users' ? 'Member' : 'Details'}</th>
          <th>Status</th>
          <th>${state.adminTab === 'bookings' ? 'Total' : 'Actions'}</th>
        </tr>
      </thead>
      <tbody>${body}</tbody>
    </table>
  </div>`;
}

async function adminPage() {
  state.admin = await api('/api/admin/overview');
  const a = state.admin;
  return heading('Administration', 'Keep the community welcoming, accurate, and up to date.') +
    `<div class="stats">
      ${[
        ['Members', a.users.length],
        ['Pending places', a.places.filter(p => p.status === 'PENDING').length],
        ['Open reports', a.reports.filter(r => r.status !== 'RESOLVED').length],
        ['Bookings', a.bookings.length]
      ].map(([l, n]) => `<div class="stat"><strong>${n}</strong><span>${l}</span></div>`).join('')}
    </div>
    <div class="tabs">
      ${['places', 'users', 'reviews', 'reports', 'bookings'].map(t => `
        <button class="tab ${t === state.adminTab ? 'active' : ''}" data-action="admin-tab" data-value="${t}">
          ${t.charAt(0).toUpperCase() + t.slice(1)}
        </button>
      `).join('')}
    </div>
    ${adminTable()}`;
}

function helpPage() {
  return heading('Help & support', 'A few useful details before you set off.') +
    `<div class="form-page">
      <details open>
        <summary>How do I book a guide?</summary>
        <p>Choose a guide, select available dates and a destination, and send a booking request. The guide can accept or decline it. Your booking is confirmed only after acceptance.</p>
      </details>
      <details>
        <summary>Can I change my trip dates?</summary>
        <p>Cancel the existing booking and send a new request for the dates you need. Availability is checked again for the new request.</p>
      </details>
      <details>
        <summary>How are places approved?</summary>
        <p>Community submissions go to an admin for review. Edited places return to pending approval before they appear in search.</p>
      </details>
      <details>
        <summary>When can I leave a review?</summary>
        <p>After a completed booking, you can review your guide and the place you visited. You can edit your reviews from My reviews.</p>
      </details>
      <details>
        <summary>How do emergency reports work?</summary>
        <p>Reports go to the platform administrator. They are not monitored as a live emergency service. In immediate danger, contact local emergency services directly.</p>
      </details>
      <details>
        <summary>How do payments work?</summary>
        <p>Booking totals are guide-fee estimates in Sri Lankan rupees. Online payment collection is not connected in this local project build.</p>
      </details>
      <div class="callout">
        <div>
          <h3>Need a hand?</h3>
          <p>Send the team a question or report an issue.</p>
        </div>
        ${button('new-report', 'Contact support', '', 'secondary')}
      </div>
    </div>`;
}

// Reports & Admin Event Listeners
document.addEventListener('click', async event => {
  const el = event.target.closest('[data-action]');
  if (!el) return;
  const action = el.dataset.action;
  const id = Number(el.dataset.id);
  const value = el.dataset.value;

  if (action === 'new-report') {
    reportModal();
  } else if (action === 'notifications') {
    location.hash = 'notifications';
  } else if (action === 'read-notifications') {
    await api('/api/notifications/read', { method: 'POST' });
    await render();
  } else if (action === 'admin-tab') {
    state.adminTab = value;
    await render();
  } else if (action === 'approve-place') {
    await api(`/api/admin/places/${id}`, { method: 'PATCH', body: { status: el.dataset.status } });
    await render();
    toast('Place updated');
  } else if (action === 'verify-guide') {
    await api(`/api/admin/guides/${id}`, { method: 'PATCH', body: { enabled: value === 'true' } });
    await render();
  } else if (action === 'toggle-user') {
    confirmAction('Update account access?', value === 'true' ? 'Enable this account?' : 'Disable this account? It will lose access to account actions.', async () => {
      await api(`/api/admin/users/${id}`, { method: 'PATCH', body: { enabled: value === 'true' } });
      await render();
    });
  } else if (action === 'moderate-review') {
    await api(`/api/admin/reviews/${id}`, { method: 'PATCH', body: { status: el.dataset.status } });
    await render();
  } else if (action === 'admin-place-view') {
    const p = state.admin.places.find(p => p.id === id);
    modal(p.name, `
      <img class="detail-image" src="${esc(p.image_url)}" alt="${esc(p.name)}">
      <p>${esc(p.description)}</p>
      <div class="location">${esc(p.location)} / ${esc(p.category)}</div>
      <p>${esc(p.tips)}</p>
    `);
  } else if (action === 'handle-report') {
    const r = state.admin.reports.find(r => r.id === id);
    modal('Report #' + id, `
      <strong>${esc(r.subject)}</strong>
      <p style="margin:12px 0">${esc(r.description)}</p>
      <div class="location">${esc(r.location)} / ${esc(r.author_name)}</div>
      <form id="handle-report-form" data-id="${id}">
        <label for="status">Status</label>
        <select name="status" id="status">
          ${['OPEN', 'IN_PROGRESS', 'RESOLVED'].map(s => `<option ${s === r.status ? 'selected' : ''}>${s}</option>`).join('')}
        </select>
        <label for="response" style="margin-top:16px">Response to traveler</label>
        <textarea name="response" id="response" maxlength="2000">${esc(r.response)}</textarea>
        <p class="form-error"></p>
        <button class="btn" type="submit">Save response</button>
      </form>
    `);
  }
});

document.addEventListener('submit', async event => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;

  if (form.id === 'report-form') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    await api('/api/reports', { method: 'POST', body: data });
    closeModal();
    location.hash = 'reports';
    await render();
    toast('Report sent');
  } else if (form.id === 'handle-report-form') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    await api(`/api/admin/reports/${form.dataset.id}`, { method: 'PATCH', body: data });
    closeModal();
    await render();
    toast('Response saved');
  }
});
