/**
 * admin.js — Admin Panel Logic
 * NEC ClubApp
 */

// ── Auth Guard ────────────────────────────────────────────────────────────
const token = localStorage.getItem('token');
const role  = localStorage.getItem('role');
const name  = localStorage.getItem('name');

if (!token)          { window.location.href = '/index.html'; }
if (role !== 'ADMIN') { window.location.href = '/dashboard.html'; }

// ── State ─────────────────────────────────────────────────────────────────
let allAdminClubs  = [];
let allUsers       = [];
let currentClubId  = null;

// ── Init ──────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    const user = api.getUser();
    if (user) {
        document.getElementById('admin-name').textContent = user.name;
        document.getElementById('admin-greet').textContent = user.name;
        document.getElementById('admin-email').textContent = user.email;
        document.getElementById('sidebar-avatar').textContent = user.name.charAt(0).toUpperCase();
    }
    loadOverview();
});

// ── Navigation ────────────────────────────────────────────────────────────
function showAdminSection(key) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.getElementById(`section-${key}`).classList.add('active');
    const navEl = document.getElementById(`nav-${key}`);
    if (navEl) navEl.classList.add('active');

    if (key === 'overview') loadOverview();
    if (key === 'clubs')    loadAdminClubs();
    if (key === 'users')    loadUsers();
    if (key === 'settings') loadCollegeSettings();
}

// ── Overview ──────────────────────────────────────────────────────────────
async function loadOverview() {
    try {
        const data = await api.get('/admin/overview');

        // Stats
        document.getElementById('admin-stats').innerHTML = [
            { icon: '🏛', value: data.totalClubs,  label: 'Total Clubs'  },
            { icon: '👥', value: data.totalUsers,  label: 'Total Users'  },
            { icon: '🎉', value: data.totalEvents, label: 'Total Events' },
            { icon: '🔴', value: data.ongoingCount,label: 'Ongoing Events'},
        ].map(s => `
            <div class="stat-card">
                <div class="stat-icon">${s.icon}</div>
                <div class="stat-value">${s.value}</div>
                <div class="stat-label">${s.label}</div>
            </div>`).join('');

        // Event posters
        const grid = document.getElementById('admin-events-grid');
        if (!data.ongoingEvents.length) {
            grid.innerHTML = `<div class="empty-state"><div class="empty-icon">📭</div><p>No upcoming events.</p></div>`;
            return;
        }
        grid.innerHTML = data.ongoingEvents.map(ev => adminEventPosterCard(ev)).join('');
    } catch(e) {
        console.error(e);
    }
}

function adminEventPosterCard(ev) {
    const bg = ev.posterImage
        ? `<img src="/uploads/${ev.posterImage}" alt="${ev.name}" style="width:100%;height:100%;object-fit:cover;position:absolute;inset:0;"/>`
        : `<div class="poster-overlay"><div class="poster-event-name">${ev.name}</div><div class="poster-club-name">${ev.clubName}</div></div>`;
    return `
        <div class="poster-card" onclick="openAdminEventDetail(${ev.id}, ${ev.clubId})">
            <div class="poster-image">${bg}</div>
            <div class="poster-body">
                <div class="poster-meta">
                    <span>📅 ${ev.date || '—'} ${ev.time ? '· '+ev.time : ''}</span>
                    <span>📍 ${ev.venue || '—'}</span>
                    <span>🏛 ${ev.clubName}</span>
                </div>
            </div>
            <div class="poster-footer">
                <span class="attendee-count">👥 ${ev.attendeeCount} registered</span>
                ${ev.membersOnly ? '<span class="members-only-tag">Members Only</span>' : ''}
            </div>
        </div>`;
}

