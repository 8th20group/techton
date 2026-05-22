/**
 * ==================== CREW POINT SHOP - CORE ENGINE ====================
 * Single Page Application Manager (Vanilla JS)
 */

// Global State
const state = {
    currentCrew: null, // { crewId, githubId, nickname, track, generation, point }
    activeView: 'dashboard',
    adminMode: false,
    weeklyActivities: null,
    pointsSummary: null,
    shopItems: [],
    myTickets: [],
    pointHistories: [],
    crewRankings: [],
    coachRankings: [],
    pendingAdminActivities: []
};

// Web Audio API Synthesizer (Retro 8-bit sound effects)
const soundSynth = {
    ctx: null,
    
    init() {
        if (!this.ctx) {
            this.ctx = new (window.AudioContext || window.webkitAudioContext)();
        }
    },
    
    playCoin() {
        this.init();
        if (!this.ctx) return;
        
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        
        osc.type = 'sine';
        osc.frequency.setValueAtTime(987.77, now); // B5
        osc.frequency.setValueAtTime(1318.51, now + 0.08); // E6
        
        gain.gain.setValueAtTime(0.1, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.35);
        
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        
        osc.start(now);
        osc.stop(now + 0.35);
    },
    
    playSuccess() {
        this.init();
        if (!this.ctx) return;
        
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(523.25, now); // C5
        osc.frequency.setValueAtTime(659.25, now + 0.1); // E5
        osc.frequency.setValueAtTime(783.99, now + 0.2); // G5
        osc.frequency.setValueAtTime(1046.50, now + 0.3); // C6
        
        gain.gain.setValueAtTime(0.15, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.5);
        
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        
        osc.start(now);
        osc.stop(now + 0.5);
    },
    
    playDefeat() {
        this.init();
        if (!this.ctx) return;
        
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(150, now);
        osc.frequency.linearRampToValueAtTime(80, now + 0.4);
        
        gain.gain.setValueAtTime(0.1, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.4);
        
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        
        osc.start(now);
        osc.stop(now + 0.4);
    },
    
    playPurchase() {
        this.init();
        if (!this.ctx) return;
        
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(880, now);
        osc.frequency.exponentialRampToValueAtTime(440, now + 0.15);
        
        gain.gain.setValueAtTime(0.1, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.25);
        
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        
        osc.start(now);
        osc.stop(now + 0.25);
    }
};

// Toast Notifications Helper
const showToast = (message, type = 'info') => {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'warning') icon = '⚠️';
    if (type === 'danger') icon = '🚨';
    
    toast.innerHTML = `<span class="toast-icon">${icon}</span><span class="toast-msg">${message}</span>`;
    container.appendChild(toast);
    
    // trigger animation
    setTimeout(() => toast.classList.add('show'), 50);
    
    // remove toast
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
};

// Custom Dialog alert/confirmation modal
const customAlert = (title, message, showCancel = false, onConfirm = null) => {
    const modal = document.getElementById('alert-modal');
    const titleEl = document.getElementById('alert-title');
    const messageEl = document.getElementById('alert-message');
    const btnOk = document.getElementById('btn-alert-ok');
    const btnCancel = document.getElementById('btn-alert-cancel');
    
    titleEl.textContent = title;
    messageEl.textContent = message;
    
    btnCancel.style.display = showCancel ? 'inline-flex' : 'none';
    modal.style.display = 'flex';
    
    // Cleanup old events
    const cleanOk = btnOk.cloneNode(true);
    btnOk.parentNode.replaceChild(cleanOk, btnOk);
    
    const cleanCancel = btnCancel.cloneNode(true);
    btnCancel.parentNode.replaceChild(cleanCancel, btnCancel);
    
    cleanOk.addEventListener('click', () => {
        modal.style.display = 'none';
        if (onConfirm) onConfirm();
    });
    
    cleanCancel.addEventListener('click', () => {
        modal.style.display = 'none';
    });
};

