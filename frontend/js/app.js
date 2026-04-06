const LEGACY_DEFAULT_API_BASE_URL = 'https://localhost:8443';
const DEFAULT_API_BASE_URL = resolveDefaultApiBaseUrl();

function resolveDefaultApiBaseUrl() {
    if (typeof window !== 'undefined' && /^https?:$/.test(window.location.protocol) && window.location.host) {
        return `${window.location.protocol}//${window.location.host}`;
    }

    return 'https://localhost:8384';
}

function normalizeApiBaseUrl(url) {
    return url.replace(/\/+$/, '');
}

function getApiBaseUrl() {
    const savedUrl = localStorage.getItem('apiBaseUrl');
    const defaultUrl = normalizeApiBaseUrl(DEFAULT_API_BASE_URL);

    if (!savedUrl) {
        return defaultUrl;
    }

    const normalizedSavedUrl = normalizeApiBaseUrl(savedUrl);
    if (normalizedSavedUrl === LEGACY_DEFAULT_API_BASE_URL && normalizedSavedUrl !== defaultUrl) {
        return defaultUrl;
    }

    return normalizedSavedUrl;
}

function setApiBaseUrl(url) {
    localStorage.setItem('apiBaseUrl', normalizeApiBaseUrl(url));
}

function buildApiUrl(path) {
    if (/^https?:\/\//i.test(path)) {
        return path;
    }

    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    return `${getApiBaseUrl()}${normalizedPath}`;
}

function getToken() {
    return localStorage.getItem('token');
}

function getRole() {
    return localStorage.getItem('role');
}

function redirectToLogin() {
    window.location.href = '/login';
}

function checkAuth() {
    const token = getToken();
    if (!token) {
        redirectToLogin();
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    redirectToLogin();
}

async function fetchWithAuth(url, options = {}) {
    const token = getToken();

    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };

    const mergedOptions = {
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };

    if (options.body && typeof options.body === 'object') {
        mergedOptions.body = JSON.stringify(options.body);
    }

    const response = await fetch(buildApiUrl(url), mergedOptions);

    if (response.status === 401) {
        logout();
    }

    return response;
}