// ── All Clubs ─────────────────────────────────────────────────────────────
async function loadAdminClubs() {
    const grid = document.getElementById('admin-clubs-grid');
    grid.innerHTML = `<div class="loading-spinner">Loading clubs...</div>`;
    try {
        allAdminClubs = await api.get('/admin/clubs');
        renderAdminClubs(allAdminClubs);
        populateCoordinatorDropdown();
    } catch(e) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Failed to load clubs.</p></div>`;
    }
}

function renderAdminClubs(clubs) {
    const grid = document.getElementById('admin-clubs-grid');
    if (!clubs.length) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-icon">🏛</div><p>No clubs yet. Create one!</p></div>`;
        return;
    }
    grid.innerHTML = clubs.map(c => {
        const initials = c.name.split(' ').map(w=>w[0]).join('').slice(0,3).toUpperCase();
        const logo = c.logoImage
            ? `<img src="/uploads/${c.logoImage}" alt="${c.name}"/>`
            : initials;
        return `
            <div class="admin-club-card" onclick="openAdminClubDetail(${c.id})">
                <div class="admin-club-header">
                    <div class="admin-club-logo">${logo}</div>
                    <div>
                        <div class="admin-club-name">${c.name}</div>
                        <div class="admin-club-dept">${c.department === 'ALL' ? 'All Departments' : (c.department || 'General')}</div>
                        <div class="admin-club-coord">👤 ${c.coordinatorName}</div>
                    </div>
                </div>
                <div style="font-size:0.82rem;color:var(--text-muted);display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">
                    ${c.description || 'No description.'}
                </div>
                <div class="admin-club-stats">
                    <span>👥 ${c.memberCount} members</span>
                    <span>🎉 ${c.eventCount} events</span>
                    ${c.foundedYear ? `<span>📅 Est. ${c.foundedYear}</span>` : ''}
                </div>
            </div>`;
    }).join('');
}

function filterAdminClubs() {
    const q    = document.getElementById('admin-club-search').value.toLowerCase();
    const dept = document.getElementById('admin-club-dept').value;
    const filtered = allAdminClubs.filter(c =>
        (!q    || c.name.toLowerCase().includes(q)) &&
        (!dept || c.department === dept)
    );
    renderAdminClubs(filtered);
}

async function deleteClub(id, name) {
    if (!confirm(`Are you absolutely sure you want to delete "${name}"? This will remove all events and data associated with this club.`)) return;
    try {
        await api.delete(`/clubs/${id}`);
        showToast(`Club "${name}" deleted successfully.`, 'success');
        showAdminSection('clubs');
    } catch(err) {
        showToast(err.error || 'Failed to delete club.', 'error');
    }
}

