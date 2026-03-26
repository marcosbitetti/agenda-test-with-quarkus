(function () {
  const welcomeName = document.getElementById('welcome-name');
  const welcomeEmail = document.getElementById('welcome-email');
  const accountUsername = document.getElementById('account-username');
  const accountEmail = document.getElementById('account-email');
  const errorMessage = document.getElementById('error-message');
  const refreshUserButton = document.getElementById('refresh-user');
  const logoutButton = document.getElementById('logout-button');
  const contactsPanel = document.getElementById('contacts-panel');
  const formatPhoneNumberForDisplay = window.AgendaPhoneFormatter?.formatPhoneNumberForDisplay ?? (value => String(value ?? ''));

  const state = {
    contacts: [],
    successMessage: '',
    errorMessage: '',
    form: emptyForm(),
  };

  function emptyForm() {
    return {
      mode: 'create',
      contactId: null,
      firstName: '',
      lastName: '',
      birthDate: '',
      phoneNumbers: '',
      relationshipDegree: '',
    };
  }

  function redirectToLogin() {
    window.location.replace('/');
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function showPageError(message) {
    errorMessage.textContent = message;
    errorMessage.classList.remove('hidden');
  }

  function clearPageError() {
    errorMessage.textContent = '';
    errorMessage.classList.add('hidden');
  }

  function setPanelError(message) {
    state.errorMessage = message;
    state.successMessage = '';
    renderContactsPanel();
  }

  function setPanelSuccess(message) {
    state.successMessage = message;
    state.errorMessage = '';
    renderContactsPanel();
  }

  function renderUser(user) {
    welcomeName.textContent = `Bem-vindo, ${user.username}`;
    welcomeEmail.textContent = user.email || 'Sem e-mail informado';
    accountUsername.textContent = `Usuario: ${user.username}`;
    accountEmail.textContent = `E-mail: ${user.email || 'Sem e-mail informado'}`;
  }

  function formatBirthDate(value) {
    const [year, month, day] = value.split('-');
    return `${day}/${month}/${year}`;
  }

  function joinPhoneNumbers(phoneNumbers) {
    return phoneNumbers.join('\n');
  }

  function joinFormattedPhoneNumbers(phoneNumbers) {
    return phoneNumbers.map(formatPhoneNumberForDisplay).join('\n');
  }

  function splitPhoneNumbers(value) {
    return value
      .split(/\r?\n/)
      .map(item => item.trim())
      .filter(Boolean);
  }

  function renderAlert(message, classes) {
    if (!message) {
      return '';
    }

    return `<p class="mt-4 rounded-2xl border px-4 py-3 text-sm ${classes}">${escapeHtml(message)}</p>`;
  }

  function renderContactsList() {
    if (state.contacts.length === 0) {
      return '<p class="mt-4 rounded-2xl border border-dashed border-stone-300 bg-stone-50 px-4 py-6 text-sm text-stone-500">Nenhum contato ativo cadastrado.</p>';
    }

    return `
      <ul class="mt-4 space-y-3">
        ${state.contacts.map(contact => `
          <li class="rounded-3xl border border-stone-200 bg-stone-50 p-5 shadow-sm">
            <div class="flex flex-wrap items-start justify-between gap-4">
              <div class="space-y-3">
                <div>
                  <h3 class="text-lg font-semibold text-stone-900">${escapeHtml(contact.fullName)}</h3>
                  <p class="text-sm text-stone-500">Nascimento: ${formatBirthDate(contact.birthDate)}</p>
                </div>
                ${contact.relationshipDegree ? `<p class="text-sm text-stone-500">Parentesco: ${escapeHtml(contact.relationshipDegree)}</p>` : ''}
                <div class="flex flex-wrap gap-2">
                  ${contact.phoneNumbers.map(number => `<span class="rounded-full bg-stone-100 px-3 py-1 text-xs font-medium text-stone-700">${escapeHtml(formatPhoneNumberForDisplay(number))}</span>`).join('')}
                </div>
              </div>
              <div class="flex flex-wrap gap-2">
                <button type="button" data-action="edit" data-contact-id="${contact.id}" class="rounded-2xl border border-emerald-200 px-4 py-2 text-sm font-medium text-emerald-700 transition hover:bg-emerald-50">Editar</button>
                <button type="button" data-action="delete" data-contact-id="${contact.id}" class="rounded-2xl border border-red-200 px-4 py-2 text-sm font-medium text-red-700 transition hover:bg-red-50">Excluir</button>
              </div>
            </div>
          </li>
        `).join('')}
      </ul>
    `;
  }

  function renderContactsPanel() {
    const isEditing = state.form.mode === 'edit';
    contactsPanel.innerHTML = `
      <div class="rounded-3xl border border-stone-200 bg-stone-50 p-6">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h2 class="text-xl font-semibold text-stone-900">Contatos ativos</h2>
            <p class="mt-3 text-sm leading-6 text-stone-600">A agenda usa renderizacao estatica no navegador e consome apenas a API JSON autenticada.</p>
          </div>
          <div class="rounded-2xl border border-emerald-200 bg-white px-4 py-3 text-right shadow-sm">
            <p class="text-xs font-medium uppercase tracking-[0.2em] text-emerald-700">Resumo</p>
            <p class="mt-1 text-3xl font-semibold text-stone-900">${state.contacts.length}</p>
            <p class="text-xs text-stone-500">contatos cadastrados</p>
          </div>
        </div>
      </div>

      <div class="rounded-3xl border border-stone-200 bg-white p-6 shadow-sm">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="text-sm font-medium uppercase tracking-[0.2em] text-emerald-700">Agenda</p>
            <h2 class="mt-2 text-2xl font-semibold text-stone-900">Lista principal</h2>
          </div>
          <button type="button" id="refresh-contacts" class="rounded-2xl border border-emerald-300 px-4 py-2 text-sm font-medium text-emerald-800 transition hover:bg-emerald-100">Atualizar contatos</button>
        </div>
        ${renderAlert(state.successMessage, 'border-emerald-200 bg-emerald-50 text-emerald-700')}
        ${renderAlert(state.errorMessage, 'border-red-200 bg-red-50 text-red-700')}
        ${renderContactsList()}
      </div>

      <div class="rounded-3xl border border-stone-200 bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="text-sm font-medium uppercase tracking-[0.2em] text-emerald-700">${isEditing ? 'Editar contato' : 'Novo contato'}</p>
            <p class="mt-1 text-sm text-stone-500">${isEditing ? 'Revise os dados e salve as alteracoes usando somente a API JSON.' : 'Preencha os dados principais e informe um telefone por linha.'}</p>
          </div>
          ${isEditing ? '<button type="button" id="cancel-edit" class="rounded-2xl border border-stone-300 px-4 py-2 text-sm font-medium text-stone-700 transition hover:bg-stone-100">Cancelar edicao</button>' : ''}
        </div>
        <form id="contact-form" class="mt-4 space-y-4">
          <div class="grid gap-4 sm:grid-cols-2">
            <label class="block text-sm text-stone-700"><span class="mb-1 block font-medium">Nome</span><input name="firstName" required value="${escapeHtml(state.form.firstName)}" class="w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white" /></label>
            <label class="block text-sm text-stone-700"><span class="mb-1 block font-medium">Sobrenome</span><input name="lastName" required value="${escapeHtml(state.form.lastName)}" class="w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white" /></label>
          </div>
          <label class="block text-sm text-stone-700"><span class="mb-1 block font-medium">Data de nascimento</span><input name="birthDate" type="date" required value="${escapeHtml(state.form.birthDate)}" class="w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white" /></label>
          <label class="block text-sm text-stone-700"><span class="mb-1 block font-medium">Telefones</span><textarea name="phoneNumbers" required rows="3" placeholder="Um telefone por linha" class="w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white">${escapeHtml(state.form.phoneNumbers)}</textarea></label>
          <label class="block text-sm text-stone-700"><span class="mb-1 block font-medium">Grau de parentesco</span><input name="relationshipDegree" value="${escapeHtml(state.form.relationshipDegree)}" class="w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white" /></label>
          <button type="submit" class="w-full rounded-2xl bg-emerald-700 px-4 py-3 font-medium text-white transition hover:bg-emerald-600">${isEditing ? 'Salvar alteracoes' : 'Salvar contato'}</button>
        </form>
      </div>
    `;
  }

  function bindContactsPanelEvents() {
    const refreshContactsButton = document.getElementById('refresh-contacts');
    const cancelEditButton = document.getElementById('cancel-edit');
    const contactForm = document.getElementById('contact-form');

    refreshContactsButton?.addEventListener('click', async () => {
      await loadContacts();
    });

    cancelEditButton?.addEventListener('click', () => {
      state.form = emptyForm();
      state.errorMessage = '';
      renderContactsPanel();
      bindContactsPanelEvents();
    });

    contactForm?.addEventListener('submit', async event => {
      event.preventDefault();

      const formData = new FormData(contactForm);
      const payload = {
        firstName: String(formData.get('firstName') || ''),
        lastName: String(formData.get('lastName') || ''),
        birthDate: String(formData.get('birthDate') || ''),
        phoneNumbers: splitPhoneNumbers(String(formData.get('phoneNumbers') || '')),
        relationshipDegree: String(formData.get('relationshipDegree') || '').trim() || null,
      };

      state.form = {
        mode: state.form.mode,
        contactId: state.form.contactId,
        firstName: payload.firstName,
        lastName: payload.lastName,
        birthDate: payload.birthDate,
        phoneNumbers: joinPhoneNumbers(payload.phoneNumbers),
        relationshipDegree: payload.relationshipDegree || '',
      };

      if (state.form.mode === 'edit') {
        await updateContact(payload);
        return;
      }

      await createContact(payload);
    });

    contactsPanel.querySelectorAll('[data-action="edit"]').forEach(button => {
      button.addEventListener('click', () => {
        const contactId = Number(button.getAttribute('data-contact-id'));
        const contact = state.contacts.find(item => item.id === contactId);
        if (!contact) {
          setPanelError('Contato nao encontrado.');
          bindContactsPanelEvents();
          return;
        }

        state.successMessage = '';
        state.errorMessage = '';
        state.form = {
          mode: 'edit',
          contactId: contact.id,
          firstName: contact.firstName,
          lastName: contact.lastName,
          birthDate: contact.birthDate,
          phoneNumbers: joinFormattedPhoneNumbers(contact.phoneNumbers),
          relationshipDegree: contact.relationshipDegree || '',
        };
        renderContactsPanel();
        bindContactsPanelEvents();
      });
    });

    contactsPanel.querySelectorAll('[data-action="delete"]').forEach(button => {
      button.addEventListener('click', async () => {
        const contactId = Number(button.getAttribute('data-contact-id'));
        await deleteContact(contactId);
      });
    });
  }

  async function requestJson(url, options = {}) {
    const response = await fetch(url, {
      headers: {
        Accept: 'application/json',
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(options.headers || {}),
      },
      ...options,
    });

    if (response.status === 401) {
      redirectToLogin();
      throw new Error('Sessao invalida.');
    }

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get('content-type') || '';
    const body = contentType.includes('application/json') ? await response.json() : null;

    if (!response.ok) {
      throw new Error(body?.message || 'Nao foi possivel concluir a operacao.');
    }

    return body;
  }

  async function loadCurrentUser() {
    clearPageError();

    try {
      const user = await requestJson('/api/users/me', { method: 'GET' });
      renderUser(user);
    } catch (error) {
      if (error.message !== 'Sessao invalida.') {
        showPageError('Nao foi possivel carregar sua sessao.');
      }
    }
  }

  async function loadContacts() {
    try {
      const contacts = await requestJson('/api/contacts', { method: 'GET' });
      state.contacts = contacts || [];
      renderContactsPanel();
      bindContactsPanelEvents();
    } catch (error) {
      if (error.message !== 'Sessao invalida.') {
        contactsPanel.innerHTML = '<div class="rounded-3xl border border-red-200 bg-red-50 p-6 shadow-sm"><p class="text-sm font-medium text-red-700">Nao foi possivel carregar os contatos agora.</p></div>';
      }
    }
  }

  async function createContact(payload) {
    try {
      await requestJson('/api/contacts', {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      state.form = emptyForm();
      setPanelSuccess('Contato salvo com sucesso.');
      await loadContacts();
      state.successMessage = 'Contato salvo com sucesso.';
      renderContactsPanel();
      bindContactsPanelEvents();
    } catch (error) {
      if (error.message !== 'Sessao invalida.') {
        setPanelError(error.message);
        bindContactsPanelEvents();
      }
    }
  }

  async function updateContact(payload) {
    try {
      await requestJson(`/api/contacts/${state.form.contactId}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      });

      state.form = emptyForm();
      state.successMessage = 'Contato atualizado com sucesso.';
      state.errorMessage = '';
      await loadContacts();
      state.successMessage = 'Contato atualizado com sucesso.';
      renderContactsPanel();
      bindContactsPanelEvents();
    } catch (error) {
      if (error.message !== 'Sessao invalida.') {
        setPanelError(error.message);
        bindContactsPanelEvents();
      }
    }
  }

  async function deleteContact(contactId) {
    try {
      await requestJson(`/api/contacts/${contactId}`, {
        method: 'DELETE',
      });

      if (state.form.contactId === contactId) {
        state.form = emptyForm();
      }

      state.successMessage = 'Contato excluido com sucesso.';
      state.errorMessage = '';
      await loadContacts();
      state.successMessage = 'Contato excluido com sucesso.';
      renderContactsPanel();
      bindContactsPanelEvents();
    } catch (error) {
      if (error.message !== 'Sessao invalida.') {
        setPanelError(error.message);
        bindContactsPanelEvents();
      }
    }
  }

  refreshUserButton.addEventListener('click', async () => {
    await loadCurrentUser();
  });

  logoutButton.addEventListener('click', async () => {
    clearPageError();

    try {
      const response = await fetch('/api/auth/session', { method: 'DELETE' });

      if (!response.ok) {
        showPageError('Nao foi possivel encerrar a sessao agora.');
        return;
      }

      redirectToLogin();
    } catch (error) {
      showPageError('Nao foi possivel encerrar a sessao agora.');
    }
  });

  renderContactsPanel();
  bindContactsPanelEvents();
  loadCurrentUser();
  loadContacts();
})();