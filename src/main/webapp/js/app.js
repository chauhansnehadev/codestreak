/**
 * CodeStreak Global App JS
 * Handles common API requests, navigation, sidebar rendering, and modals.
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
      this.currentUser = { username: 'sneha', fullName: 'Sneha Chauhan' };
      this.updateUserUI(this.currentUser);
    } catch (e) {
      this.currentUser = { username: 'sneha', fullName: 'Sneha Chauhan' };
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

    // Sidebar navigation items:
    // 1. Dashboard
    // 2. Problems
    // 3. Platforms
    // 4. Recommendations
    const navItems = [
      {
        id: 'dashboard',
        label: 'Dashboard',
        icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>',
        href: 'dashboard.html'
      },
      {
        id: 'problems',
        label: 'Problems',
        icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>',
        href: 'problems.html'
      },
      {
        id: 'platforms',
        label: 'Platforms',
        icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>',
        href: 'platforms.html'
      },
      {
        id: 'recommendations',
        label: 'Recommendations',
        icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>',
        href: 'recommendations.html'
      }
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
          <div class="user-avatar" id="sidebar-avatar">S</div>
          <div class="user-meta">
            <span class="user-name" id="sidebar-user-name">Sneha Chauhan</span>
            <span class="user-sub" id="sidebar-user-sub">@sneha</span>
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
