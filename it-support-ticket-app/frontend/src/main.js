import './styles.css';

const API_BASE = 'http://localhost:8080/api';
const DEFAULT_TICKET_IDS = [1, 2, 3];

const app = document.querySelector('#app');

const state = {
  currentUserId: 1,
  currentRole: 'EMPLOYEE',
  tickets: [],
  selectedTicketId: null,
  loadingTickets: false,
  busyAction: '',
  successMessage: '',
  errorMessage: '',
};

function setSuccess(message) {
  state.successMessage = message;
  state.errorMessage = '';
}

function setError(message) {
  state.errorMessage = message;
  state.successMessage = '';
}

function isRole(role) {
  return state.currentRole === role;
}

function getCurrentUserId() {
  return Number(state.currentUserId) || 1;
}

function formatStatus(status) {
  return status ? status.replace('_', ' ') : 'UNKNOWN';
}

function formatPriority(priority) {
  return priority || 'MEDIUM';
}

function getStatusClass(status) {
  const normalized = (status || '').toUpperCase();
  const mapping = {
    OPEN: 'status-open',
    ASSIGNED: 'status-assigned',
    IN_PROGRESS: 'status-progress',
    RESOLVED: 'status-resolved',
  };
  return mapping[normalized] || 'status-default';
}

function getPriorityClass(priority) {
  const normalized = (priority || '').toUpperCase();
  const mapping = {
    LOW: 'priority-low',
    MEDIUM: 'priority-medium',
    HIGH: 'priority-high',
    URGENT: 'priority-urgent',
  };
  return mapping[normalized] || 'priority-medium';
}

async function apiRequest(path, options = {}) {
  const headers = {
    ...(options.headers || {}),
    'Content-Type': 'application/json',
    'X-User-Id': String(getCurrentUserId()),
    'X-User-Role': state.currentRole,
  };

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = payload?.message || payload?.error || 'Request failed.';
    throw new Error(message);
  }

  return payload;
}

async function fetchTicketById(ticketId) {
  return apiRequest(`/tickets/${ticketId}`, { method: 'GET' });
}

async function loadTicketList() {
  state.loadingTickets = true;
  state.errorMessage = '';
  render();

  try {
    const ids = state.tickets.length ? state.tickets.map((ticket) => ticket.id) : DEFAULT_TICKET_IDS;
    const uniqueIds = [...new Set(ids)];
    const ticketResponses = await Promise.all(uniqueIds.map((id) => fetchTicketById(id)));
    state.tickets = ticketResponses.sort((a, b) => Number(b.id) - Number(a.id));
    state.selectedTicketId = state.selectedTicketId || ticketResponses[0]?.id || null;

    if (!state.tickets.length) {
      setError('No tickets are available right now. Create the first one from the form below.');
    }
  } catch (error) {
    state.tickets = [];
    setError(error.message || 'Unable to load tickets from the backend.');
  } finally {
    state.loadingTickets = false;
    render();
  }
}

const ERROR_KEYS = {
  TICKET_ID_REQUIRED: 'tickets.validation.idRequired',
};

function upsertTicket(ticket) {
  const index = state.tickets.findIndex((item) => Number(item.id) === Number(ticket.id));
  if (index >= 0) {
    state.tickets[index] = ticket;
  } else {
    state.tickets.unshift(ticket);
  }
  state.selectedTicketId = ticket.id;
}

function getSelectedTicket() {
  return state.tickets.find((ticket) => Number(ticket.id) === Number(state.selectedTicketId)) || null;
}