// API Services
const API = {
    baseUrl: '', // Local context

    async handleResponse(res) {
        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            const errMsg = errData.message || '서버 오류가 발생했습니다.';
            throw new Error(errMsg);
        }
        return res.json().catch(() => ({}));
    },

    async register(githubId, nickname, track) {
        return fetch(`${this.baseUrl}/crews`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ githubId, nickname, track })
        }).then(this.handleResponse);
    },

    async login(githubId) {
        return fetch(`${this.baseUrl}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ githubId })
        }).then(this.handleResponse);
    },

    async getMe(githubId) {
        return fetch(`${this.baseUrl}/auth/me?githubId=${githubId}`)
            .then(this.handleResponse);
    },

    async getPointsSummary(crewId) {
        return fetch(`${this.baseUrl}/crews/${crewId}/points/summary`)
            .then(this.handleResponse);
    },

    async getWeeklyActivities(crewId) {
        return fetch(`${this.baseUrl}/crews/${crewId}/weekly-activities`)
            .then(this.handleResponse);
    },

    async getCrewActivities(crewId) {
        return fetch(`${this.baseUrl}/crews/${crewId}/activities`)
            .then(this.handleResponse);
    },

    async getCrewRankings() {
        return fetch(`${this.baseUrl}/rankings/crews`)
            .then(this.handleResponse);
    },

    async getCoachRankings() {
        return fetch(`${this.baseUrl}/rankings/coaches`)
            .then(this.handleResponse);
    },

    async syncGitHub(crewId, date = '') {
        const query = date ? `?activityDate=${date}` : '';
        return fetch(`${this.baseUrl}/crews/${crewId}/activities/github/sync${query}`, {
            method: 'POST'
        }).then(this.handleResponse);
    },

    async submitCommit(crewId, activityDate, githubUrl) {
        return fetch(`${this.baseUrl}/crews/${crewId}/activities/commit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ activityDate, githubUrl })
        }).then(this.handleResponse);
    },

    async submitReview(crewId, activityDate, reviewUrl) {
        return fetch(`${this.baseUrl}/crews/${crewId}/activities/review`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ activityDate, reviewUrl })
        }).then(this.handleResponse);
    },

    async submitMission(crewId, activityDate, imageFile, memo) {
        const formData = new FormData();
        formData.append('activityDate', activityDate);
        formData.append('image', imageFile);
        if (memo) formData.append('memo', memo);
        
        return fetch(`${this.baseUrl}/crews/${crewId}/activities/mission`, {
            method: 'POST',
            body: formData
        }).then(this.handleResponse);
    },

    async submitBlog(crewId, activityDate, blogUrl, memo) {
        return fetch(`${this.baseUrl}/crews/${crewId}/activities/blog`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ activityDate, blogUrl, memo })
        }).then(this.handleResponse);
    },

    // Shop
    async getShopItems() {
        return fetch(`${this.baseUrl}/shop/items`)
            .then(this.handleResponse);
    },

    async buyRandomBox(crewId) {
        return fetch(`${this.baseUrl}/crews/${crewId}/shop/random-box`, {
            method: 'POST'
        }).then(this.handleResponse);
    },

    async buyCoachTicket(crewId, ticketType) {
        return fetch(`${this.baseUrl}/crews/${crewId}/shop/coach-tickets`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ticketType })
        }).then(this.handleResponse);
    },

    async getMyTickets(crewId) {
        return fetch(`${this.baseUrl}/crews/${crewId}/tickets`)
            .then(this.handleResponse);
    },

    async useTicket(crewId, ticketId) {
        return fetch(`${this.baseUrl}/crews/${crewId}/tickets/${ticketId}/use`, {
            method: 'PATCH'
        }).then(this.handleResponse);
    },

    async getPointHistories(crewId) {
        return fetch(`${this.baseUrl}/crews/${crewId}/point-histories`)
            .then(this.handleResponse);
    },

    // Admin
    async getPendingAdminActivities() {
        return fetch(`${this.baseUrl}/admin/activities/pending`)
            .then(this.handleResponse);
    },

    async adminApproveActivity(activityId) {
        return fetch(`${this.baseUrl}/admin/activities/${activityId}/approve`, {
            method: 'PATCH'
        }).then(this.handleResponse);
    },

    async adminRejectActivity(activityId, reason) {
        return fetch(`${this.baseUrl}/admin/activities/${activityId}/reject`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reason })
        }).then(this.handleResponse);
    },

    async adminGitHubSync(date = '') {
        const query = date ? `?activityDate=${date}` : '';
        return fetch(`${this.baseUrl}/admin/activities/github/sync${query}`, {
            method: 'POST'
        }).then(this.handleResponse);
    }
};

