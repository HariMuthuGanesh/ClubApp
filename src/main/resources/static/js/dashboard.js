/**
 * dashboard.js — Student & Coordinator Logic
 * NEC ClubApp
 */

// ── Auth Guard ─────────────────────────────────────────────────────────────
const token = localStorage.getItem('token');
const role  = localStorage.getItem('role');
const name  = localStorage.getItem('name');

if (!token) { window.location.href = '/index.html'; }
if (role === 'ADMIN') { window.location.href = '/admin.html'; }

// ── State ──────────────────────────────────────────────────────────────────
let allClubs         = [];
let currentClubId    = null;
let currentEventId   = null;
let joinTargetClubId = null;
let prevSection      = 'section-all-clubs';
let myClubData       = null;
let activeEventId    = null;

// ── Init ───────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    buildSidebar();
    loadOverview();
});

// ── Sidebar ────────────────────────────────────────────────────────────────
function buildSidebar() {
    const user = api.getUser();
    if (!user) return;
    document.getElementById('sidebar-name').textContent = user.name;
    document.getElementById('sidebar-avatar').textContent = user.name.charAt(0).toUpperCase();
    const roleEl = document.getElementById('sidebar-role');
    roleEl.textContent = user.role;
    roleEl.className   = `role-badge ${user.role}`;

    const nav = document.getElementById('sidebar-nav');
    const items = role === 'COORDINATOR'
        ? [
            { icon: '🏠', label: 'Overview',  section: 'section-overview'  },
            { icon: '🏅', label: 'My Club',   section: 'section-my-club'   },
          ]
        : [
            { icon: '🏠', label: 'Overview',  section: 'section-overview'  },
            { icon: '🏛', label: 'All Clubs', section: 'section-all-clubs' },
            { icon: '⭐', label: 'My Clubs',  section: 'section-my-clubs'  },
          ];

    nav.innerHTML = items.map(i => `
        <div class="nav-item" id="nav-${i.section}" onclick="showSection('${i.section}')">
            <span class="nav-icon">${i.icon}</span>
            <span>${i.label}</span>
            ${i.section === 'section-my-club' ? '<span class="nav-badge hidden" id="pending-badge">0</span>' : ''}
        </div>
    `).join('');

    setActiveNav('section-overview');
}

function setActiveNav(section) {
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    const el = document.getElementById(`nav-${section}`);
    if (el) el.classList.add('active');
}

function showSection(id) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(id).classList.add('active');
    setActiveNav(id);

    if (id === 'section-overview')   loadOverview();
    if (id === 'section-all-clubs')  loadAllClubs();
    if (id === 'section-my-clubs')   loadMyClubs();
    if (id === 'section-my-club')    loadMyClub();
}

// ── Overview ───────────────────────────────────────────────────────────────
async function loadOverview() {
    document.getElementById('overview-greeting').textContent = `Welcome, ${name}! 👋`;
    const badge = document.getElementById('overview-role-badge');
    badge.textContent = role;
    badge.className   = `role-badge ${role}`;

    try {
        let events = [];
        let stats  = [];

        if (role === 'COORDINATOR') {
            const club = await api.get('/clubs/my-club');
            if (club && club.id) {
                myClubData = club;
                events = await api.get(`/clubs/${club.id}/events/ongoing`);
                stats = [
                    { icon: '👥', value: club.memberCount, label: 'Club Members' },
                    { icon: '🎉', value: club.eventCount, label: 'Total Events' },
                    { icon: '🎯', value: events.length, label: 'Ongoing Events' }
                ];
            }
            loadCoordinatorPending();
            loadNewsFeed();
        } else {
            events = await api.get('/events/ongoing');
            const myClubs = await api.get('/clubs/mine');
            stats = [
                { icon: '🎉', value: events.length, label: 'Upcoming Events' },
                { icon: '⭐', value: myClubs.length, label: 'My Clubs' },
                { icon: '🏛', value: allClubs.length || '—', label: 'Total Clubs' }
            ];
            loadNewsFeed();
            document.getElementById('coordinator-pending-block').classList.add('hidden');
        }

        renderOverviewStats(stats);
        renderEventPosters(events);
    } catch(e) { console.error(e); }
}

function renderOverviewStats(stats) {
    document.getElementById('overview-stats').innerHTML = stats.map(s => `
        <div class="stat-card">
            <div class="stat-icon">${s.icon}</div>
            <div class="stat-value">${s.value}</div>
            <div class="stat-label">${s.label}</div>
        </div>
    `).join('');
}

function renderEventPosters(events) {
    const container = document.getElementById('overview-events');
    if (!events.length) {
        container.innerHTML = `<div class="empty-state"><div class="empty-icon">📭</div><p>No upcoming events right now.</p></div>`;
        return;
    }
    container.innerHTML = events.map(ev => posterCardHTML(ev, true)).join('');
}

function posterCardHTML(ev, showRegister = false) {
    const posterBg = ev.posterImage
        ? `<img src="/uploads/${ev.posterImage}" alt="${ev.name}"/>`
        : `<div class="poster-overlay"><div class="poster-event-name">${ev.name}</div><div class="poster-club-name">${ev.clubName}</div></div>`;

    const action = (showRegister && role === 'STUDENT')
        ? (ev.registered
            ? `<span style="font-size:0.78rem;color:var(--success);font-weight:600;">✔ Registered</span>`
            : `<button class="btn btn-primary btn-sm" onclick="registerEvent(${ev.id}, event)">Register</button>`)
        : '';

    return `
        <div class="poster-card" onclick="openEventDetail(${ev.id}, ${ev.clubId})">
            <div class="poster-image">${posterBg}</div>
            <div class="poster-body">
                <div class="poster-meta">
                    <span>📅 ${ev.date || '—'} ${ev.time ? '· ' + ev.time : ''}</span>
                    <span>📍 ${ev.venue || '—'}</span>
                    <span>🏛 ${ev.clubName}</span>
                </div>
            </div>
            <div class="poster-footer">
                <span class="attendee-count">👥 ${ev.attendeeCount} registered</span>
                <div style="display:flex;align-items:center;gap:0.5rem;">
                    ${ev.membersOnly ? '<span class="members-only-tag">Members Only</span>' : ''}
                    ${action}
                </div>
            </div>
        </div>`;
}