// ── Club Detail ────────────────────────────────────────────────────────────
async function openAdminClubDetail(clubId) {
    currentClubId = clubId;
    showRawSection('section-club-detail');

    const container = document.getElementById('admin-club-detail');
    container.innerHTML = `<div class="loading-spinner">Loading club details...</div>`;

    try {
        const [club, ongoing, past, members] = await Promise.all([
            api.get(`/admin/clubs/${clubId}`),
            api.get(`/admin/clubs/${clubId}/events/ongoing`),
            api.get(`/admin/clubs/${clubId}/events/past`),
            api.get(`/admin/clubs/${clubId}/members`)
        ]);

        const initials = club.name.split(' ').map(w=>w[0]).join('').slice(0,3).toUpperCase();
        const logo = club.logoImage
            ? `<img src="/uploads/${club.logoImage}" alt="${club.name}"/>`
            : initials;

        container.innerHTML = `
            <!-- Club Header -->
            <div class="club-detail-header">
                <div class="club-detail-logo">${logo}</div>
                <div class="club-detail-info" style="flex:1;">
                    <h2>${club.name}</h2>
                    <p>${club.description || ''}</p>
                    <div class="club-detail-badges">
                        ${club.department ? `<span class="club-badge">🏫 ${club.department === 'ALL' ? 'All Departments' : club.department}</span>` : ''}
                        ${club.foundedYear ? `<span class="club-badge">📅 Est. ${club.foundedYear}</span>` : ''}
                        <span class="club-badge">👥 ${club.memberCount} members</span>
                        <span class="club-badge">🎉 ${club.eventCount} events</span>
                    </div>
                </div>
                <div style="display:flex;flex-direction:column;gap:0.5rem;align-items:flex-end;">
                    <div style="text-align:right;">
                        <div style="font-size:0.75rem;opacity:0.7;">Coordinator</div>
                        <div style="font-weight:600;">${club.coordinatorName}</div>
                        <div style="font-size:0.78rem;opacity:0.7;">${club.coordinatorEmail}</div>
                    </div>
                    <div style="display:flex;gap:0.5rem;margin-top:0.25rem;">
                        <button class="btn btn-primary btn-sm" onclick="openEditClubModal(${club.id})">✏️ Edit</button>
                        <button class="btn btn-gold btn-sm" onclick="openChangeCoordinator(${club.id})">🔄 Change</button>
                        <button class="btn btn-danger btn-sm" onclick="deleteClub(${club.id}, '${club.name.replace(/'/g, "\\'")}')">🗑 Delete</button>
                    </div>
                </div>
            </div>

            ${club.vision ? `
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;margin-bottom:1.5rem;">
                <div style="background:var(--bg-card);border-radius:var(--radius);padding:1.25rem;box-shadow:var(--shadow-sm);">
                    <div style="font-size:0.72rem;font-weight:600;color:var(--nec-blue);text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px;">Vision</div>
                    <p style="font-size:0.88rem;color:var(--text-secondary);">${club.vision}</p>
                </div>
                <div style="background:var(--bg-card);border-radius:var(--radius);padding:1.25rem;box-shadow:var(--shadow-sm);">
                    <div style="font-size:0.72rem;font-weight:600;color:var(--nec-blue);text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px;">Mission</div>
                    <p style="font-size:0.88rem;color:var(--text-secondary);">${club.mission || '—'}</p>
                </div>
            </div>` : ''}

            <!-- Tabs -->
            <div class="tabs" id="club-detail-tabs">
                <button class="tab-pill active" onclick="switchAdminClubTab(this,'adt-ongoing')">🎯 Ongoing Events (${ongoing.length})</button>
                <button class="tab-pill" onclick="switchAdminClubTab(this,'adt-past')">🗂 Past Events (${past.length})</button>
                <button class="tab-pill" onclick="switchAdminClubTab(this,'adt-members')">👥 Members (${members.length})</button>
            </div>

            <!-- Ongoing Events -->
            <div id="adt-ongoing" class="tab-content active">
                ${ongoing.length
                    ? `<div class="poster-grid">${ongoing.map(ev => adminEventPosterCard(ev)).join('')}</div>`
                    : `<div class="empty-state"><div class="empty-icon">📭</div><p>No ongoing events.</p></div>`}
            </div>

            <!-- Past Events -->
            <div id="adt-past" class="tab-content">
                <div class="filter-bar">
                    <input type="month" id="admin-past-month" onchange="filterAdminPast(${JSON.stringify(past).replace(/"/g,'&quot;')})"/>
                </div>
                <div id="admin-past-grid" class="poster-grid">
                    ${past.length
                        ? past.map(ev => adminEventPosterCard(ev)).join('')
                        : `<div class="empty-state"><div class="empty-icon">📭</div><p>No past events.</p></div>`}
                </div>
            </div>

            <!-- Members -->
            <div id="adt-members" class="tab-content">
                <table class="data-table">
                    <thead><tr><th>#</th><th>Name</th><th>Email</th><th>Dept</th><th>Year</th></tr></thead>
                    <tbody>
                        ${members.length
                            ? members.map((m,i) => `
                                <tr>
                                    <td>${i+1}</td>
                                    <td>${m.name}</td>
                                    <td>${m.email}</td>
                                    <td>${m.department || '—'}</td>
                                    <td>${m.year || '—'}</td>
                                </tr>`).join('')
                            : `<tr><td colspan="5" style="text-align:center;color:var(--text-muted);">No members yet.</td></tr>`}
                    </tbody>
                </table>
            </div>`;
    } catch(e) {
        container.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Failed to load club details.</p></div>`;
    }
}

function filterAdminPast(past) {
    const month    = document.getElementById('admin-past-month').value;
    const filtered = month ? past.filter(e => e.date && e.date.startsWith(month)) : past;
    document.getElementById('admin-past-grid').innerHTML = filtered.length
        ? filtered.map(ev => adminEventPosterCard(ev)).join('')
        : `<div class="empty-state"><div class="empty-icon">📭</div><p>No events.</p></div>`;
}

function switchAdminClubTab(btn, tabId) {
    btn.closest('.tabs').querySelectorAll('.tab-pill').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('#admin-club-detail .tab-content').forEach(t => t.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById(tabId).classList.add('active');
}

// ── Event Detail ──────────────────────────────────────────────────────────
async function openAdminEventDetail(eventId, clubId) {
    showRawSection('section-event-detail');
    const container = document.getElementById('admin-event-detail');
    container.innerHTML = `<div class="loading-spinner">Loading event...</div>`;

    try {
        const [ev, attendees] = await Promise.all([
            api.get(`/admin/events/${eventId}`),
            api.get(`/admin/events/${eventId}/attendees`)
        ]);

        const posterHTML = ev.posterImage
            ? `<img src="/uploads/${ev.posterImage}" style="width:100%;display:block;border-radius:var(--radius);" alt="${ev.name}"/>`
            : `<div style="min-height:280px;background:linear-gradient(135deg,var(--nec-blue),var(--nec-blue-light));border-radius:var(--radius);display:flex;flex-direction:column;align-items:center;justify-content:center;padding:2rem;">
                   <div style="font-size:1.5rem;font-weight:700;color:white;text-align:center;">${ev.name}</div>
                   <div style="font-size:0.9rem;color:var(--nec-gold);margin-top:8px;">${ev.clubName}</div>
               </div>`;

        container.innerHTML = `
            <h1 style="font-size:1.5rem;font-weight:700;color:var(--nec-blue);margin-bottom:1.5rem;">${ev.name}</h1>
            <div class="event-mindmap">
                <div>${posterHTML}</div>
                <div class="event-mindmap-nodes">
                    <div class="mindmap-node"><div class="mindmap-node-label">🏛 Club</div><div class="mindmap-node-value">${ev.clubName}</div></div>
                    <div class="mindmap-node"><div class="mindmap-node-label">📅 Date</div><div class="mindmap-node-value">${ev.date || '—'}</div></div>
                    <div class="mindmap-node"><div class="mindmap-node-label">⏰ Time</div><div class="mindmap-node-value">${ev.time || '—'}</div></div>
                    <div class="mindmap-node"><div class="mindmap-node-label">📍 Venue</div><div class="mindmap-node-value">${ev.venue || '—'}</div></div>
                    <div class="mindmap-node"><div class="mindmap-node-label">👥 Registered</div><div class="mindmap-node-value">${ev.attendeeCount} people</div></div>
                    <div class="mindmap-node"><div class="mindmap-node-label">🔒 Access</div><div class="mindmap-node-value">${ev.membersOnly ? 'Members Only' : 'Open to All'}</div></div>
                    ${ev.description ? `<div class="mindmap-node"><div class="mindmap-node-label">📝 About</div><div class="mindmap-node-value">${ev.description}</div></div>` : ''}
                    ${ev.winners ? `<div class="mindmap-node" style="border-left-color:var(--nec-gold);"><div class="mindmap-node-label">🏆 Winners</div><div class="mindmap-node-value">
                        <ul style="margin:0;padding-left:1.25rem;">
                            ${ev.winners.split(',').map(w => `<li>${w.trim()}</li>`).join('')}
                        </ul>
                    </div></div>` : ''}
                </div>
            </div>

            <hr class="divider"/>

            <div class="section-header">
                <h2>👥 Registered Attendees (${attendees.length})</h2>
            </div>
            ${attendees.length
                ? `<table class="data-table">
                    <thead><tr><th>#</th><th>Name</th><th>Email</th><th>Dept</th><th>Year</th></tr></thead>
                    <tbody>
                        ${attendees.map((a,i) => `
                            <tr>
                                <td>${i+1}</td>
                                <td>${a.name}</td>
                                <td>${a.email}</td>
                                <td>${a.department || '—'}</td>
                                <td>${a.year || '—'}</td>
                            </tr>`).join('')}
                    </tbody>
                  </table>`
                : `<div class="empty-state"><div class="empty-icon">📭</div><p>No registrations yet.</p></div>`}`;
    } catch(e) {
        container.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Failed to load event.</p></div>`;
    }
}