// Core App Controller
const App = {
    init() {
        this.loader = document.getElementById('app-loader');
        this.loginView = document.getElementById('login-view');
        this.registerView = document.getElementById('register-view');
        this.mainShell = document.getElementById('main-shell');
        
        this.bindEvents();
        this.checkAuth();
    },

    showLoader(show = true) {
        this.loader.style.display = show ? 'flex' : 'none';
    },

    bindEvents() {
        // Auth Redirections
        document.getElementById('go-to-register').addEventListener('click', (e) => {
            e.preventDefault();
            this.loginView.style.display = 'none';
            this.registerView.style.display = 'block';
        });

        document.getElementById('go-to-login').addEventListener('click', (e) => {
            e.preventDefault();
            this.registerView.style.display = 'none';
            this.loginView.style.display = 'block';
        });

        // Register Action
        document.getElementById('register-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const githubId = document.getElementById('reg-github-id').value.trim();
            const nickname = document.getElementById('reg-nickname').value.trim();
            const track = document.getElementById('reg-track').value;
            
            this.showLoader(true);
            try {
                const crew = await API.register(githubId, nickname, track);
                showToast('회원가입이 완료되었습니다! 로그인합니다.', 'success');
                this.saveSession(crew.githubId, crew.id, crew.nickname);
                await this.refreshUserContext();
                this.registerView.style.display = 'none';
                this.mainShell.style.display = 'flex';
                this.switchSubview('dashboard');
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });

        // Login Action
        document.getElementById('login-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const githubId = document.getElementById('login-github-id').value.trim();
            
            this.showLoader(true);
            try {
                const session = await API.login(githubId);
                showToast(`${session.nickname} 크루님, 반갑습니다!`, 'success');
                this.saveSession(session.githubId, session.crewId, session.nickname);
                await this.refreshUserContext();
                this.loginView.style.display = 'none';
                this.mainShell.style.display = 'flex';
                this.switchSubview('dashboard');
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });

        // Logout Action
        document.getElementById('btn-logout').addEventListener('click', () => {
            customAlert('로그아웃', '정말 로그아웃 하시겠습니까?', true, () => {
                this.clearSession();
                state.currentCrew = null;
                this.mainShell.style.display = 'none';
                this.loginView.style.display = 'block';
                showToast('성공적으로 로그아웃되었습니다.', 'info');
            });
        });

        // Subview switches via Nav links
        document.querySelectorAll('.sidebar-nav .nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const target = link.getAttribute('data-target');
                this.switchSubview(target);
            });
        });

        // Toggle Admin Mode
        const btnAdminMode = document.getElementById('toggle-admin-mode');
        btnAdminMode.addEventListener('click', () => {
            state.adminMode = !state.adminMode;
            const adminNavItem = document.getElementById('admin-nav-item');
            if (state.adminMode) {
                adminNavItem.style.display = 'block';
                btnAdminMode.textContent = '🛡️ 관리자 모드 비활성';
                btnAdminMode.classList.remove('btn-outline');
                btnAdminMode.classList.add('btn-primary');
                showToast('관리자 시연 모드가 활성화되었습니다. 인증 승인을 진행할 수 있습니다.', 'info');
            } else {
                adminNavItem.style.display = 'none';
                btnAdminMode.textContent = '🛡️ 관리자 모드 활성';
                btnAdminMode.classList.add('btn-outline');
                btnAdminMode.classList.remove('btn-primary');
                if (state.activeView === 'admin') {
                    this.switchSubview('dashboard');
                }
            }
        });

        // Global dashboard sync button
        document.getElementById('btn-dashboard-sync').addEventListener('click', () => this.triggerGitHubSync());

        // Ranking tab buttons
        document.querySelectorAll('.rank-tab-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.rank-tab-btn').forEach(b => b.classList.remove('active'));
                document.querySelectorAll('.rank-tab-content').forEach(c => c.classList.remove('active'));
                
                btn.classList.add('active');
                const tabId = btn.getAttribute('data-tab');
                document.getElementById(tabId).classList.add('active');
            });
        });

        // Checklist Forms tab switcher
        document.querySelectorAll('.submission-tabs .sub-tab-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.submission-tabs .sub-tab-btn').forEach(b => b.classList.remove('active'));
                document.querySelectorAll('.sub-form-content').forEach(c => c.classList.remove('active'));
                
                btn.classList.add('active');
                const formId = btn.getAttribute('data-form');
                document.getElementById(formId).classList.add('active');
            });
        });

        // Image Preview handler for mission submissions
        const missionImgInput = document.getElementById('mission-image');
        missionImgInput.addEventListener('change', (e) => {
            const previewBox = document.getElementById('mission-image-preview');
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    previewBox.innerHTML = `<img src="${e.target.result}" alt="Preview">`;
                };
                reader.readAsDataURL(file);
            } else {
                previewBox.innerHTML = `<span class="preview-placeholder">사진을 선택하면 미리보기가 나타납니다.</span>`;
            }
        });

        // Submit Mission form
        document.getElementById('form-mission-submit').addEventListener('submit', async (e) => {
            e.preventDefault();
            const date = document.getElementById('mission-date').value;
            const fileInput = document.getElementById('mission-image');
            const memo = document.getElementById('mission-memo').value.trim();
            
            if (!fileInput.files.length) {
                showToast('인증 사진을 업로드해 주세요.', 'warning');
                return;
            }
            
            this.showLoader(true);
            try {
                const response = await API.submitMission(state.currentCrew.crewId, date, fileInput.files[0], memo);
                showToast(response.message, 'success');
                soundSynth.playSuccess();
                // Reset form
                document.getElementById('form-mission-submit').reset();
                document.getElementById('mission-image-preview').innerHTML = `<span class="preview-placeholder">사진을 선택하면 미리보기가 나타납니다.</span>`;
                await this.refreshUserContext();
                if (state.activeView === 'checklist') this.renderChecklist();
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });

        // Submit Blog form
        document.getElementById('form-blog-submit').addEventListener('submit', async (e) => {
            e.preventDefault();
            const date = document.getElementById('blog-date').value;
            const blogUrl = document.getElementById('blog-url').value.trim();
            const memo = document.getElementById('blog-memo').value.trim();
            
            this.showLoader(true);
            try {
                const response = await API.submitBlog(state.currentCrew.crewId, date, blogUrl, memo);
                showToast(response.message, 'success');
                soundSynth.playSuccess();
                document.getElementById('form-blog-submit').reset();
                await this.refreshUserContext();
                if (state.activeView === 'checklist') this.renderChecklist();
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });

        // Test Commit/Review Forms
        document.getElementById('form-commit-submit').addEventListener('submit', async (e) => {
            e.preventDefault();
            const date = document.getElementById('commit-date').value;
            const url = document.getElementById('commit-url').value.trim();
            this.showLoader(true);
            try {
                const response = await API.submitCommit(state.currentCrew.crewId, date, url);
                showToast(response.message, response.earnedPoint > 0 ? 'success' : 'warning');
                if (response.earnedPoint > 0) soundSynth.playSuccess();
                document.getElementById('form-commit-submit').reset();
                await this.refreshUserContext();
                if (state.activeView === 'checklist') this.renderChecklist();
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });

        document.getElementById('form-review-submit').addEventListener('submit', async (e) => {
            e.preventDefault();
            const date = document.getElementById('review-date').value;
            const url = document.getElementById('review-url').value.trim();
            this.showLoader(true);
            try {
                const response = await API.submitReview(state.currentCrew.crewId, date, url);
                showToast(response.message, response.earnedPoint > 0 ? 'success' : 'warning');
                if (response.earnedPoint > 0) soundSynth.playSuccess();
                document.getElementById('form-review-submit').reset();
                await this.refreshUserContext();
                if (state.activeView === 'checklist') this.renderChecklist();
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });

        // Shop Buy Buttons
        document.getElementById('btn-buy-randombox').addEventListener('click', () => this.handleBuyRandomBox());
        document.getElementById('btn-buy-cafe-ticket').addEventListener('click', () => this.handleBuyCoachTicket('CAFE'));
        document.getElementById('btn-buy-meal-ticket').addEventListener('click', () => this.handleBuyCoachTicket('MEAL'));

        // Admin 일괄 동기화
        document.getElementById('btn-admin-github-sync').addEventListener('click', async () => {
            this.showLoader(true);
            try {
                const response = await API.adminGitHubSync();
                showToast(response.message, 'success');
                soundSynth.playCoin();
                await this.refreshUserContext();
                if (state.activeView === 'admin') this.renderAdmin();
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });

        // Mobile toggle
        const mobToggle = document.getElementById('mobile-menu-toggle');
        const sidebar = document.getElementById('sidebar');
        mobToggle.addEventListener('click', () => {
            sidebar.classList.toggle('open');
        });

        // Close sidebar on click navigate for mobile
        document.querySelectorAll('.sidebar-nav .nav-link').forEach(link => {
            link.addEventListener('click', () => {
                sidebar.classList.remove('open');
            });
        });
    },

    // Session helpers
    saveSession(githubId, crewId, nickname) {
        localStorage.setItem('crew_github_id', githubId);
        localStorage.setItem('crew_id', crewId);
        localStorage.setItem('crew_nickname', nickname);
    },

    clearSession() {
        localStorage.removeItem('crew_github_id');
        localStorage.removeItem('crew_id');
        localStorage.removeItem('crew_nickname');
    },

    async checkAuth() {
        const githubId = localStorage.getItem('crew_github_id');
        if (githubId) {
            try {
                await this.refreshUserContext();
                this.mainShell.style.display = 'flex';
                this.switchSubview('dashboard');
            } catch (err) {
                showToast('세션 만료 또는 로그인 오류가 발생해 다시 로그인이 필요합니다.', 'warning');
                this.clearSession();
                this.loginView.style.display = 'block';
            }
        } else {
            this.loginView.style.display = 'block';
        }
        this.showLoader(false);
    },

    async refreshUserContext() {
        const githubId = localStorage.getItem('crew_github_id');
        if (!githubId) return;
        
        const crewData = await API.getMe(githubId);
        state.currentCrew = crewData;
        
        // Sync Navbar profile fields
        document.getElementById('user-nickname').textContent = crewData.nickname;
        document.getElementById('user-github').textContent = `@${crewData.githubId}`;
        document.getElementById('user-points').textContent = crewData.point.toLocaleString();
        
        const trackBadge = document.getElementById('user-track-badge');
        trackBadge.textContent = crewData.track;
        trackBadge.className = `track-badge ${crewData.track.toLowerCase()}`;
        
        // Update user avatar from Github identicon (fallback if not loaded)
        document.getElementById('user-avatar').src = `https://github.com/${crewData.githubId}.png`;
        
        // Refresh points summaries
        const summary = await API.getPointsSummary(crewData.crewId);
        state.pointsSummary = summary;
        
        // Sync shop elements
        const shopPoints = document.getElementById('shop-user-points');
        if (shopPoints) shopPoints.textContent = `${crewData.point.toLocaleString()} P`;
        
        return crewData;
    },

    // Route swapping
    async switchSubview(viewId) {
        state.activeView = viewId;
        
        // Toggle view links
        document.querySelectorAll('.sidebar-nav .nav-link').forEach(link => {
            if (link.getAttribute('data-target') === viewId) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
        
        // Toggle panels
        document.querySelectorAll('.main-content .subview').forEach(panel => {
            panel.style.display = 'none';
        });
        
        const targetView = document.getElementById(`subview-${viewId}`);
        if (targetView) {
            targetView.style.display = 'flex';
        }
        
        // Trigger loaders
        this.showLoader(true);
        try {
            await this.refreshUserContext();
            if (viewId === 'dashboard') {
                await this.renderDashboard();
            } else if (viewId === 'checklist') {
                await this.renderChecklist();
            } else if (viewId === 'shop') {
                await this.renderShop();
            } else if (viewId === 'admin') {
                await this.renderAdmin();
            }
        } catch (err) {
            showToast(err.message, 'danger');
        } finally {
            this.showLoader(false);
        }
    },

    // 1. DASHBOARD SUBVIEW
    async renderDashboard() {
        const summary = state.pointsSummary;
        if (!summary) return;
        
        document.getElementById('dashboard-total-points').textContent = `${summary.totalPoint.toLocaleString()} P`;
        document.getElementById('dashboard-weekly-earned').textContent = summary.weeklyEarnedPoint;
        
        // Progress Bar
        const pct = Math.min(100, (summary.weeklyEarnedPoint / summary.weeklyLimitPoint) * 100);
        document.getElementById('dashboard-weekly-progress-bar').style.width = `${pct}%`;
        
        // Sub-Points
        document.getElementById('sum-commit-points').textContent = `${summary.activities.commit || 0} P`;
        document.getElementById('sum-review-points').textContent = `${summary.activities.review || 0} P`;
        document.getElementById('sum-mission-points').textContent = `${summary.activities.mission || 0} P`;
        document.getElementById('sum-blog-points').textContent = `${summary.activities.blog || 0} P`;
        
        // Load Rankings
        const [crews, coaches] = await Promise.all([
            API.getCrewRankings(),
            API.getCoachRankings()
        ]);
        
        state.crewRankings = crews;
        state.coachRankings = coaches;
        
        // Render Crew Rankings
        const crewTbody = document.getElementById('crew-ranking-list');
        crewTbody.innerHTML = crews.map((c, idx) => {
            const rBadge = idx < 3 ? `rank-${idx + 1}` : 'rank-other';
            const meClass = c.githubId === state.currentCrew.githubId ? 'style="background: rgba(99, 102, 241, 0.08); font-weight: 600;"' : '';
            return `
                <tr ${meClass}>
                    <td><span class="rank-badge ${rBadge}">${c.rank}</span></td>
                    <td>${c.nickname}</td>
                    <td>@${c.githubId}</td>
                    <td class="text-right font-outfit" style="color: var(--gold); font-weight: 700;">${c.point.toLocaleString()} P</td>
                </tr>
            `;
        }).join('') || '<tr><td colspan="4" class="text-center font-muted">랭킹이 기록되지 않았습니다.</td></tr>';
        
        // Render Coach Rankings
        const coachTbody = document.getElementById('coach-ranking-list');
        coachTbody.innerHTML = coaches.map((co, idx) => {
            const rBadge = idx < 3 ? `rank-${idx + 1}` : 'rank-other';
            return `
                <tr>
                    <td><span class="rank-badge ${rBadge}">${co.rank}</span></td>
                    <td><strong>${co.coachName} 코치님</strong></td>
                    <td class="text-right font-outfit" style="color: var(--primary); font-weight: 700;">${co.usedCount} 회</td>
                </tr>
            `;
        }).join('') || '<tr><td colspan="3" class="text-center font-muted">인기 코치 정보가 없습니다.</td></tr>';
    },

    // Trigger Github activity Sync (Crews endpoint)
    async triggerGitHubSync() {
        this.showLoader(true);
        try {
            const response = await API.syncGitHub(state.currentCrew.crewId);
            showToast(`${response.message} (커밋 ${response.commitSyncedCount}개, 리뷰 ${response.reviewSyncedCount}개 동기화)`, 'success');
            soundSynth.playCoin();
            await this.refreshUserContext();
            if (state.activeView === 'dashboard') await this.renderDashboard();
            if (state.activeView === 'checklist') await this.renderChecklist();
        } catch (err) {
            showToast(err.message, 'danger');
        } finally {
            this.showLoader(false);
        }
    },

    // 2. CHECKLIST SUBVIEW
    async renderChecklist() {
        const activities = await API.getWeeklyActivities(state.currentCrew.crewId);
        state.weeklyActivities = activities;
        
        // Header info
        document.getElementById('checklist-week-range').textContent = `${activities.weekStartDate} ~ ${activities.weekEndDate}`;
        document.getElementById('checklist-weekly-earned').textContent = activities.weeklyEarnedPoint;
        
        // Pre-fill forms date with current system local date
        const todayStr = new Date().toISOString().substring(0, 10);
        document.getElementById('mission-date').value = todayStr;
        document.getElementById('blog-date').value = todayStr;
        document.getElementById('commit-date').value = todayStr;
        document.getElementById('review-date').value = todayStr;

        // Render checklist items
        const listContainer = document.getElementById('checklist-items');
        listContainer.innerHTML = activities.items.map(item => {
            let itemIcon = '💻';
            if (item.type === 'REVIEW') itemIcon = '🔍';
            if (item.type === 'MISSION') itemIcon = '🏆';
            if (item.type === 'BLOG') itemIcon = '✍️';
            
            const pct = (item.earnedCount / item.maxCount) * 100;
            return `
                <div class="checklist-item">
                    <div class="chk-header-row">
                        <div class="chk-title-area">
                            <span class="chk-icon">${itemIcon}</span>
                            <span class="chk-name">${item.name}</span>
                        </div>
                        <span class="chk-points-badge">+ ${item.point}P / 회</span>
                    </div>
                    <div class="chk-body-row">
                        <div class="chk-progress-bar">
                            <div class="chk-progress-fill" style="width: ${pct}%"></div>
                        </div>
                        <div class="chk-numeric-stats">
                            <span class="earned">${item.earnedCount}</span> / <span class="max">${item.maxCount} 회</span>
                        </div>
                        <div class="chk-points-total">
                            ${item.earnedPoint} / ${item.maxPoint} P
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        // Render My Submissions list and status / rejection reason
        const mySubmissionsTbody = document.getElementById('my-submissions-list');
        if (!mySubmissionsTbody) return;

        try {
            const myActivities = await API.getCrewActivities(state.currentCrew.crewId);
            const manualActivities = myActivities.filter(act => act.type === 'MISSION' || act.type === 'BLOG');
            
            if (manualActivities.length === 0) {
                mySubmissionsTbody.innerHTML = `<tr><td colspan="5" class="text-center font-muted">인증 신청 내역이 존재하지 않습니다.</td></tr>`;
            } else {
                mySubmissionsTbody.innerHTML = manualActivities.map(act => {
                    let typeIcon = act.type === 'MISSION' ? '🏆' : '✍️';
                    let typeText = act.type === 'MISSION' ? '미션 성공' : '블로그/회고';
                    let statusClass = act.status.toLowerCase(); // pending, approved, rejected
                    let statusText = '';
                    if (act.status === 'PENDING') statusText = '검수 대기';
                    else if (act.status === 'APPROVED') statusText = '승인 완료';
                    else if (act.status === 'REJECTED') statusText = '반려됨';

                    const createdAtFormatted = new Date(act.createdAt).toLocaleString('ko-KR', {
                        month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                    });

                    // Evidence URL and Memo
                    let detailsHtml = '';
                    if (act.type === 'BLOG') {
                        detailsHtml += `<div class="sub-link-info">링크: <a href="${act.evidenceUrl}" target="_blank" class="evidence-link text-indigo" style="text-decoration: underline;">${act.evidenceUrl}</a></div>`;
                    } else if (act.type === 'MISSION') {
                        detailsHtml += `<div class="sub-link-info">증빙: <a href="${act.evidenceUrl}" target="_blank" class="evidence-link text-indigo" style="text-decoration: underline;">인증 사진 보기 🖼️</a></div>`;
                    }

                    if (act.memo) {
                        detailsHtml += `<div class="sub-memo-info font-muted" style="margin-top: 4px;">메모: ${act.memo}</div>`;
                    }

                    // If rejected, show reason
                    if (act.status === 'REJECTED' && act.rejectReason) {
                        detailsHtml += `
                            <div class="sub-reject-reason-box">
                                <span class="reject-icon">⚠️</span>
                                <span class="reject-label">반려 사유:</span>
                                <span class="reject-text">${act.rejectReason}</span>
                            </div>
                        `;
                    }

                    return `
                        <tr>
                            <td class="font-muted" style="font-size: 0.82rem;">${createdAtFormatted}</td>
                            <td style="font-weight: 600;"><span style="margin-right: 6px;">${typeIcon}</span>${typeText}</td>
                            <td style="font-family: var(--font-outfit); font-size: 0.9rem;">${act.activityDate}</td>
                            <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                            <td class="text-left" style="font-size: 0.88rem;">${detailsHtml}</td>
                        </tr>
                    `;
                }).join('');
            }
        } catch (e) {
            console.error("Failed to render my submissions list:", e);
            mySubmissionsTbody.innerHTML = `<tr><td colspan="5" class="text-center font-muted">내역을 불러오는 중 오류가 발생했습니다.</td></tr>`;
        }
    },

    // 3. SHOP SUBVIEW
    async renderShop() {
        const [tickets, histories] = await Promise.all([
            API.getMyTickets(state.currentCrew.crewId),
            API.getPointHistories(state.currentCrew.crewId)
        ]);
        
        state.myTickets = tickets;
        state.pointHistories = histories;
        
        // Update user balances in shop header
        document.getElementById('shop-user-points').textContent = `${state.currentCrew.point.toLocaleString()} P`;

        // Render My Tickets
        const ticketsContainer = document.getElementById('my-tickets-list');
        if (tickets.length === 0) {
            ticketsContainer.innerHTML = `<p class="text-center font-muted" style="padding: 30px 0;">보유한 이용권이 없습니다. 포인트를 모아 교환해 보세요!</p>`;
        } else {
            ticketsContainer.innerHTML = tickets.map(t => {
                const isCafe = t.type === 'CAFE';
                const isUsed = t.status === 'USED';
                const typeText = isCafe ? '카페 이용권' : '식사 이용권';
                const visualIcon = isCafe ? '☕' : '🍱';
                const badgeClass = isCafe ? 'cafe' : 'meal';
                
                const timeFormatted = new Date(t.createdAt).toLocaleString('ko-KR', {
                    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                });

                const useButton = isUsed ? 
                    `<button class="btn btn-ghost btn-sm" disabled style="opacity: 0.6;">사용 완료</button>` :
                    `<button class="btn btn-primary btn-sm" onclick="App.handleUseTicket(${t.ticketId})">사용하기</button>`;

                return `
                    <div class="ticket-item ${isUsed ? 'used' : ''}">
                        <div class="ticket-badge-visual ${badgeClass}">${visualIcon}</div>
                        <div class="ticket-details">
                            <h4 class="ticket-headline">${typeText}</h4>
                            <p class="ticket-coach">매칭 코치: <strong>${t.coachName} 코치님</strong></p>
                            <span class="ticket-time">${timeFormatted}</span>
                        </div>
                        <div class="ticket-action-box">
                            ${useButton}
                        </div>
                    </div>
                `;
            }).join('');
        }

        // Render Point History
        const historyTbody = document.getElementById('point-history-list');
        if (histories.length === 0) {
            historyTbody.innerHTML = `<tr><td colspan="4" class="text-center font-muted">포인트 적립 및 이용 내역이 존재하지 않습니다.</td></tr>`;
        } else {
            historyTbody.innerHTML = histories.map(h => {
                const isEarn = h.type === 'EARN';
                const typeText = isEarn ? '적립' : '차감';
                const badgeClass = isEarn ? 'earn' : 'use';
                
                const amountFormatted = isEarn ? `+${h.amount} P` : `${h.amount} P`;
                const amountColor = isEarn ? 'var(--success)' : 'var(--danger)';
                
                const timeFormatted = new Date(h.createdAt).toLocaleString('ko-KR', {
                    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                });

                return `
                    <tr>
                        <td><span class="history-badge ${badgeClass}">${typeText}</span></td>
                        <td style="color: ${amountColor}; font-weight: 700; font-family: var(--font-outfit);">${amountFormatted}</td>
                        <td>${h.reason}</td>
                        <td class="text-right font-muted" style="font-size: 0.78rem;">${timeFormatted}</td>
                    </tr>
                `;
            }).join('');
        }
    },

    // Purchase Random Box (Gamified opening chest)
    async handleBuyRandomBox() {
        if (state.currentCrew.point < 10) {
            showToast('포인트가 부족합니다. 최소 10P가 필요합니다.', 'warning');
            return;
        }

        const chest = document.getElementById('chest-animation-box');
        const modal = document.getElementById('randombox-modal');
        const modalChest = document.getElementById('modal-chest-visual');
        const modalTitle = document.getElementById('box-result-title');
        const resultCard = document.getElementById('box-result-card');
        const rewardBadge = document.getElementById('reward-badge');
        const rewardDesc = document.getElementById('reward-desc');
        const btnClose = document.getElementById('btn-close-box-modal');

        // trigger page shake visual
        chest.classList.add('chest-shake-effect');
        soundSynth.playPurchase();
        
        this.showLoader(true);
        try {
            const reward = await API.buyRandomBox(state.currentCrew.crewId);
            
            setTimeout(() => {
                chest.classList.remove('chest-shake-effect');
                this.showLoader(false);
                
                // Show modal overlay
                modal.style.display = 'flex';
                modalTitle.textContent = '상자 개봉 중...';
                modalChest.classList.add('chest-shake-effect');
                resultCard.style.opacity = '0';
                btnClose.style.display = 'none';

                // Play synth shaking effect
                let count = 0;
                const interval = setInterval(() => {
                    soundSynth.playPurchase();
                    count++;
                    if (count >= 3) clearInterval(interval);
                }, 300);

                setTimeout(async () => {
                    modalChest.classList.remove('chest-shake-effect');
                    
                    // Show final visual reward cards
                    resultCard.style.opacity = '1';
                    btnClose.style.display = 'inline-flex';
                    
                    if (reward.result === 'LOSE') {
                        modalTitle.textContent = '아쉽지만 꽝입니다!';
                        rewardBadge.textContent = '꽝';
                        rewardBadge.style.color = 'var(--text-muted)';
                        rewardDesc.textContent = reward.message;
                        soundSynth.playDefeat();
                    } else if (reward.result === 'POINT') {
                        modalTitle.textContent = '포인트 획득 성공!';
                        rewardBadge.textContent = `+${reward.rewardPoint} P`;
                        rewardBadge.style.color = 'var(--gold)';
                        rewardDesc.textContent = reward.message;
                        soundSynth.playCoin();
                    } else if (reward.result === 'COACH_TICKET') {
                        modalTitle.textContent = '🎉 코치 이용권 당첨!';
                        rewardBadge.textContent = `${reward.coach.name} 코치`;
                        rewardBadge.style.color = 'var(--primary)';
                        rewardDesc.textContent = reward.message;
                        soundSynth.playSuccess();
                    }

                    // Reload contexts
                    await this.refreshUserContext();
                    if (state.activeView === 'shop') await this.renderShop();

                    btnClose.onclick = () => {
                        modal.style.display = 'none';
                    };

                }, 1500);

            }, 500);

        } catch (err) {
            chest.classList.remove('chest-shake-effect');
            this.showLoader(false);
            showToast(err.message, 'danger');
        }
    },

    // Purchase Coach tickets
    async handleBuyCoachTicket(ticketType) {
        const price = ticketType === 'CAFE' ? 100 : 300;
        if (state.currentCrew.point < price) {
            showToast('포인트가 부족합니다.', 'warning');
            return;
        }

        customAlert('이용권 구매', `${ticketType === 'CAFE' ? '카페(100P)' : '식사(300P)'} 이용권을 교환하시겠습니까? 코치님은 랜덤 매칭됩니다.`, true, async () => {
            this.showLoader(true);
            try {
                const response = await API.buyCoachTicket(state.currentCrew.crewId, ticketType);
                showToast(response.message, 'success');
                soundSynth.playSuccess();
                await this.refreshUserContext();
                if (state.activeView === 'shop') await this.renderShop();
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });
    },

    // Use ticket (Called globally via inline element)
    async handleUseTicket(ticketId) {
        customAlert('이용권 사용', '코치 동의 하에 해당 이용권을 사용 처리하시겠습니까?', true, async () => {
            this.showLoader(true);
            try {
                const response = await API.useTicket(state.currentCrew.crewId, ticketId);
                showToast(response.message, 'success');
                soundSynth.playCoin();
                await this.refreshUserContext();
                if (state.activeView === 'shop') await this.renderShop();
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });
    },

    // 4. ADMIN PANEL SUBVIEW
    async renderAdmin() {
        const pending = await API.getPendingAdminActivities();
        state.pendingAdminActivities = pending;

        const pendingTbody = document.getElementById('admin-pending-list');
        if (pending.length === 0) {
            pendingTbody.innerHTML = `<tr><td colspan="5" class="text-center font-muted">현재 검수 대기 중인 활동 인증이 없습니다.</td></tr>`;
        } else {
            pendingTbody.innerHTML = pending.map(p => {
                const isMission = p.type === 'MISSION';
                const typeText = isMission ? '🏆 미션 성공' : '✍️ 블로그/회고';
                const typeColor = isMission ? 'var(--success)' : 'var(--warning)';

                let evidenceHtml = '';
                if (isMission) {
                    evidenceHtml = `<a href="${p.evidenceUrl}" target="_blank" class="evidence-link">🖼️ 사진 보기</a>`;
                } else {
                    evidenceHtml = `<a href="${p.evidenceUrl}" target="_blank" class="evidence-link">🔗 링크 열기</a>`;
                }

                return `
                    <tr>
                        <td><strong>${p.nickname}</strong> <span class="font-muted">(크루 ID: ${p.crewId})</span></td>
                        <td><span style="color: ${typeColor}; font-weight:600;">${typeText}</span></td>
                        <td>${evidenceHtml}</td>
                        <td><span class="font-muted" style="font-size:0.85rem;">${p.memo || '-'}</span></td>
                        <td class="text-center">
                            <div class="btn-actions-row">
                                <button class="btn btn-primary btn-sm" onclick="App.handleAdminApprove(${p.activityId})">승인</button>
                                <button class="btn btn-secondary btn-sm" style="color: var(--danger); border-color: rgba(239,68,68,0.2);" onclick="App.handleAdminReject(${p.activityId})">반려</button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        }
    },

    // Admin approve action
    async handleAdminApprove(activityId) {
        customAlert('인증 승인', '해당 활동 인증을 승인하고 크루에게 포인트를 지급하시겠습니까?', true, async () => {
            this.showLoader(true);
            try {
                const response = await API.adminApproveActivity(activityId);
                showToast(`활동 승인 완료! (+ ${response.earnedPoint}P 지급)`, 'success');
                soundSynth.playSuccess();
                await this.refreshUserContext();
                if (state.activeView === 'admin') await this.renderAdmin();
            } catch (err) {
                showToast(err.message, 'danger');
            } finally {
                this.showLoader(false);
            }
        });
    },

    // Admin reject action
    async handleAdminReject(activityId) {
        const reason = prompt('반려 사유를 입력해주세요:');
        if (reason === null) return; // cancel
        if (!reason.trim()) {
            showToast('반려 사유를 입력해야 합니다.', 'warning');
            return;
        }

        this.showLoader(true);
        try {
            const response = await API.adminRejectActivity(activityId, reason.trim());
            showToast(`활동 반려 처리가 완료되었습니다. 사유: ${response.reason}`, 'info');
            soundSynth.playDefeat();
            await this.refreshUserContext();
            if (state.activeView === 'admin') await this.renderAdmin();
        } catch (err) {
            showToast(err.message, 'danger');
        } finally {
            this.showLoader(false);
        }
    }
};

// Initialize Application once DOM fully loaded
window.addEventListener('DOMContentLoaded', () => {
    App.init();
});

// Bind globally accessible functions
window.App = App;
