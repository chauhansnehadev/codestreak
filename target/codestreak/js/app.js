/**
 * CodeStreak Global App JS
 * Handles common API requests, authentication check, sidebar rendering, and modals.
 */

const App = {
  currentUser: null,

  async init(activePage) {
    this.renderSidebar(activePage);
    await this.checkAuth();
  },

  async checkAuth() {
    try {
      const res = await fetch('/codestreak/api/auth/me');
      if (res.ok) {
        const json = await res.json();
        if (json.success && json.data) {
          this.currentUser = json.data;
          this.updateUserUI(json.data);
          return;
        }
      }
      // If no session active, default to demouser display without redirecting
      this.currentUser = { username: 'demouser', fullName: 'Demo Coder' };
      this.updateUserUI(this.currentUser);
    } catch (e) {
      this.currentUser = { username: 'demouser', fullName: 'Demo Coder' };
      this.updateUserUI(this.currentUser);
    }
  },

  updateUserUI(user) {
    const nameEl = document.getElementById('sidebar-user-name');
    const subEl = document.getElementById('sidebar-user-sub');
    const avatarEl = document.getElementById('sidebar-avatar');

    if (nameEl) nameEl.textContent = user.fullName || user.username;
    if (subEl) subEl.textContent = `@${user.username}`;
    if (avatarEl) {
      avatarEl.textContent = (user.fullName || user.username).charAt(0).toUpperCase();
    }
  },

  async logout() {
    try {
      await fetch('/codestreak/api/auth/logout', { method: 'POST' });
    } finally {
      window.location.href = '/codestreak/login.html';
    }
  },

  renderSidebar(activePage) {
    const sidebar = document.getElementById('app-sidebar');
    if (!sidebar) return;

    const navItems = [
      { id: 'dashboard', label: 'Dashboard', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>', href: 'dashboard.html' },
      { id: 'problems', label: 'Problems', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>', href: 'problems.html' },
      { id: 'platforms', label: 'Platforms', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>', href: 'platforms.html' },
      { id: 'roadmap', label: 'DSA Roadmap', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/><line x1="8" y1="2" x2="8" y2="18"/><line x1="16" y1="6" x2="16" y2="22"/></svg>', href: 'roadmap.html' },
      { id: 'recommendations', label: 'Recommendations', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>', href: 'recommendations.html' },
      { id: 'revisions', label: 'Revision Planner', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>', href: 'revisions.html' },
      { id: 'analytics', label: 'Analytics', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>', href: 'analytics.html' },
      { id: 'profile', label: 'Profile & Settings', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>', href: 'profile.html' }
    ];

    let html = `
      <div class="brand-header">
        <div class="brand-icon">⚡</div>
        <div class="brand-name">CodeStreak</div>
        <span class="brand-tag">DSA</span>
      </div>
      <ul class="nav-links">
    `;

    navItems.forEach(item => {
      const activeClass = (item.id === activePage) ? 'active' : '';
      html += `
        <li>
          <a href="/codestreak/${item.href}" class="nav-link ${activeClass}">
            ${item.icon}
            <span>${item.label}</span>
          </a>
        </li>
      `;
    });

    html += `
      </ul>
      <div class="sidebar-footer">
        <div class="user-mini">
          <div class="user-avatar" id="sidebar-avatar">U</div>
          <div class="user-meta">
            <span class="user-name" id="sidebar-user-name">Loading...</span>
            <span class="user-sub" id="sidebar-user-sub">...</span>
          </div>
        </div>
        <button class="logout-btn" onclick="App.logout()" title="Logout">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
      </div>
    `;

    sidebar.innerHTML = html;
  },

  toast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      setTimeout(() => toast.remove(), 250);
    }, 3000);
  }
};