function goBackToClub() {
    if (currentClubId) openAdminClubDetail(currentClubId);
    else showAdminSection('clubs');
}

// ── Users ─────────────────────────────────────────────────────────────────
async function loadUsers() {
    try {
        allUsers = await api.get('/admin/users');
        renderUsers(allUsers);
    } catch(e) {
        document.getElementById('users-tbody').innerHTML =
            `<tr><td colspan="7" style="text-align:center;color:var(--danger);">Failed to load users.</td></tr>`;
    }
}

function renderUsers(users) {
    const tbody = document.getElementById('users-tbody');
    if (!users.length) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--text-muted);">No users found.</td></tr>`;
        return;
    }
    tbody.innerHTML = users.map((u, i) => `
        <tr>
            <td>${i+1}</td>
            <td><strong>${u.name}</strong></td>
            <td>${u.email}</td>
            <td><span class="role-badge ${u.role}" style="font-size:0.68rem;">${u.role}</span></td>
            <td>${u.department || '—'}</td>
            <td>${u.year || '—'}</td>
            <td>
                ${u.role === 'ADMIN'
                    ? `<button class="btn btn-danger btn-sm" onclick="forceDeleteAdmin(${u.id},'${u.name}')" title="Requires admin secret key">🔐 Force Delete</button>`
                    : `
                    ${u.role === 'STUDENT' ? `<button class="btn btn-ghost btn-sm" onclick="promoteUser(${u.id},'${u.name}')">Promote</button>` : ''}
                    <button class="btn btn-danger btn-sm" onclick="deleteUser(${u.id},'${u.name}')">🗑</button>
                `}
            </td>
        </tr>`).join('');
}