async function handleCreateTicket(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const formData = new FormData(form);
  const payload = {
    requesterUserId: Number(formData.get('requesterUserId')),
    deviceId: Number(formData.get('deviceId')),
    title: String(formData.get('title') || '').trim(),
    description: String(formData.get('description') || '').trim(),
    priority: String(formData.get('priority') || '').trim() || 'MEDIUM',
  };

  if (!payload.requesterUserId || !payload.deviceId) {
    setError('Requester ID and Device ID are required.');
    render();
    return;
  }

  if (!payload.title) {
    setError('Title is required and cannot be blank.');
    render();
    return;
  }

  if (!payload.description) {
    setError('Description is required and cannot be blank.');
    render();
    return;
  }

  state.busyAction = 'create';
  setSuccess('');
  render();

  try {
    const ticket = await apiRequest('/tickets', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    upsertTicket(ticket);
    form.reset();
    setSuccess(`Ticket ${ticket.ticketNumber || ticket.id} created successfully.`);
  } catch (error) {
    setError(error.message || 'Ticket creation failed.');
  } finally {
    state.busyAction = '';
    render();
  }
}

async function handleAssignTicket(event) {
  event.preventDefault();
  const ticket = getSelectedTicket();
  if (!ticket) return;

  const technicianId = Number(new FormData(event.currentTarget).get('technicianUserId'));
  if (!technicianId) {
    setError('A technician user ID is required to assign a ticket.');
    render();
    return;
  }

  state.busyAction = 'assign';
  setSuccess('');
  render();

  try {
    const updated = await apiRequest(`/tickets/${ticket.id}/assign`, {
      method: 'POST',
      body: JSON.stringify({ technicianUserId: technicianId }),
    });
    upsertTicket(updated);
    setSuccess(`Ticket assigned to technician ${updated.assignedTechnicianUserId}.`);
  } catch (error) {
    setError(error.message || 'Assignment failed.');
  } finally {
    state.busyAction = '';
    render();
  }
}

async function handleStartTicket() {
  const ticket = getSelectedTicket();
  if (!ticket) return;

  state.busyAction = 'start';
  setSuccess('');
  render();

  try {
    const updated = await apiRequest(`/tickets/${ticket.id}/start`, {
      method: 'POST',
      body: JSON.stringify({}),
    });
    upsertTicket(updated);
    setSuccess(`Work started for ticket ${ticket.ticketNumber || ticket.id}.`);
  } catch (error) {
    setError(error.message || 'Unable to start work on this ticket.');
  } finally {
    state.busyAction = '';
    render();
  }
}

async function handleResolveTicket(event) {
  event.preventDefault();
  const ticket = getSelectedTicket();
  if (!ticket) return;

  const formData = new FormData(event.currentTarget);
  const resolutionNote = String(formData.get('resolutionNote') || '').trim();

  if (!resolutionNote) {
    setError('Resolution note is required before closing the ticket.');
    render();
    return;
  }

  state.busyAction = 'resolve';
  setSuccess('');
  render();

  try {
    const updated = await apiRequest(`/tickets/${ticket.id}/resolve`, {
      method: 'POST',
      body: JSON.stringify({ resolutionNote }),
    });
    upsertTicket(updated);
    setSuccess(`Ticket ${ticket.ticketNumber || ticket.id} resolved successfully.`);
  } catch (error) {
    setError(error.message || 'Ticket resolution failed.');
  } finally {
    state.busyAction = '';
    render();
  }
}

function renderTicketCards() {
  const tickets = state.tickets;

  if (!tickets.length) {
    return `
      <div class="empty-state">
        <h3>No tickets yet</h3>
        <p>No active ticket data was returned by the backend. Create a ticket or check the backend server.</p>
      </div>
    `;
  }

  return tickets
    .map((ticket) => {
      const selected = Number(ticket.id) === Number(state.selectedTicketId);
      return `
        <button class="ticket-card ${selected ? 'selected' : ''}" data-ticket-id="${ticket.id}" type="button">
          <div class="ticket-card-top">
            <span class="ticket-number">${ticket.ticketNumber || `T-${ticket.id}`}</span>
            <span class="status-pill ${getStatusClass(ticket.status)}">${formatStatus(ticket.status)}</span>
          </div>
          <h3>${ticket.title}</h3>
          <div class="ticket-meta">
            <span>Priority: <strong>${formatPriority(ticket.priority)}</strong></span>
            <span>Req: ${ticket.requesterUserId}</span>
          </div>
        </button>
      `;
    })
    .join('');
}

function renderDetailPanel() {
  const ticket = getSelectedTicket();
  if (!ticket) {
    return `
      <div class="empty-state detail-empty">
        <h3>Select a ticket</h3>
        <p>Choose a ticket from the list to review its status, assignment, and resolution details.</p>
      </div>
    `;
  }

  const canAssign = ticket.status === 'OPEN' && isRole('TECH_LEAD');
  const canStart = ticket.status === 'ASSIGNED' && isRole('IT_TECHNICIAN') && Number(ticket.assignedTechnicianUserId) === getCurrentUserId();
  const canResolve = ticket.status === 'IN_PROGRESS' && isRole('IT_TECHNICIAN') && Number(ticket.assignedTechnicianUserId) === getCurrentUserId();

  return `
    <div class="detail-header">
      <div>
        <p class="detail-label">Ticket</p>
        <h2>${ticket.ticketNumber || `T-${ticket.id}`}</h2>
      </div>
      <span class="status-pill ${getStatusClass(ticket.status)}">${formatStatus(ticket.status)}</span>
    </div>

    <div class="field-grid">
      <div class="field-item">
        <span class="label">Title</span>
        <strong>${ticket.title}</strong>
      </div>
      <div class="field-item">
        <span class="label">Priority</span>
        <strong class="priority-badge ${getPriorityClass(ticket.priority)}">${formatPriority(ticket.priority)}</strong>
      </div>
      <div class="field-item">
        <span class="label">Requester</span>
        <strong>${ticket.requesterUserId}</strong>
      </div>
      <div class="field-item">
        <span class="label">Device ID</span>
        <strong>${ticket.deviceId}</strong>
      </div>
      <div class="field-item">
        <span class="label">Assigned tech</span>
        <strong>${ticket.assignedTechnicianUserId ?? 'Unassigned'}</strong>
      </div>
      <div class="field-item">
        <span class="label">Updated</span>
        <strong>${new Date(ticket.updatedAt).toLocaleString()}</strong>
      </div>
    </div>

    <div class="detail-description">
      <span class="label">Description</span>
      <p>${ticket.description}</p>
    </div>

    <div class="detail-description">
      <span class="label">Resolution note</span>
      <p>${ticket.resolutionNote || 'No resolution note recorded yet.'}</p>
    </div>

    <div class="action-stack">
      ${canAssign ? `
        <form id="assign-form" class="action-form">
          <label>
            Technician user ID
            <input type="number" name="technicianUserId" min="1" required placeholder="e.g. 22" />
          </label>
          <button type="submit" class="primary-button" ${state.busyAction === 'assign' ? 'disabled' : ''}>
            ${state.busyAction === 'assign' ? 'Assigning...' : 'Assign technician'}
          </button>
        </form>
      ` : ''}

      ${canStart ? `
        <button type="button" id="start-ticket-button" class="primary-button" ${state.busyAction === 'start' ? 'disabled' : ''}>
          ${state.busyAction === 'start' ? 'Starting work...' : 'Start work'}
        </button>
      ` : ''}

      ${canResolve ? `
        <form id="resolve-form" class="action-form">
          <label>
            Resolution note
            <textarea name="resolutionNote" rows="4" placeholder="Describe the fix or resolution" required></textarea>
          </label>
          <button type="submit" class="primary-button" ${state.busyAction === 'resolve' ? 'disabled' : ''}>
            ${state.busyAction === 'resolve' ? 'Resolving...' : 'Resolve ticket'}
          </button>
        </form>
      ` : ''}
    </div>
  `;
}

function render() {
  const selectedTicket = getSelectedTicket();

  app.innerHTML = `
    <div class="page-shell">
      <aside class="sidebar">
        <div class="brand-block">
          <p class="eyebrow">IT Support Ticket</p>
          <h1>Ticket workflow</h1>
        </div>

        <nav class="nav">
          <button type="button" class="nav-button active">Overview</button>
        </nav>

        <div class="actor-card">
          <label>
            Current user ID
            <input id="actor-user-id" type="number" min="1" value="${state.currentUserId}" />
          </label>
          <label>
            Current role
            <select id="actor-role">
              ${['EMPLOYEE', 'TECH_LEAD', 'IT_TECHNICIAN', 'ADMIN'].map((role) => `
                <option value="${role}" ${state.currentRole === role ? 'selected' : ''}>${role}</option>
              `).join('')}
            </select>
          </label>
        </div>
      </aside>

      <main class="content-area">
        <header class="topbar">
          <div>
            <p class="topbar-label">Demo workspace</p>
            <h2>Ticket operations</h2>
          </div>
          <button type="button" id="refresh-tickets" class="secondary-button">Refresh tickets</button>
        </header>

        ${state.errorMessage ? `<div class="inline-message error">${state.errorMessage}</div>` : ''}
        ${state.successMessage ? `<div class="inline-message success">${state.successMessage}</div>` : ''}

        <div class="board-grid">
          <section class="panel ticket-panel">
            <div class="panel-header">
              <h3>Ticket list</h3>
            </div>
            ${state.loadingTickets ? '<div class="loader">Loading tickets...</div>' : renderTicketCards()}
          </section>

          <section class="panel details-panel">
            ${renderDetailPanel()}
          </section>
        </div>

        <section class="panel form-panel">
          <div class="panel-header">
            <h3>Create new ticket</h3>
          </div>
          <form id="create-ticket-form" class="stacked-form">
            <div class="form-grid">
              <label>
                Requester user ID
                <input type="number" name="requesterUserId" min="1" value="${state.currentUserId}" required />
              </label>
              <label>
                Device ID
                <input type="number" name="deviceId" min="1" value="1" required />
              </label>
              <label class="full-width">
                Title
                <input type="text" name="title" maxlength="120" placeholder="Printer not responding" required />
              </label>
              <label class="full-width">
                Description
                <textarea name="description" rows="4" placeholder="Describe the device issue" required></textarea>
              </label>
              <label>
                Priority
                <select name="priority">
                  ${['LOW', 'MEDIUM', 'HIGH', 'URGENT'].map((priority) => `<option value="${priority}">${priority}</option>`).join('')}
                </select>
              </label>
            </div>
            <div class="form-actions">
              <button type="submit" class="primary-button" ${state.busyAction === 'create' ? 'disabled' : ''}>
                ${state.busyAction === 'create' ? 'Creating ticket...' : 'Create ticket'}
              </button>
            </div>
          </form>
        </section>
      </main>
    </div>
  `;

  const ticketButtons = document.querySelectorAll('.ticket-card');
  ticketButtons.forEach((button) => {
    button.addEventListener('click', () => {
      state.selectedTicketId = button.dataset.ticketId;
      setSuccess('');
      render();
    });
  });

  const createForm = document.querySelector('#create-ticket-form');
  if (createForm) createForm.addEventListener('submit', handleCreateTicket);

  const assignForm = document.querySelector('#assign-form');
  if (assignForm) assignForm.addEventListener('submit', handleAssignTicket);

  const resolveForm = document.querySelector('#resolve-form');
  if (resolveForm) resolveForm.addEventListener('submit', handleResolveTicket);

  const startButton = document.querySelector('#start-ticket-button');
  if (startButton) startButton.addEventListener('click', handleStartTicket);

  const refreshButton = document.querySelector('#refresh-tickets');
  if (refreshButton) refreshButton.addEventListener('click', () => loadTicketList());

  const actorUserId = document.querySelector('#actor-user-id');
  if (actorUserId) {
    actorUserId.addEventListener('change', (event) => {
      state.currentUserId = Number(event.target.value) || 1;
      state.successMessage = '';
      state.errorMessage = '';
      render();
    });
  }

  const actorRole = document.querySelector('#actor-role');
  if (actorRole) {
    actorRole.addEventListener('change', (event) => {
      state.currentRole = event.target.value;
      state.successMessage = '';
      state.errorMessage = '';
      render();
    });
  }

  if (!selectedTicket && state.tickets.length) {
    state.selectedTicketId = state.tickets[0].id;
  }
}

window.addEventListener('DOMContentLoaded', () => {
  loadTicketList();
});
