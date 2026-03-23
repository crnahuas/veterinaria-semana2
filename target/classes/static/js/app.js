function getToken() {
    return localStorage.getItem('token');
}

function getRole() {
    return localStorage.getItem('role');
}

function checkAuth() {
    const token = getToken();
    if (!token) {
        window.location.href = '/login';
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    window.location.href = '/login';
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

    const response = await fetch(url, mergedOptions);

    if (response.status === 401) {
        logout();
    }

    return response;
}