async function registerEvent(eventId, e) {
    e.stopPropagation();
    try {
        await api.post(`/events/${eventId}/register`, {});
        showToast('Successfully registered!', 'success');
        loadOverview();
    } catch(err) {
        showToast(err.error || err.message || 'Could not register.', 'error');
    }
}

async function loadNewsFeed() {
    try {
        const clubs  = await api.get('/clubs');
        const myClbs = await api.get('/clubs/mine');
        const myIds  = new Set(myClbs.map(c => c.id));

        const feed = document.getElementById('news-feed');
        if (!clubs.length) {
            feed.innerHTML = `<div class="empty-state"><div class="empty-icon">📭</div><p>No news yet.</p></div>`;
            return;
        }
        feed.innerHTML = clubs.flatMap(club =>
            (club.eventCount > 0) ? [`
                <div class="news-item ${myIds.has(club.id) ? 'my-club' : ''}" onclick="openClubDetail(${club.id})" style="cursor:pointer;">
                    <div class="news-content">
                        <h4>${club.name}</h4>
                        <p>${club.eventCount} event(s) scheduled</p>
                    </div>
                    <span class="news-date">${club.department || ''}</span>
                </div>
            `] : []
        ).join('') || `<div class="empty-state"><div class="empty-icon">📭</div><p>Nothing yet.</p></div>`;
    } catch(e) { console.error(e); }
}

async function loadCoordinatorPending() {
    try {
        const requests = await api.get('/clubs/requests/pending');
        const badge    = document.getElementById('pending-badge');
        const block    = document.getElementById('coordinator-pending-block');

        if (requests.length) {
            badge.textContent = requests.length;
            badge.classList.remove('hidden');
            block.classList.remove('hidden');
            document.getElementById('pending-preview').innerHTML =
                requests.slice(0, 3).map(r => requestCardHTML(r, true)).join('');
        }
    } catch(e) { console.error(e); }
}

// ── All Clubs ──────────────────────────────────────────────────────────────
async function loadAllClubs() {
    const grid = document.getElementById('all-clubs-grid');
    grid.innerHTML = `<div class="loading-spinner">Loading clubs...</div>`;
    try {
        allClubs = await api.get('/clubs');
        renderClubCards(allClubs, 'all-clubs-grid');
    } catch(e) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Failed to load clubs.</p></div>`;
    }
}

function renderClubCards(clubs, containerId) {
    const grid = document.getElementById(containerId);
    if (!clubs.length) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-icon">🏛</div><p>No clubs found.</p></div>`;
        return;
    }
    grid.innerHTML = clubs.map(club => {
        const initials = club.name.split(' ').map(w => w[0]).join('').slice(0,3).toUpperCase();
        const logo     = club.logoImage
            ? `<img src="/uploads/${club.logoImage}" alt="${club.name}"/>`
            : initials;
        const statusBadge = club.joinRequestStatus === 'ACCEPTED' ? `<span class="member-badge member">✔ Member</span>`
            : club.joinRequestStatus === 'PENDING' ? `<span class="member-badge pending">⏳ Pending</span>`
            : club.joinRequestStatus === 'REJECTED' ? `<span class="member-badge rejected">✗ Rejected</span>`
            : '';
        return `
            <div class="club-card" onclick="openClubDetail(${club.id})">
                ${statusBadge}
                <div class="club-logo">${logo}</div>
                <div class="club-name">${club.name}</div>
                <div class="club-dept">${club.department === 'ALL' ? 'All Departments' : (club.department || 'General')}</div>
                <div class="club-desc">${club.description || 'No description yet.'}</div>
                <div class="club-meta">
                    <span>👥 ${club.memberCount} members</span>
                    <span>🎉 ${club.eventCount} events</span>
                </div>
            </div>`;
    }).join('');
}

function filterClubs() {
    const q    = document.getElementById('club-search').value.toLowerCase();
    const dept = document.getElementById('club-dept-filter').value;
    const filtered = allClubs.filter(c =>
        (!q    || c.name.toLowerCase().includes(q) || (c.description||'').toLowerCase().includes(q)) &&
        (!dept || c.department === dept)
    );
    renderClubCards(filtered, 'all-clubs-grid');
}

// ── My Clubs ───────────────────────────────────────────────────────────────
async function loadMyClubs() {
    const grid = document.getElementById('my-clubs-grid');
    grid.innerHTML = `<div class="loading-spinner">Loading...</div>`;
    try {
        const clubs    = await api.get('/clubs/mine');
        const requests = await api.get('/clubs/my-requests');
        renderClubCards(clubs, 'my-clubs-grid');
        renderMyRequests(requests);
    } catch(e) { console.error(e); }
}

function renderMyRequests(requests) {
    const el = document.getElementById('my-requests-list');
    if (!requests.length) {
        el.innerHTML = `<div class="empty-state"><div class="empty-icon">📭</div><p>No join requests sent yet.</p></div>`;
        return;
    }
    el.innerHTML = requests.map(r => `
        <div class="request-card">
            <div class="request-top">
                <span class="request-name">${r.clubName}</span>
                <span class="status-badge ${r.status}">${r.status}</span>
            </div>
            <div class="request-meta">Sent ${formatDate(r.requestedAt)}</div>
            ${r.message ? `<div class="request-message">"${r.message}"</div>` : ''}
            ${r.status === 'REJECTED' ? `<button class="btn btn-primary btn-sm" onclick="openJoinModal(${r.clubId})">Re-apply</button>` : ''}
        </div>
    `).join('');
}