function filterUsers() {
    const q    = document.getElementById('user-search').value.toLowerCase();
    const role = document.getElementById('user-role-filter').value;
    const filtered = allUsers.filter(u =>
        (!q    || u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q)) &&
        (!role || u.role === role)
    );
    renderUsers(filtered);
}

async function deleteUser(id, uname) {
    if (!confirm(`Delete user "${uname}"? This cannot be undone.`)) return;
    try {
        await api.delete(`/admin/users/${id}`);
        showToast(`${uname} deleted.`, 'success');
        loadUsers();
    } catch(err) {
        showToast(err.error || 'Failed.', 'error');
    }
}

async function forceDeleteAdmin(id, uname) {
    if (!confirm(`⚠️ You are about to delete the ADMIN account "${uname}".\n\nThis requires the admin secret key. Proceed?`)) return;
    const secret = prompt(`Enter the Admin Secret Key to confirm deletion of "${uname}":`);
    if (!secret) { showToast('Cancelled.', 'error'); return; }
    try {
        const res = await fetch(`/api/admin/users/${id}/force`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'X-Admin-Secret': secret
            }
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({ error: res.statusText }));
            throw err;
        }
        showToast(`Admin "${uname}" has been removed.`, 'success');
        loadUsers();
    } catch(err) {
        showToast(err.error || 'Failed. Check your secret key.', 'error');
    }
}

async function promoteUser(id, uname) {
    if (!confirm(`Promote "${uname}" to Coordinator?`)) return;
    try {
        await api.put(`/admin/users/${id}/promote`, {});
        showToast(`${uname} is now a Coordinator!`, 'success');
        loadUsers();
    } catch(err) {
        showToast(err.error || 'Failed.', 'error');
    }
}

// ── Create Club ───────────────────────────────────────────────────────────
async function populateCoordinatorDropdown() {
    try {
        const coordinators = await api.get('/admin/users/coordinators');
        const sel = document.getElementById('cc-coordinator');
        if (!coordinators.length) {
            sel.innerHTML = `<option value="">No coordinators — create one first</option>`;
            return;
        }
        sel.innerHTML = coordinators.map(c =>
            `<option value="${c.email}">${c.name} (${c.email}) — ${c.department || 'N/A'}</option>`
        ).join('');
    } catch(e) { console.error(e); }
}

async function submitCreateClub() {
    const name = document.getElementById('cc-name').value.trim();
    const coordEmail = document.getElementById('cc-coordinator').value;
    if (!name)       { showToast('Club name is required.', 'error'); return; }
    if (!coordEmail) { showToast('Please assign a coordinator.', 'error'); return; }
    try {
        await api.post('/clubs', {
            name,
            department:       document.getElementById('cc-dept').value,
            foundedYear:      parseInt(document.getElementById('cc-year').value) || null,
            description:      document.getElementById('cc-desc').value,
            vision:           document.getElementById('cc-vision').value,
            mission:          document.getElementById('cc-mission').value,
            coordinatorEmail: coordEmail
        });
        closeModal('modal-create-club');
        showToast('Club created!', 'success');
        document.getElementById('cc-name').value = '';
        document.getElementById('cc-dept').value = 'General';
        document.getElementById('cc-year').value = '';
        document.getElementById('cc-desc').value = '';
        document.getElementById('cc-vision').value = '';
        document.getElementById('cc-mission').value = '';
        loadAdminClubs();
    } catch(err) {
        showToast(err.error || err.message || 'Failed to create club.', 'error');
    }
}

