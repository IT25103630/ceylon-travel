/**
 * Function: Tourist and Guide Chat Management
 * Member Name: Premasiriwardhana I. H. N. C.
 * Student ID: IT25103630
 * Role: Scrum Master
 * Course: SE2030 - Software Engineering - SLIIT
 */

'use strict';

function messageHTML() {
  return state.messages.map(m => `
    <div class="message ${m.sender_id === state.user.id ? 'own' : ''}">
      <div class="bubble">${esc(m.body)}</div>
      <div class="message-tools">
        <small>${new Date(m.created_at).toLocaleTimeString('en-GB', {hour:'2-digit', minute:'2-digit'})}${m.edited ? ' / edited' : ''}</small>
        ${m.sender_id === state.user.id ? tool('edit-message', 'pencil', 'Edit message', `data-id="${m.id}"`) + tool('delete-message', 'trash-2', 'Delete message', `data-id="${m.id}"`) : ''}
      </div>
    </div>
  `).join('');
}

async function refreshMessages() {
  if (state.view !== 'messages' || !state.room) return;
  const room = state.room;
  const messages = await api(`/api/conversations/${room}/messages`);
  if (state.view !== 'messages' || room !== state.room) return;
  state.messages = messages;
  const list = $('#message-list');
  if (list) {
    list.innerHTML = messageHTML();
    list.scrollTop = list.scrollHeight;
    icons();
  }
}

async function messagesPage() {
  state.rooms = await api('/api/conversations');
  if (!state.rooms.some(r => r.id === state.room)) state.room = state.rooms[0]?.id;
  if (!state.rooms.length) {
    return heading('Messages', 'A good journey starts with a conversation.') +
      empty('Start a conversation', 'Open a guide profile to send your first message.', '<a class="btn secondary" href="#guides">Find a guide</a>');
  }
  const room = state.rooms.find(r => r.id === state.room);
  state.messages = await api(`/api/conversations/${state.room}/messages`);

  const partnerName = state.user.role === 'GUIDE' ? room.tourist_name : room.guide_name;

  return heading('Messages', 'Your conversations with local people.') +
    `<div class="chat">
      <div class="rooms">
        ${state.rooms.map(r => `
          <button class="room ${r.id === state.room ? 'active' : ''}" data-action="room" data-id="${r.id}">
            <span class="avatar">${esc(initials(state.user.role === 'GUIDE' ? r.tourist_name : r.guide_name))}</span>
            <span>
              <strong>${esc(state.user.role === 'GUIDE' ? r.tourist_name : r.guide_name)}</strong>
              <small>${esc(r.last_message || 'Conversation started')}</small>
            </span>
          </button>
        `).join('')}
      </div>
      <section class="thread">
        <div class="thread-title">${esc(partnerName)}</div>
        <div class="messages" id="message-list">${messageHTML()}</div>
        <form id="send-message" class="composer">
          <input name="body" placeholder="Write a message..." aria-label="Message" maxlength="2000" required autocomplete="off">
          <button class="btn" aria-label="Send message" title="Send message">${icon('send')}</button>
        </form>
      </section>
    </div>`;
}

// Chat Action Handlers
document.addEventListener('click', async event => {
  const el = event.target.closest('[data-action]');
  if (!el) return;
  const action = el.dataset.action;
  const id = Number(el.dataset.id);

  if (action === 'room') {
    state.room = id;
    await render();
  } else if (action === 'start-chat') {
    if (needRole('TOURIST')) {
      modal('Message your guide', `
        <form id="start-chat-form" data-guide="${id}">
          <label for="body">Your message</label>
          <textarea id="body" name="body" required maxlength="2000" placeholder="Hello! I would like to ask about..."></textarea>
          <p class="form-error"></p>
          <button class="btn" type="submit">Send message ${icon('send')}</button>
        </form>
      `);
    }
  } else if (action === 'edit-message') {
    const m = state.messages.find(m => m.id === id);
    modal('Edit message', `
      <form id="edit-message-form" data-id="${id}">
        <label for="body">Message</label>
        <textarea name="body" id="body" required maxlength="2000">${esc(m.body)}</textarea>
        <p class="form-error"></p>
        <button class="btn" type="submit">Save message</button>
      </form>
    `);
  } else if (action === 'delete-message') {
    confirmAction('Delete this message?', 'This removes the message for both participants.', async () => {
      await api(`/api/messages/${id}`, { method: 'DELETE' });
      await refreshMessages();
    });
  }
});

document.addEventListener('submit', async event => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;

  if (form.id === 'start-chat-form') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    const room = await api('/api/conversations', {
      method: 'POST',
      body: { guide_id: Number(form.dataset.guide), body: data.body }
    });
    state.room = room.id;
    closeModal();
    location.hash = 'messages';
    await render();
  } else if (form.id === 'send-message') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    await api(`/api/conversations/${state.room}/messages`, { method: 'POST', body: data });
    form.reset();
    await refreshMessages();
  } else if (form.id === 'edit-message-form') {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    await api(`/api/messages/${form.dataset.id}`, { method: 'PUT', body: data });
    closeModal();
    await refreshMessages();
  }
});
