/**
 * CeylonTravel - Core Application Shell & State Management
 * SE2030 - Software Engineering - SLIIT
 */

'use strict';

const $ = (s, root = document) => root.querySelector(s);
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const icon = name => `<i data-lucide="${name}"></i>`;
const money = n => `LKR ${Number(n || 0).toLocaleString('en-LK', {maximumFractionDigits:0})}`;
const initials = name => String(name || '').split(' ').slice(0, 2).map(s => s[0]).join('');
const tag = s => `<span class="tag ${esc(String(s).toLowerCase())}">${esc(String(s).replaceAll('_', ' '))}</span>`;
const rating = n => `<span class="rating">${icon('star')} ${n ? Number(n).toFixed(1) : 'New'}</span>`;
const button = (action, label, extra = '', cls = '') => `<button class="btn ${cls}" data-action="${action}" ${extra}>${label}</button>`;
const tool = (action, name, title, extra = '') => `<button class="icon-button" data-action="${action}" title="${esc(title)}" aria-label="${esc(title)}" ${extra}>${icon(name)}</button>`;
const dateISO = date => `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
const today = () => dateISO(new Date());
const niceDate = s => new Date(String(s).slice(0,10)+'T12:00:00').toLocaleDateString('en-GB',{day:'numeric',month:'short',year:'numeric'});

const state = {
  user: null,
  csrf: null,
  view: 'discover',
  q: '',
  category: '',
  places: [],
  guides: [],
  bookings: [],
  reviews: [],
  contributions: [],
  gallery: [],
  reports: [],
  rooms: [],
  room: null,
  messages: [],
  admin: null,
  adminTab: 'places',
  month: new Date(new Date().getFullYear(), new Date().getMonth(), 1),
  slots: [],
  events: null,
  confirm: null
};

let renderVersion = 0;

async function api(path, options = {}) {
  const method = options.method || 'GET';
  const headers = {...options.headers};
  if (method !== 'GET') {
    if (!state.csrf) state.csrf = await (await fetch('/api/auth/csrf')).json();
    headers[state.csrf.header] = state.csrf.token;
  }
  let body = options.body;
  if (body && !(body instanceof FormData) && !(body instanceof URLSearchParams)) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(body);
  }
  const response = await fetch(path, {...options, method, headers, body});
  const text = await response.text();
  let data;
  try { data = text ? JSON.parse(text) : null; } catch { data = null; }
  if (!response.ok) {
    if (response.status === 401) throw new Error(data?.message || 'Please sign in to continue.');
    if (response.status === 403) throw new Error(data?.message || 'This action is not available for your account. Refresh and try again.');
    throw new Error(data?.message || 'The request could not be completed. Please try again.');
  }
  return data;
}

function icons() {
  window.lucide?.createIcons();
}

function toast(message) {
  const el = $('#toast');
  if (!el) return;
  el.textContent = message;
  el.classList.add('visible');
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => el.classList.remove('visible'), 4000);
}

function heading(title, sub, action = '') {
  return `<div class="page-head"><div><div class="eyebrow">CEYLON TRAVEL / SRI LANKA</div><h1>${esc(title)}</h1><p class="subtitle">${esc(sub)}</p></div>${action}</div>`;
}

function empty(title, text, action = '') {
  return `<div class="empty">${icon('compass')}<h3>${esc(title)}</h3><p>${esc(text)}</p>${action}</div>`;
}

function section(title, sub = '', action = '') {
  return `<div class="section-head"><div><h2>${title}</h2>${sub ? `<p>${sub}</p>` : ''}</div>${action}</div>`;
}

function field(name, label, value = '', type = 'text', attrs = '') {
  return `<div><label for="${name}">${label}</label><input id="${name}" name="${name}" type="${type}" value="${esc(value)}" ${attrs}></div>`;
}

function uploadField(url = '') {
  return `<div class="full"><label for="photo">Photo (JPG or PNG, up to 5 MB)</label><input type="file" id="photo" name="photo" accept="image/jpeg,image/png" ${url?'':'required'}><input type="hidden" name="image_url" value="${esc(url)}">${url?`<img class="upload-preview" src="${esc(url)}" alt="Current photo">`:''}</div>`;
}

function modal(title, body) {
  $('#dialog-body').innerHTML = `<div class="dialog-head"><h2>${esc(title)}</h2>${tool('close','x','Close dialog')}</div>${body}`;
  if (!$('#dialog').open) $('#dialog').showModal();
  icons();
}

function closeModal() {
  $('#dialog').close();
}

function confirmAction(title, text, callback) {
  modal(title, `<p>${esc(text)}</p><div class="actions" style="margin-top:24px">${button('confirm','Confirm')}${button('close','Keep it','','secondary')}</div><p class="form-error"></p>`);
  state.confirm = callback;
}

function needRole(...roles) {
  if (!state.user) {
    if (typeof authModal === 'function') authModal();
    return false;
  }
  if (roles.length && !roles.includes(state.user.role)) {
    toast('This action is available for ' + roles.map(r => r.toLowerCase()).join(' or ') + ' accounts.');
    return false;
  }
  return true;
}

async function uploaded(form, data) {
  const file = form.elements.photo?.files[0];
  if (file) {
    const fd = new FormData();
    fd.append('file', file);
    data.image_url = (await api('/api/uploads', {method: 'POST', body: fd})).url;
  }
  delete data.photo;
  return data;
}

function connectEvents() {
  state.events?.close();
  if (!state.user) return;
  state.events = new EventSource('/api/events');
  state.events.addEventListener('changed', event => {
    if (event.data === 'chat' && typeof refreshMessages === 'function') {
      refreshMessages().catch(() => {});
    } else if (event.data === 'notifications') {
      toast('You have a new update.');
    }
  });
}

function renderShell() {
  const role = state.user?.role;
  const nav = [
    ['discover', 'compass', 'Discover'],
    ['guides', 'users-round', 'Local guides'],
    ['gallery', 'images', 'Photo gallery']
  ];
  if (state.user) {
    nav.push(null);
    if (['TOURIST', 'GUIDE'].includes(role)) {
      nav.push(['bookings', 'calendar-check', 'My bookings'], ['messages', 'message-circle', 'Messages']);
    }
    if (role === 'GUIDE') nav.push(['calendar', 'calendar-days', 'My availability']);
    if (role === 'TOURIST') nav.push(['reviews', 'star', 'My reviews']);
    nav.push(['contributions', 'map-pinned', 'My places'], ['reports', 'flag', 'Reports & support']);
    if (role === 'ADMIN') nav.push(['admin', 'shield-check', 'Administration']);
    nav.push(['profile', 'circle-user-round', 'My profile']);
  }
  $('#navigation').innerHTML = nav.map(n => n ? `<a class="nav-item ${state.view === n[0] ? 'active' : ''}" href="#${n[0]}">${icon(n[1])}${n[2]}</a>` : '<div class="nav-divider"></div>').join('');
  $('#crumb').textContent = ({
    discover: 'Discover',
    place: 'Destination',
    guide: 'Guide profile',
    admin: 'Administration',
    contributions: 'My places',
    calendar: 'My availability',
    help: 'Help & support'
  })[state.view] || state.view.charAt(0).toUpperCase() + state.view.slice(1);

  $('#account').innerHTML = state.user
    ? `${tool('notifications', 'bell', 'Notifications')}<a class="account-name" href="#profile">${esc(state.user.name)}<small>${esc(role.toLowerCase())}</small></a><a class="avatar" href="#profile" aria-label="My profile">${esc(initials(state.user.name))}</a>${tool('logout', 'log-out', 'Sign out')}`
    : `${button('login', 'Sign in', '', 'secondary')}${button('register', 'Join Ceylon Travel')}`;
  icons();
}

async function render() {
  const version = ++renderVersion;
  const parts = (location.hash.slice(1) || 'discover').split('/');
  state.view = parts[0];
  $('#sidebar').classList.remove('open');
  renderShell();

  const protectedViews = ['bookings', 'calendar', 'contributions', 'reviews', 'reports', 'messages', 'profile', 'admin', 'notifications'];
  if (protectedViews.includes(state.view) && !state.user) {
    $('#main').innerHTML = heading('Your journey starts here', 'Sign in to view your account.') +
      empty('Welcome to Ceylon Travel', 'Sign in or create an account to continue.', button('login', 'Sign in'));
    icons();
    return;
  }

  const roles = {
    admin: ['ADMIN'],
    calendar: ['GUIDE'],
    reviews: ['TOURIST'],
    bookings: ['TOURIST', 'GUIDE'],
    messages: ['TOURIST', 'GUIDE']
  };
  if (roles[state.view] && !roles[state.view].includes(state.user?.role)) {
    $('#main').innerHTML = empty('This page is not available', 'Choose a page from your navigation.');
    icons();
    return;
  }

  $('#main').innerHTML = '<div class="loading">Loading your journey...</div>';
  try {
    const views = {
      discover: typeof discover === 'function' ? discover : null,
      guides: typeof guidesPage === 'function' ? guidesPage : null,
      place: typeof placePage === 'function' ? () => placePage(Number(parts[1])) : null,
      guide: typeof guidePage === 'function' ? () => guidePage(Number(parts[1])) : null,
      bookings: typeof bookingsPage === 'function' ? bookingsPage : null,
      calendar: typeof calendarPage === 'function' ? calendarPage : null,
      gallery: typeof galleryPage === 'function' ? galleryPage : null,
      contributions: typeof contributionsPage === 'function' ? contributionsPage : null,
      reviews: typeof reviewsPage === 'function' ? reviewsPage : null,
      reports: typeof reportsPage === 'function' ? reportsPage : null,
      messages: typeof messagesPage === 'function' ? messagesPage : null,
      profile: typeof profilePage === 'function' ? profilePage : null,
      admin: typeof adminPage === 'function' ? adminPage : null,
      notifications: typeof notificationsPage === 'function' ? notificationsPage : null,
      help: typeof helpPage === 'function' ? helpPage : null
    };

    const handler = views[state.view];
    const html = handler ? await handler() : empty('Page not found', 'Return to Discover to continue.', '<a class="btn" href="#discover">Discover</a>');
    if (version !== renderVersion) return;
    $('#main').innerHTML = html;
    icons();
    const list = $('#message-list');
    if (list) list.scrollTop = list.scrollHeight;
  } catch (error) {
    if (version === renderVersion) {
      $('#main').innerHTML = empty('Something needs attention', error.message, button('retry', 'Try again', '', 'secondary'));
      icons();
    }
  }
}

// Global Event Listeners
window.addEventListener('hashchange', () => {
  window.scrollTo(0, 0);
  render();
});

$('#dialog')?.addEventListener('click', e => {
  if (e.target === $('#dialog')) {
    const r = $('#dialog').getBoundingClientRect();
    if (e.clientX < r.left || e.clientX > r.right || e.clientY < r.top || e.clientY > r.bottom) {
      closeModal();
    }
  }
});

(async () => {
  try {
    const user = await api('/api/auth/me');
    state.user = user.id ? user : null;
    connectEvents();
  } catch {}
  await render();
})();