async function openEditClubModal(clubId) {
    currentClubId = clubId;
    try {
        const club = await api.get(`/admin/clubs/${clubId}`);
        document.getElementById('ec-name').value = club.name;
        document.getElementById('ec-dept').value = club.department || 'General';
        document.getElementById('ec-year').value = club.foundedYear || '';
        document.getElementById('ec-desc').value = club.description || '';
        document.getElementById('ec-vision').value = club.vision || '';
        document.getElementById('ec-mission').value = club.mission || '';
        openModal('modal-edit-club');
    } catch(e) {
        showToast('Failed to load club data.', 'error');
    }
}

async function submitEditClub() {
    const name = document.getElementById('ec-name').value.trim();
    if (!name) { showToast('Club name is required.', 'error'); return; }
    try {
        await api.put(`/clubs/${currentClubId}`, {
            name,
            department:  document.getElementById('ec-dept').value,
            foundedYear: parseInt(document.getElementById('ec-year').value) || null,
            description: document.getElementById('ec-desc').value,
            vision:      document.getElementById('ec-vision').value,
            mission:     document.getElementById('ec-mission').value
        });
        closeModal('modal-edit-club');
        showToast('Club updated!', 'success');
        openAdminClubDetail(currentClubId);
    } catch(err) {
        showToast(err.error || 'Failed to update club.', 'error');
    }
}

// ── Create Coordinator ────────────────────────────────────────────────────
async function submitCreateCoordinator() {
    const name     = document.getElementById('nc-name').value.trim();
    const email    = document.getElementById('nc-email').value.trim();
    const password = document.getElementById('nc-password').value;
    if (!name || !email || !password) {
        showToast('All fields are required.', 'error'); return;
    }
    try {
        await api.post('/admin/users/coordinator', {
            name, email, password,
            department: document.getElementById('nc-dept').value
        });
        closeModal('modal-create-coordinator');
        showToast('Coordinator account created!', 'success');
        document.getElementById('nc-name').value = '';
        document.getElementById('nc-email').value = '';
        document.getElementById('nc-password').value = '';
        document.getElementById('nc-dept').value = 'CSE';
        loadUsers();
    } catch(err) {
        showToast(err.error || 'Failed.', 'error');
    }
}

// ── Create Admin ──────────────────────────────────────────────────────────
async function submitCreateAdmin() {
    const name     = document.getElementById('na-name').value.trim();
    const email    = document.getElementById('na-email').value.trim();
    const password = document.getElementById('na-password').value;
    const secret   = document.getElementById('na-secret').value;
    if (!name || !email || !password || !secret) {
        showToast('All fields including the secret key are required.', 'error'); return;
    }
    try {
        const res = await fetch('/api/admin/users/admin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'X-Admin-Secret': secret
            },
            body: JSON.stringify({ name, email, password })
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({ error: res.statusText }));
            throw err;
        }
        closeModal('modal-create-admin');
        showToast('Admin account created successfully!', 'success');
        document.getElementById('na-name').value = '';
        document.getElementById('na-email').value = '';
        document.getElementById('na-password').value = '';
        document.getElementById('na-secret').value = '';
        loadUsers();
    } catch(err) {
        showToast(err.error || 'Failed to create admin.', 'error');
    }
}

// ── Change Coordinator ────────────────────────────────────────────────────
async function openChangeCoordinator(clubId) {
    currentClubId = clubId;
    const sel = document.getElementById('change-coordinator-select');
    sel.innerHTML = `<option>Loading...</option>`;
    openModal('modal-change-coordinator');
    try {
        const coordinators = await api.get('/admin/users/coordinators');
        sel.innerHTML = coordinators.map(c =>
            `<option value="${c.email}">${c.name} (${c.email})</option>`
        ).join('');
    } catch(e) {
        sel.innerHTML = `<option>Failed to load coordinators.</option>`;
    }
}