// ── Club Detail ────────────────────────────────────────────────────────────
async function openClubDetail(clubId) {
    prevSection      = getCurrentSection();
    currentClubId    = clubId;
    showSection('section-club-detail');
    setActiveNav(prevSection);

    const container = document.getElementById('club-detail-content');
    container.innerHTML = `<div class="loading-spinner">Loading club...</div>`;

    try {
        const [club, events] = await Promise.all([
            api.get(`/clubs/${clubId}`),
            api.get(`/clubs/${clubId}/events`)
        ]);

        const tzOffset = (new Date()).getTimezoneOffset() * 60000;
        const today    = (new Date(Date.now() - tzOffset)).toISOString().split('T')[0];
        const upcoming = events.filter(e => e.date >= today);
        const past     = events.filter(e => e.date < today);

        const initials = club.name.split(' ').map(w => w[0]).join('').slice(0,3).toUpperCase();
        const logo     = club.logoImage
            ? `<img src="/uploads/${club.logoImage}" style="width:72px;height:72px;border-radius:14px;object-fit:cover;"/>`
            : `<div style="width:72px;height:72px;background:var(--nec-blue);color:var(--nec-gold);border-radius:14px;display:flex;align-items:center;justify-content:center;font-size:1.4rem;font-weight:700;">${initials}</div>`;

        const joinBtn = role === 'STUDENT'
            ? club.joinRequestStatus === 'ACCEPTED' ? `<span class="member-badge member" style="font-size:0.85rem;padding:6px 14px;">✔ Member</span>`
            : club.joinRequestStatus === 'PENDING'  ? `<span class="member-badge pending" style="font-size:0.85rem;padding:6px 14px;">⏳ Request Pending</span>`
            : `<button class="btn btn-primary" onclick="openJoinModal(${club.id})">+ Join Club</button>`
            : '';

        container.innerHTML = `
            <!-- Club Header -->
            <div style="background:var(--bg-card);border-radius:var(--radius);padding:1.75rem;box-shadow:var(--shadow-sm);margin-bottom:1.5rem;border-top:4px solid var(--nec-gold);">
                <div style="display:flex;align-items:flex-start;gap:1.25rem;flex-wrap:wrap;">
                    ${logo}
                    <div style="flex:1;min-width:200px;">
                        <div style="display:flex;align-items:center;justify-content:space-between;gap:1rem;flex-wrap:wrap;">
                            <h1 style="font-size:1.5rem;font-weight:700;color:var(--nec-blue);">${club.name}</h1>
                            ${joinBtn}
                        </div>
                        <div style="font-size:0.85rem;color:var(--nec-blue);font-weight:500;margin:4px 0;">${club.department === 'ALL' ? 'All Departments' : (club.department || '')} ${club.foundedYear ? '· Est. ' + club.foundedYear : ''}</div>
                        <div style="font-size:0.88rem;color:var(--text-secondary);margin-top:6px;">${club.description || ''}</div>
                        <div style="margin-top:0.75rem;font-size:0.82rem;color:var(--text-muted);">👤 Coordinator: <strong>${club.coordinatorName}</strong></div>
                        <div style="display:flex;gap:1rem;margin-top:0.5rem;font-size:0.82rem;color:var(--text-muted);">
                            <span>👥 ${club.memberCount} members</span>
                            <span>🎉 ${club.eventCount} events</span>
                        </div>
                    </div>
                </div>
                ${club.vision ? `<div style="margin-top:1rem;padding-top:1rem;border-top:1px solid var(--border);display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
                    <div><div style="font-size:0.75rem;font-weight:600;color:var(--nec-blue);text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px;">Vision</div><div style="font-size:0.85rem;color:var(--text-secondary);">${club.vision}</div></div>
                    <div><div style="font-size:0.75rem;font-weight:600;color:var(--nec-blue);text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px;">Mission</div><div style="font-size:0.85rem;color:var(--text-secondary);">${club.mission || '—'}</div></div>
                </div>` : ''}
            </div>

            <!-- Tabs -->
            <div class="tabs">
                <button class="tab-pill active" onclick="switchDetailTab(this,'detail-upcoming')">🎯 Upcoming Events (${upcoming.length})</button>
                <button class="tab-pill" onclick="switchDetailTab(this,'detail-past')">🗂 Past Events (${past.length})</button>
            </div>

            <div id="detail-upcoming" class="tab-content active">
                ${upcoming.length
                    ? `<div class="poster-grid">${upcoming.map(ev => posterCardHTML(ev, true)).join('')}</div>`
                    : `<div class="empty-state"><div class="empty-icon">📭</div><p>No upcoming events.</p></div>`}
            </div>

            <div id="detail-past" class="tab-content">
                <div class="filter-bar">
                    <input type="month" id="past-month-filter" onchange="filterPastEvents(${JSON.stringify(past).replace(/"/g, '&quot;')})" />
                </div>
                <div id="past-events-grid" class="poster-grid">
                    ${past.length
                        ? past.map(ev => posterCardHTML(ev, false)).join('')
                        : `<div class="empty-state"><div class="empty-icon">📭</div><p>No past events.</p></div>`}
                </div>
            </div>`;
    } catch(e) {
        container.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Failed to load club details.</p></div>`;
    }
}

function filterPastEvents(past) {
    const month = document.getElementById('past-month-filter').value; // e.g. "2025-04"
    const filtered = month ? past.filter(e => e.date && e.date.startsWith(month)) : past;
    document.getElementById('past-events-grid').innerHTML = filtered.length
        ? filtered.map(ev => posterCardHTML(ev, false)).join('')
        : `<div class="empty-state"><div class="empty-icon">📭</div><p>No events for this period.</p></div>`;
}

function switchDetailTab(btn, tabId) {
    btn.closest('.tabs').querySelectorAll('.tab-pill').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById(tabId).classList.add('active');
}

function goBackFromClub() { showSection(prevSection); }

// ── Event Detail ───────────────────────────────────────────────────────────
async function openEventDetail(eventId, clubId) {
    currentEventId = eventId;
    showSection('section-event-detail');

    const container = document.getElementById('event-detail-content');
    container.innerHTML = `<div class="loading-spinner">Loading event...</div>`;

    try {
        const events = await api.get(`/clubs/${clubId}/events`);
        const ev     = events.find(e => e.id === eventId);
        if (!ev) throw new Error('Event not found');

        const todayStr = new Date().toISOString().split('T')[0];
        const isPast   = ev.date && ev.date < todayStr;
        const isRegistered = ev.isRegistered;
        const isFull       = ev.isFull;
        const canRegister  = role === 'STUDENT' && !ev.isRegistered && !ev.isFull && !isPast;

        const posterHTML = ev.posterImage
            ? `<img src="/uploads/${ev.posterImage}" alt="${ev.name}" style="width:100%;display:block;"/>`
            : `<div style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:300px;padding:2rem;">
                   <div style="font-size:1.5rem;font-weight:700;color:white;text-align:center;">${ev.name}</div>
                   <div style="font-size:0.9rem;color:var(--nec-gold);margin-top:8px;">${ev.clubName}</div>
               </div>`;

        container.innerHTML = `
            <h1 style="font-size:1.5rem;font-weight:700;color:var(--nec-blue);margin-bottom:1.5rem;">${ev.name}</h1>
            <div class="event-detail-grid">
                <div class="event-poster-full">${posterHTML}</div>
                <div class="event-info-card">
                    <div class="info-row"><span class="info-label">📅 Date</span><span class="info-value">${ev.date || '—'}</span></div>
                    <div class="info-row"><span class="info-label">⏰ Time</span><span class="info-value">${ev.time || '—'}</span></div>
                    <div class="info-row"><span class="info-label">📍 Venue</span><span class="info-value">${ev.venue || '—'}</span></div>
                    <div class="info-row"><span class="info-label">🏛 Club</span><span class="info-value">${ev.clubName}</span></div>
                    <div class="info-row"><span class="info-label">👥 Registered</span><span class="info-value">${ev.attendeeCount} ${ev.maxAttendees ? `/ ${ev.maxAttendees}` : ''} people</span></div>
                    <div class="info-row"><span class="info-label">🔒 Access</span><span class="info-value">${ev.membersOnly ? 'Members Only' : 'Open to All'}</span></div>
                    ${ev.description ? `<div class="info-row"><span class="info-label">📝 About</span><span class="info-value">${ev.description}</span></div>` : ''}
                    <div style="margin-top:1.25rem;">
                        ${isRegistered
                            ? `<span style="color:var(--success);font-weight:600;">✔ You are registered</span>`
                            : isPast
                            ? `<span style="color:var(--text-muted);font-weight:600;">🕒 Event has ended</span>`
                            : isFull
                            ? `<span style="color:var(--danger);font-weight:600;">⚠️ Event is Full</span>`
                            : canRegister
                            ? `<button class="btn btn-primary" onclick="registerEvent(${ev.id}, event)">Register for this Event</button>`
                            : ''}
                    </div>
                    ${ev.winners ? `
                    <div style="margin-top:1.5rem;padding:1rem;background:rgba(212,175,55,0.1);border-left:4px solid var(--nec-gold);border-radius:4px;">
                        <h3 style="font-size:1.05rem;color:var(--nec-gold);margin-bottom:0.75rem;font-weight:700;">🏆 Event Winners</h3>
                        <ul style="margin:0;padding-left:1.25rem;color:var(--text-main);">
                            ${ev.winners.split(',').map(w => `<li style="margin-bottom:4px;">${w.trim()}</li>`).join('')}
                        </ul>
                    </div>
                    ` : ''}
                </div>
            </div>`;
    } catch(e) {
        container.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Failed to load event.</p></div>`;
    }
}

