/**
 * Function: User Authentication & Profile Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */

'use strict';

function authModal(register = false) {
  modal(register ? 'Join Ceylon Travel' : 'Welcome back', `
    <div class="auth-tabs">
      <button class="${register ? '' : 'active'}" data-action="login">Sign in</button>
      <button class="${register ? 'active' : ''}" data-action="register">Create account</button>
    </div>
    <form id="${register ? 'register-form' : 'login-form'}">
      <div class="form-grid">
        ${register ? '<div class="full">' + field('name', 'Full name', '', 'text', 'required maxlength="100" autocomplete="name"') + '</div>' : ''}
        <div class="full">${field('email', 'Email address', '', 'email', 'required autocomplete="email"')}</div>
        <div class="full">${field('password', 'Password', '', 'password', `required minlength="8" maxlength="64" autocomplete="${register ? 'new-password' : 'current-password'}"`)}</div>
        ${register ? `
          <div class="full">
            <label for="role">Account type</label>
            <select name="role" id="role">
              <option value="TOURIST">Tourist</option>
              <option value="GUIDE">Tour guide</option>
              <option value="COMMUNITY">Community member</option>
            </select>
          </div>
        ` : ''}
      </div>
      <p class="form-error"></p>
      <button class="btn" type="submit">${register ? 'Create account' : 'Sign in'} ${icon('arrow-right')}</button>
    </form>
  `);
}

async function profilePage() {
  const u = state.user;
  const g = u.role === 'GUIDE' ? await api('/api/guides/profile') : null;
  state.guideProfile = g;

  return heading('My profile', 'A little about you, for the journeys ahead.') +
    `<div class="form-page">
      <div class="profile-heading">
        <span class="avatar">${esc(initials(u.name))}</span>
        <div>
          <h3>${esc(u.name)}</h3>
          <p class="muted small">${esc(u.email)}</p>
          ${tag(u.role)}
        </div>
      </div>
      <form id="profile-form">
        <div class="form-grid">
          ${field('name', 'Full name', u.name, 'text', 'required maxlength="100"')}
          ${field('phone', 'Phone', u.phone, 'tel', 'maxlength="30"')}
        </div>
        <p class="form-error"></p>
        <button class="btn" type="submit">Save profile</button>
      </form>
      ${g ? section('Guide profile', '', tag(g.verified ? 'VERIFIED' : 'AWAITING VERIFICATION')) + `
        <form id="guide-profile-form">
          <div class="form-grid">
            ${field('location', 'Location', g.location, 'text', 'required maxlength="100"')}
            ${field('languages', 'Languages', g.languages, 'text', 'required maxlength="200"')}
            ${field('daily_rate', 'Daily rate (LKR)', g.daily_rate, 'number', 'required min="1" max="1000000" step="0.01"')}
            <div class="full">
              <label for="bio">About you</label>
              <textarea id="bio" name="bio" required maxlength="2000">${esc(g.bio)}</textarea>
            </div>
            ${uploadField(g.image_url)}
          </div>
          <p class="form-error"></p>
          <button class="btn" type="submit">Save guide profile</button>
        </form>
      ` : ''}
    </div>`;
}

// Auth Click Handlers
document.addEventListener('click', async event => {
  const el = event.target.closest('[data-action]');
  if (!el) return;
  const action = el.dataset.action;

  if (action === 'login') {
    authModal();
  } else if (action === 'register') {
    authModal(true);
  } else if (action === 'close') {
    closeModal();
  } else if (action === 'logout') {
    await api('/api/auth/logout', { method: 'POST' });
    state.user = null;
    state.csrf = null;
    state.events?.close();
    state.room = null;
    location.hash = 'discover';
    await render();
    toast('Signed out');
  } else if (action === 'confirm') {
    if (typeof state.confirm === 'function') {
      el.disabled = true;
      await state.confirm();
      closeModal();
      toast('Updated');
    }
  }
});

// Auth Submit Handlers
document.addEventListener('submit', async event => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;
  const error = $('.form-error', form);

  try {
    if (form.id === 'register-form' || form.id === 'login-form') {
      event.preventDefault();
      const data = Object.fromEntries(new FormData(form));

      if (form.id === 'register-form') {
        await api('/api/auth/register', { method: 'POST', body: data });
      }

      await api('/api/auth/login', {
        method: 'POST',
        body: new URLSearchParams({
          username: data.email.toLowerCase().trim(),
          password: data.password
        })
      });

      state.csrf = null;
      state.user = await api('/api/auth/me');
      connectEvents();
      closeModal();
      await render();
      toast('Welcome, ' + state.user.name.split(' ')[0]);
    } else if (form.id === 'profile-form') {
      event.preventDefault();
      const data = Object.fromEntries(new FormData(form));
      state.user = await api('/api/auth/profile', { method: 'PUT', body: data });
      await render();
      toast('Profile saved');
    } else if (form.id === 'guide-profile-form') {
      event.preventDefault();
      let data = Object.fromEntries(new FormData(form));
      data = await uploaded(form, data);
      data.daily_rate = Number(data.daily_rate);
      await api('/api/guides/profile', { method: 'PUT', body: data });
      await render();
      toast('Guide profile saved');
    }
  } catch (err) {
    if (error) error.textContent = err.message;
    else toast(err.message);
  }
});