async function submitChangeCoordinator() {
    const email = document.getElementById('change-coordinator-select').value;
    if (!email) { showToast('Please select a coordinator.', 'error'); return; }
    try {
        await api.put(`/admin/clubs/${currentClubId}/coordinator`, { email });
        closeModal('modal-change-coordinator');
        showToast('Coordinator updated!', 'success');
        openAdminClubDetail(currentClubId);
    } catch(err) {
        showToast(err.error || 'Failed.', 'error');
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────
function showRawSection(id) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(id).classList.add('active');
}

function openModal(id) {
    document.getElementById(id).classList.remove('hidden');
}

function closeModal(id) {
    document.getElementById(id).classList.add('hidden');
}

function logout() {
    localStorage.clear();
    window.location.href = '/index.html';
}

// ── Profile & Settings ──
async function openProfileModal() {
    const user = api.getUser();
    if (!user) return;
    
    document.getElementById('profile-modal-name').textContent = user.name;
    document.getElementById('profile-modal-email').textContent = user.email;
    document.getElementById('profile-avatar').textContent = user.name.charAt(0).toUpperCase();
    
    document.getElementById('prof-name').value = user.name;
    document.getElementById('prof-dept').value = user.department || '';
    
    const yearFields = document.getElementById('student-only-fields');
    if (user.role === 'STUDENT') {
        yearFields.style.display = 'block';
        document.getElementById('prof-year').value = user.year || '';
    } else {
        yearFields.style.display = 'none';
    }
    
    switchProfileTab('edit');
    openModal('modal-profile');
}

function switchProfileTab(tab) {
    document.getElementById('btn-tab-edit').classList.toggle('active', tab === 'edit');
    document.getElementById('btn-tab-pass').classList.toggle('active', tab === 'password');
    document.getElementById('profile-tab-edit').classList.toggle('active', tab === 'edit');
    document.getElementById('profile-tab-password').classList.toggle('active', tab === 'password');
}

async function updateProfile() {
    const name = document.getElementById('prof-name').value;
    const department = document.getElementById('prof-dept').value;
    const year = document.getElementById('prof-year') ? document.getElementById('prof-year').value : null;
    
    try {
        const res = await api.put('/users/me', { name, department, year });
        localStorage.setItem('name', res.name);
        localStorage.setItem('user', JSON.stringify(res));
        showToast('Profile updated!', 'success');
        setTimeout(() => location.reload(), 1000);
    } catch (e) {
        showToast(e.error || 'Failed to update profile', 'error');
    }
}

async function changePassword() {
    const currentPassword = document.getElementById('pass-current').value;
    const newPassword = document.getElementById('pass-new').value;
    const confirmPassword = document.getElementById('pass-confirm').value;
    
    if (!currentPassword || !newPassword || !confirmPassword) {
        showToast('All fields required.', 'error'); return;
    }
    if (newPassword !== confirmPassword) {
        showToast('Passwords do not match.', 'error'); return;
    }
    
    try {
        await api.put('/users/me/password', { currentPassword, newPassword, confirmPassword });
        showToast('Password updated!', 'success');
        closeModal('modal-profile');
    } catch (e) {
        showToast(e.error || 'Failed to change password.', 'error');
    }
}

async function loadCollegeSettings() {
    try {
        const data = await api.get('/college');
        document.getElementById('set-name').value = data.collegeName || '';
        document.getElementById('set-location').value = data.location || '';
        document.getElementById('set-est').value = data.established || '';
        document.getElementById('set-tnea').value = data.tneaCode || '';
        document.getElementById('set-web').value = data.website || '';
        document.getElementById('set-principal').value = data.principalName || '';
        document.getElementById('set-vision').value = data.vision || '';
        document.getElementById('set-mission').value = data.mission || '';
        
        if (data.logoImage) {
            document.getElementById('set-logo-preview').innerHTML = `<img src="/uploads/${data.logoImage}" alt="Logo"/>`;
        }
    } catch (e) {
        console.error(e);
    }
}

async function saveCollegeSettings() {
    const body = {
        collegeName: document.getElementById('set-name').value,
        location: document.getElementById('set-location').value,
        established: document.getElementById('set-est').value,
        tneaCode: document.getElementById('set-tnea').value,
        website: document.getElementById('set-web').value,
        principalName: document.getElementById('set-principal').value,
        vision: document.getElementById('set-vision').value,
        mission: document.getElementById('set-mission').value
    };
    
    try {
        await api.put('/college', body);
        
        const fileInput = document.getElementById('set-logo-file');
        if (fileInput.files.length > 0) {
            const formData = new FormData();
            formData.append('file', fileInput.files[0]);
            await api.uploadFile('/college/logo', formData);
        }
        
        showToast('Settings saved!', 'success');
        loadCollegeSettings();
    } catch (e) {
        showToast(e.error || 'Failed to save settings.', 'error');
    }
}

function showToast(msg, type = 'success') {
    api.showToast(msg, type);
}