function goBackFromEvent() {
    if (currentClubId) {
        showSection('section-club-detail');
    } else {
        showSection('section-overview');
    }
}

// ── Join Modal ─────────────────────────────────────────────────────────────
function openJoinModal(clubId) {
    joinTargetClubId = clubId;
    document.getElementById('join-message').value = '';
    document.getElementById('modal-join').classList.remove('hidden');
}

async function submitJoinRequest() {
    const message = document.getElementById('join-message').value;
    if (!message.trim()) { showToast('Please write a message.', 'error'); return; }
    try {
        await api.post(`/clubs/${joinTargetClubId}/join-request`, { message });
        closeModal('modal-join');
        showToast('Join request sent!', 'success');
        openClubDetail(joinTargetClubId);
    } catch(err) {
        showToast(err.error || 'Failed to send request.', 'error');
    }
}

// ── Coordinator: My Club ───────────────────────────────────────────────────
async function loadMyClub() {
    const container = document.getElementById('my-club-content');
    container.innerHTML = `<div class="loading-spinner">Loading your club...</div>`;
    try {
        const club     = await api.get('/clubs/my-club');
        if (club.message) {
            container.innerHTML = `<div class="empty-state"><div class="empty-icon">🏛</div><p>${club.message}</p></div>`;
            return;
        }
        myClubData = club;
        const events   = await api.get(`/clubs/${club.id}/events`);
        const requests = await api.get('/clubs/requests/all');
        const members  = await api.get('/clubs/my-club/members');

        const tzOffset = (new Date()).getTimezoneOffset() * 60000;
        const today    = (new Date(Date.now() - tzOffset)).toISOString().split('T')[0];
        const upcoming = events.filter(e => e.date >= today);
        const past     = events.filter(e => e.date < today);
        const pending  = requests.filter(r => r.status === 'PENDING');

        // Update pending badge
        const badge = document.getElementById('pending-badge');
        if (badge) {
            badge.textContent = pending.length;
            badge.classList.toggle('hidden', pending.length === 0);
        }

        container.innerHTML = `
            <!-- Club Info Banner -->
            <div style="background:linear-gradient(135deg,var(--nec-blue),var(--nec-blue-light));border-radius:var(--radius);padding:1.5rem;margin-bottom:1.5rem;color:white;">
                <div style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:1rem;">
                    <div>
                        <h2 style="font-size:1.4rem;font-weight:700;">${club.name}</h2>
                        <div style="font-size:0.85rem;opacity:0.8;margin-top:4px;">${club.department || ''} · Est. ${club.foundedYear || '—'}</div>
                    </div>
                    <div style="display:flex;gap:0.75rem;">
                        <button class="btn btn-outline-white btn-sm" onclick="openEditClubModal(${club.id})">✏️ Edit Club Info</button>
                        <button class="btn btn-outline-white btn-sm" onclick="openNewsModal()">📢 Post News</button>
                        <button class="btn btn-gold btn-sm" onclick="openCreateEventModal()">+ Create Event</button>
                    </div>
                </div>
            </div>

            <!-- Tabs -->
            <div class="tabs" id="my-club-tabs">
                <button class="tab-pill active" id="tab-events" onclick="showClubTab('tab-events')">🎉 Events</button>
                <button class="tab-pill" id="tab-requests" onclick="showClubTab('tab-requests')">
                    📨 Requests ${pending.length ? `<span style="background:var(--danger);color:white;border-radius:10px;padding:1px 7px;font-size:0.7rem;margin-left:4px;">${pending.length}</span>` : ''}
                </button>
                <button class="tab-pill" id="tab-members" onclick="showClubTab('tab-members')">👥 Members</button>
                <button class="tab-pill" id="tab-news" onclick="showClubTab('tab-news')">📢 Announcements</button>
                <button class="tab-pill" id="tab-info" onclick="showClubTab('tab-info')">ℹ️ Info</button>
            </div>

            <!-- Events Tab -->
            <div id="tab-events-content" class="tab-content active">
                <div class="section-header" style="margin-bottom:1rem;">
                    <h2>Upcoming Events (${upcoming.length})</h2>
                </div>
                <div class="poster-grid">
                    ${upcoming.map(ev => coordinatorEventCard(ev, today)).join('') ||
                      `<div class="empty-state"><div class="empty-icon">📭</div><p>No upcoming events. Create one!</p></div>`}
                </div>
                <div class="section-header" style="margin-top:1.5rem;margin-bottom:1rem;">
                    <h2>Past Events (${past.length})</h2>
                </div>
                <div class="poster-grid">
                    ${past.map(ev => coordinatorEventCard(ev, today)).join('') ||
                      `<div class="empty-state"><div class="empty-icon">📭</div><p>No past events.</p></div>`}
                </div>
            </div>

            <!-- Requests Tab -->
            <div id="tab-requests-content" class="tab-content">
                <div class="section-header">
                    <h2>Pending Requests (${pending.length})</h2>
                </div>
                ${pending.map(r => requestCardHTML(r, false)).join('') ||
                  `<div class="empty-state"><div class="empty-icon">✅</div><p>No pending requests.</p></div>`}

                <div class="section-header" style="margin-top:1.5rem;">
                    <h2>All Requests</h2>
                </div>
                ${requests.map(r => `
                    <div class="request-card">
                        <div class="request-top">
                            <span class="request-name">${r.userName} <span style="font-size:0.78rem;color:var(--text-muted);">${r.userDepartment || ''} · ${r.userYear || ''}</span></span>
                            <span class="status-badge ${r.status}">${r.status}</span>
                        </div>
                        ${r.message ? `<div class="request-message">"${r.message}"</div>` : ''}
                    </div>`).join('') ||
                  `<div class="empty-state"><div class="empty-icon">📭</div><p>No requests yet.</p></div>`}
            </div>

            <!-- Members Tab -->
            <div id="tab-members-content" class="tab-content">
                <div class="section-header">
                    <h2>Members (${members.length})</h2>
                </div>
                <table class="data-table">
                    <thead><tr><th>Name</th><th>Email</th><th>Dept</th><th>Year</th><th>Action</th></tr></thead>
                    <tbody>
                        ${members.map(m => `
                            <tr>
                                <td>${m.name}</td>
                                <td>${m.email}</td>
                                <td>${m.department || '—'}</td>
                                <td>${m.year || '—'}</td>
                                <td>
                                    ${m.role === 'COORDINATOR'
                                        ? `<span class="role-badge COORDINATOR" style="font-size:0.7rem;">Coordinator</span>`
                                        : `<button class="btn btn-ghost btn-sm" onclick="promoteStudent(${m.id},'${m.name}')">Promote</button>`}
                                </td>
                            </tr>`).join('') ||
                            `<tr><td colspan="5" style="text-align:center;color:var(--text-muted);">No members yet.</td></tr>`}
                    </tbody>
                </table>
            </div>

            <!-- News Tab -->
            <div id="tab-news-content" class="tab-content">
                <div class="section-header">
                    <h2>Recent Announcements</h2>
                </div>
                <div class="news-feed" id="my-club-news">
                    <div class="loading-spinner">Loading news...</div>
                </div>
            </div>

            <!-- Info Tab -->
            <div id="tab-info-content" class="tab-content">
                <div style="background:var(--bg-card);border-radius:var(--radius);padding:1.5rem;box-shadow:var(--shadow-sm);">
                    <div class="info-row"><span class="info-label">Club Name</span><span class="info-value">${club.name}</span></div>
                    <div class="info-row"><span class="info-label">Department</span><span class="info-value">${club.department || '—'}</span></div>
                    <div class="info-row"><span class="info-label">Founded</span><span class="info-value">${club.foundedYear || '—'}</span></div>
                    <div class="info-row"><span class="info-label">Description</span><span class="info-value">${club.description || '—'}</span></div>
                    <div class="info-row"><span class="info-label">Vision</span><span class="info-value">${club.vision || '—'}</span></div>
                    <div class="info-row"><span class="info-label">Mission</span><span class="info-value">${club.mission || '—'}</span></div>
                </div>
            </div>`;

        loadMyClubNews(club.id);
    } catch(e) {
        container.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Failed to load club.</p></div>`;
    }
}

function coordinatorEventCard(ev, today) {
    const isOngoing = ev.date === today;
    return `
        <div class="poster-card">
            <div class="poster-image" onclick="openEventDetail(${ev.id}, ${ev.clubId})">
                ${ev.posterImage
                    ? `<img src="/uploads/${ev.posterImage}" alt="${ev.name}"/>`
                    : `<div class="poster-overlay"><div class="poster-event-name">${ev.name}</div><div class="poster-club-name">${ev.clubName}</div></div>`}
            </div>
            <div class="poster-body">
                <div class="poster-meta">
                    <span>📅 ${ev.date || '—'}</span>
                    <span>📍 ${ev.venue || '—'}</span>
                    <span>👥 ${ev.attendeeCount} ${ev.maxAttendees ? `/ ${ev.maxAttendees}` : ''}</span>
                </div>
            </div>
            <div class="poster-footer" style="flex-wrap:wrap;gap:0.5rem;padding:0.5rem 1rem 1rem;">
                ${isOngoing ? `<button class="btn btn-success btn-sm" onclick="openAttendance(${ev.id})">Attendance</button>` : ''}
                <button class="btn btn-primary btn-sm" onclick="viewAttendees(${ev.id})">👥 View</button>
                <button class="btn btn-info btn-sm" onclick="downloadAttendance(${ev.id})" style="background:var(--nec-gold);color:#000;">📥 Excel</button>
                <button class="btn btn-ghost btn-sm" onclick="openEditEventModal(${ev.id})">✏️ Edit</button>
                <button class="btn btn-danger btn-sm" onclick="deleteEvent(${ev.id})">🗑</button>
            </div>
        </div>`;
}

function showClubTab(tabId) {
    document.querySelectorAll('#my-club-tabs .tab-pill').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('#my-club-content .tab-content').forEach(t => t.classList.remove('active'));
    document.getElementById(tabId).classList.add('active');
    document.getElementById(tabId + '-content').classList.add('active');
}

function requestCardHTML(r, mini) {
    return `
        <div class="request-card">
            <div class="request-top">
                <span class="request-name">${r.userName}
                    <span style="font-size:0.78rem;color:var(--text-muted);margin-left:6px;">${r.userDepartment || ''} · ${r.userYear || ''}</span>
                </span>
                <span class="status-badge ${r.status}">${r.status}</span>
            </div>
            ${r.message ? `<div class="request-message">"${r.message}"</div>` : ''}
            <div class="request-actions">
                <button class="btn btn-success btn-sm" onclick="respondRequest(${r.id},'accept')">✔ Accept</button>
                <button class="btn btn-danger btn-sm" onclick="respondRequest(${r.id},'reject')">✗ Reject</button>
            </div>
        </div>`;
}

async function respondRequest(requestId, action) {
    try {
        await api.put(`/clubs/requests/${requestId}/${action}`, {});
        showToast(action === 'accept' ? 'Member accepted!' : 'Request rejected.', action === 'accept' ? 'success' : 'warning');
        loadMyClub();
    } catch(err) {
        showToast(err.error || 'Failed.', 'error');
    }
}

async function promoteStudent(userId, uname) {
    if (!confirm(`Promote ${uname} to Coordinator? They will manage this club.`)) return;
    try {
        await api.put(`/clubs/my-club/members/${userId}/promote`, {});
        showToast(`${uname} promoted to Coordinator!`, 'success');
        loadMyClub();
    } catch(err) {
        showToast(err.error || 'Failed.', 'error');
    }
}

// ── Event Management ───────────────────────────────────────────────────────
function openCreateEventModal() {
    currentEventId = null;
    document.getElementById('event-modal-title').textContent = '➕ Create New Event';
    document.getElementById('event-submit-btn').textContent = 'Create Event';
    document.getElementById('ev-name').value = '';
    document.getElementById('ev-desc').value = '';
    document.getElementById('ev-venue').value = '';
    document.getElementById('ev-date').value = '';
    document.getElementById('ev-time').value = '';
    document.getElementById('ev-limit').value = '';
    document.getElementById('ev-members-only').checked = false;
    document.getElementById('ev-winners').value = '';
    document.getElementById('ev-winners-group').style.display = 'none';
    document.getElementById('ev-poster').value = '';
    document.getElementById('ev-poster-label').textContent = 'Event Poster (image)';
    document.getElementById('modal-event').classList.remove('hidden');
}

async function openEditEventModal(eventId) {
    try {
        const ev = await api.get(`/events/${eventId}`);
        currentEventId = eventId;
        document.getElementById('event-modal-title').textContent = '✏️ Edit Event';
        document.getElementById('event-submit-btn').textContent = 'Save Changes';
        document.getElementById('ev-name').value = ev.name;
        document.getElementById('ev-desc').value = ev.description || '';
        document.getElementById('ev-venue').value = ev.venue || '';
        document.getElementById('ev-date').value = ev.date || '';
        document.getElementById('ev-time').value = ev.time || '';
        document.getElementById('ev-limit').value = ev.maxAttendees || '';
        document.getElementById('ev-members-only').checked = ev.membersOnly;
        
        const tzOffset = (new Date()).getTimezoneOffset() * 60000;
        const today    = (new Date(Date.now() - tzOffset)).toISOString().split('T')[0];
        if (ev.date < today) {
            document.getElementById('ev-winners-group').style.display = 'block';
            document.getElementById('ev-winners').value = ev.winners || '';
        } else {
            document.getElementById('ev-winners-group').style.display = 'none';
            document.getElementById('ev-winners').value = '';
        }
        
        document.getElementById('ev-poster').value = '';
        document.getElementById('ev-poster-label').textContent = 'Update Poster (optional)';
        document.getElementById('modal-event').classList.remove('hidden');
    } catch(e) {
        showToast('Failed to load event data.', 'error');
    }
}

async function submitEvent() {
    if (!myClubData) { showToast('No club found.', 'error'); return; }
    const name = document.getElementById('ev-name').value.trim();
    if (!name) { showToast('Event name is required.', 'error'); return; }

    const body = {
        name,
        description:  document.getElementById('ev-desc').value,
        venue:        document.getElementById('ev-venue').value,
        date:         document.getElementById('ev-date').value,
        time:         document.getElementById('ev-time').value,
        maxAttendees: parseInt(document.getElementById('ev-limit').value) || null,
        membersOnly:  document.getElementById('ev-members-only').checked,
        winners:      document.getElementById('ev-winners').value || null
    };

    try {
        let ev;
        if (currentEventId) {
            ev = await api.put(`/events/${currentEventId}`, body);
        } else {
            ev = await api.post(`/clubs/${myClubData.id}/events`, body);
        }

        // Upload poster if provided
        const posterFile = document.getElementById('ev-poster').files[0];
        if (posterFile) {
            const fd = new FormData();
            fd.append('file', posterFile);
            await api.uploadFile(`/events/${ev.id}/poster`, fd);
        }

        closeModal('modal-event');
        showToast(currentEventId ? 'Event updated!' : 'Event created!', 'success');
        loadMyClub();
    } catch(err) {
        showToast(err.error || 'Operation failed.', 'error');
    }
}

async function viewAttendees(eventId) {
    document.getElementById('modal-attendees').classList.remove('hidden');
    const container = document.getElementById('attendees-list-content');
    container.innerHTML = `<div class="loading-spinner">Loading members...</div>`;
    try {
        const list = await api.get(`/events/${eventId}/attendees`);
        if (!list.length) {
            container.innerHTML = `<div class="empty-state"><div class="empty-icon">👥</div><p>No students registered yet.</p></div>`;
            return;
        }
        container.innerHTML = `
            <table class="data-table">
                <thead><tr><th>Name</th><th>Email</th><th>Dept</th><th>Year</th></tr></thead>
                <tbody>
                    ${list.map(u => `
                        <tr>
                            <td>${u.name}</td>
                            <td>${u.email}</td>
                            <td>${u.department || '—'}</td>
                            <td>${u.year || '—'}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch(e) {
        showToast('Failed to load attendees.', 'error');
    }
}

async function downloadAttendance(eventId) {
    try {
        await api.downloadFile(`/events/${eventId}/attendance/export`, `attendance_event_${eventId}.xlsx`);
        showToast('Attendance sheet downloaded!', 'success');
    } catch(e) {
        showToast('Failed to download attendance.', 'error');
    }
}

async function deleteEvent(eventId) {
    if (!confirm('Delete this event?')) return;
    try {
        await api.delete(`/events/${eventId}`);
        showToast('Event deleted.', 'success');
        loadMyClub();
    } catch(err) {
        showToast(err.error || 'Failed.', 'error');
    }
}

function uploadPoster(eventId) {
    const input = document.createElement('input');
    input.type = 'file'; input.accept = 'image/*';
    input.onchange = async () => {
        const fd = new FormData();
        fd.append('file', input.files[0]);
        try {
            await api.uploadFile(`/events/${eventId}/poster`, fd);
            showToast('Poster uploaded!', 'success');
            loadMyClub();
        } catch(err) {
            showToast(err.error || 'Upload failed.', 'error');
        }
    };
    input.click();
}

// ── Attendance ─────────────────────────────────────────────────────────────
async function openAttendance(eventId) {
    activeEventId = eventId;
    document.getElementById('modal-attendance').classList.remove('hidden');
    document.getElementById('attendance-list').innerHTML = `<div class="loading-spinner">Loading...</div>`;
    try {
        const events  = await api.get(`/clubs/${myClubData.id}/events`);
        const ev      = events.find(e => e.id === eventId);
        if (!ev) return;
        const members = await api.get('/clubs/my-club/members');

        document.getElementById('attendance-list').innerHTML = `
            <p style="font-size:0.85rem;color:var(--text-muted);margin-bottom:1rem;">Event: <strong>${ev.name}</strong></p>
            <table class="data-table">
                <thead><tr><th>Name</th><th>Dept</th><th>Present</th></tr></thead>
                <tbody>
                    ${members.map(m => `
                        <tr>
                            <td>${m.name}</td>
                            <td>${m.department || '—'}</td>
                            <td>
                                <input type="checkbox" id="att-${m.id}"
                                    onchange="markAttendance(${eventId},${m.id},this.checked)"/>
                            </td>
                        </tr>`).join('')}
                </tbody>
            </table>`;
    } catch(e) {
        document.getElementById('attendance-list').innerHTML = `<p>Failed to load.</p>`;
    }
}

async function markAttendance(eventId, userId, present) {
    try {
        await api.post(`/events/${eventId}/attendance`, {
            userId,
            status: present ? 'PRESENT' : 'ABSENT'
        });
    } catch(err) {
        showToast(err.error || 'Failed.', 'error');
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────
function getCurrentSection() {
    return document.querySelector('.section.active')?.id || 'section-overview';
}

function closeModal(id) {
    document.getElementById(id).classList.add('hidden');
}

function logout() {
    localStorage.clear();
    window.location.href = '/index.html';
}

function showToast(msg, type = 'success') {
    api.showToast(msg, type);
}

// ── Edit Club Logic ──
async function openEditClubModal(clubId) {
    try {
        const club = await api.get(`/clubs/${clubId}`);
        currentClubId = clubId;
        
        document.getElementById('ec-name').value = club.name;
        document.getElementById('ec-dept').value = club.department || '';
        document.getElementById('ec-year').value = club.foundedYear || '';
        document.getElementById('ec-desc').value = club.description || '';
        document.getElementById('ec-vision').value = club.vision || '';
        document.getElementById('ec-mission').value = club.mission || '';
        
        document.getElementById('modal-edit-club').classList.remove('hidden');
    } catch (e) {
        showToast('Failed to load club data', 'error');
    }
}

async function submitEditClub() {
    const body = {
        name: document.getElementById('ec-name').value,
        department: document.getElementById('ec-dept').value,
        foundedYear: parseInt(document.getElementById('ec-year').value) || null,
        description: document.getElementById('ec-desc').value,
        vision: document.getElementById('ec-vision').value,
        mission: document.getElementById('ec-mission').value
    };
    
    try {
        await api.put(`/clubs/${currentClubId}`, body);
        const logoFile = document.getElementById('ec-logo-file').files[0];
        if (logoFile) {
            const fd = new FormData();
            fd.append('file', logoFile);
            await api.uploadFile(`/clubs/${currentClubId}/logo`, fd);
        }
        showToast('Club information updated!', 'success');
        closeModal('modal-edit-club');
        loadMyClub();
    } catch (err) {
        showToast(err.error || 'Update failed', 'error');
    }
}

function formatDate(dt) {
    if (!dt) return '—';
    return new Date(dt).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' });
}

// ── Profile Logic ──
async function openProfileModal() {
    const user = api.getUser();
    if (!user) return;
    
    document.getElementById('profile-modal-name').textContent = user.name;
    document.getElementById('profile-modal-email').textContent = user.email;
    document.getElementById('profile-avatar-modal').textContent = user.name.charAt(0).toUpperCase();
    
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
    document.getElementById('modal-profile').classList.remove('hidden');
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
// ── News / Announcements ───────────────────────────────────────────────────
async function loadNewsFeed() {
    const feed = document.getElementById('news-feed');
    if (!feed) return;
    try {
        const news = await api.get('/news');
        if (!news.length) {
            feed.innerHTML = `<div style="padding:1rem;color:var(--text-muted);font-size:0.85rem;">No recent announcements.</div>`;
            return;
        }
        feed.innerHTML = news.map(n => `
            <div class="news-item ${n.clubDept === 'ALL' ? 'my-club' : ''}">
                <div class="news-content">
                    <h4>${n.title}</h4>
                    <p>${n.content}</p>
                    <div style="font-size:0.7rem;color:var(--nec-blue);font-weight:600;margin-top:4px;">${n.clubName}</div>
                </div>
                <div class="news-date">${new Date(n.postedAt).toLocaleDateString()}</div>
            </div>
        `).join('');
    } catch(e) { console.error(e); }
}

async function loadMyClubNews(clubId) {
    const container = document.getElementById('my-club-news');
    if (!container) return;
    try {
        const news = await api.get(`/news/club/${clubId}`);
        if (!news.length) {
            container.innerHTML = `<div class="empty-state"><div class="empty-icon">📭</div><p>No announcements yet.</p></div>`;
            return;
        }
        container.innerHTML = news.map(n => `
            <div class="news-item">
                <div class="news-content">
                    <h4 style="color:var(--nec-blue);">${n.title}</h4>
                    <p style="font-size:0.88rem;color:var(--text-secondary);">${n.content}</p>
                </div>
                <div style="display:flex;flex-direction:column;align-items:flex-end;gap:0.5rem;">
                    <div class="news-date">${new Date(n.postedAt).toLocaleDateString()}</div>
                    <button class="btn btn-ghost btn-sm" style="color:var(--danger);border-color:var(--danger);" onclick="deleteNews(${n.id})">Delete</button>
                </div>
            </div>
        `).join('');
    } catch(e) { console.error(e); }
}

function openNewsModal() {
    document.getElementById('news-title').value = '';
    document.getElementById('news-content').value = '';
    document.getElementById('modal-news').classList.remove('hidden');
}

async function submitNews() {
    if (!myClubData) return;
    const title = document.getElementById('news-title').value.trim();
    const content = document.getElementById('news-content').value.trim();
    if (!title || !content) { showToast('Title and content are required.', 'error'); return; }

    try {
        await api.post('/news', { clubId: myClubData.id, title, content });
        closeModal('modal-news');
        showToast('Announcement posted!', 'success');
        loadMyClub(); // Refresh
    } catch(e) { showToast(e.error || 'Failed to post news.', 'error'); }
}

async function deleteNews(newsId) {
    if (!confirm('Are you sure you want to delete this announcement?')) return;
    try {
        await api.delete(`/news/${newsId}`);
        showToast('Announcement deleted.');
        loadMyClub();
    } catch(e) { showToast('Failed to delete.'); }
}
