const api = {
    baseUrl: '/api',
    _headers(isFormData = false) {
        const token = localStorage.getItem('token');
        const h = {};
        if (token) h['Authorization'] = `Bearer ${token}`;
        if (!isFormData) h['Content-Type'] = 'application/json';
        return h;
    },
    async _handle(res) {
        if (res.status === 401) {
            localStorage.clear();
            window.location.href = '/login.html';
            return;
        }
        if (!res.ok) {
            let err;
            try { err = await res.json(); } catch { err = {error: res.statusText}; }
            throw err;
        }
        if (res.status === 204) return null;
        
        // Handle empty bodies for 200 OK
        const text = await res.text();
        return text ? JSON.parse(text) : null;
    },
    get(path) { return fetch(`${this.baseUrl}${path}`, {headers: this._headers()}).then(this._handle); },
    post(path, body) { return fetch(`${this.baseUrl}${path}`, {method: 'POST', headers: this._headers(), body: JSON.stringify(body)}).then(this._handle); },
    put(path, body) { return fetch(`${this.baseUrl}${path}`, {method: 'PUT', headers: this._headers(), body: JSON.stringify(body)}).then(this._handle); },
    delete(path) { return fetch(`${this.baseUrl}${path}`, {method: 'DELETE', headers: this._headers()}).then(this._handle); },
    uploadFile(path, formData) { return fetch(`${this.baseUrl}${path}`, {method: 'POST', headers: this._headers(true), body: formData}).then(this._handle); },
    async downloadFile(path, filename) {
        const res = await fetch(`${this.baseUrl}${path}`, { headers: this._headers() });
        if (!res.ok) {
            let err;
            try { err = await res.json(); } catch { err = {error: res.statusText}; }
            throw err;
        }
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    },
    
    showToast(msg, type = 'success') {
        const t = document.createElement('div');
        t.className = `toast ${type}`;
        t.textContent = msg;
        document.body.appendChild(t);
        setTimeout(() => { t.style.opacity = '0'; setTimeout(() => t.remove(), 500); }, 3000);
    },
    
    getUser() {
        const u = localStorage.getItem('user');
        return u ? JSON.parse(u) : null;
    }
};
