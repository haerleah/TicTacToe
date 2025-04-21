const originalFetch = window.fetch;
let accessTokenTimeoutId;
let refreshInProgress = null;

window.fetch = function (url, options = {}) {
    options.headers = options.headers || {};

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    if (csrfHeader && csrfToken) {
        options.headers[csrfHeader] = csrfToken;
    }

    return originalFetch(url, options);
};

document.addEventListener("DOMContentLoaded", () => {
    const currentTime = Math.floor(Date.now() / 1000);
    const accessToken = getCookie("access_token");
    const refreshToken = getCookie("refresh_token");

    if (refreshToken) {
        const tokenExp = parseJwt(refreshToken).exp;
        if (tokenExp > currentTime) {
            scheduleRefreshTokenUpdate(tokenExp * 1000);
        }
    }

    if(accessToken) {
        const tokenExp = parseJwt(accessToken).exp;
        if(tokenExp > currentTime) {
            scheduleTokenRefresh(tokenExp * 1000);
            document.dispatchEvent(new CustomEvent("tokenApproved"));
        } else if (refreshToken) {
            refreshAccessToken();
            document.dispatchEvent(new CustomEvent("tokenApproved"));
        }
    } else if (refreshToken) {
        refreshAccessToken();
        document.dispatchEvent(new CustomEvent("tokenApproved"));
    }
});

function scheduleTokenRefresh(expirationTime) {
    const refreshDelay = expirationTime - Date.now() - 60000;
    accessTokenTimeoutId = setTimeout(() => {
        refreshAccessToken();
    }, Math.max(refreshDelay, 0));
}

function scheduleRefreshTokenUpdate(expirationTimeMs) {
    const updateDelay = expirationTimeMs - Date.now() - 60000;
    setTimeout(() => {
        updateRefreshToken();
    }, Math.max(updateDelay, 0));
}

function refreshAccessToken() {
    if (refreshInProgress) {
        return refreshInProgress;
    }

    const params = new URLSearchParams();
    params.append("refreshToken", getCookie("refresh_token"));

    refreshInProgress = fetch("/auth/update_access_token", {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: params.toString()
    }).then(response => {
        if (response.status === 401) {
            document.cookie = "refresh_token=; path=/; max-age=0";
            if (window.location.pathname !== "/") {
                window.location.href = "/";
            }
            alert("The refresh token has expired or is invalid. You need to log in again!")
            return Promise.reject("Invalid refresh token");
        }
        return response.json();
    }).then(data => {
        document.cookie = "access_token=" + data.accessToken + "; path=/;";
        refreshInProgress = null;
        document.dispatchEvent(new CustomEvent("tokenApproved"));

        const tokenData = parseJwt(data.accessToken);
        if (tokenData && tokenData.exp) {
            scheduleTokenRefresh(tokenData.exp * 1000);
        }
    }).catch(error => console.error(error));
}

function updateRefreshToken() {
    const params = new URLSearchParams();
    params.append("refreshToken", getCookie("refresh_token"));

    refreshInProgress = fetch("/auth/update_refresh_token", {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: params.toString()
    }).then(response => {
        if (response.status === 401) {
            document.cookie = "refresh_token=; path=/auth/; max-age=0";
            if (window.location.pathname !== "/") {
                window.location.href = "/";
            }
            alert("The refresh token has expired or is invalid. You need to log in again!")
            return Promise.reject("Invalid refresh token");
        }
        return response.json();
    }).then(data => {
        document.cookie = "access_token=" + data.accessToken + "; path=/;";
        refreshInProgress = null;
        const accessTokenData = parseJwt(data.accessToken);
        const refreshTokenData = parseJwt(getCookie("refresh_token"));
        clearTimeout(accessTokenTimeoutId);
        if (accessTokenData && accessTokenData.exp) {
            scheduleTokenRefresh(accessTokenData.exp * 1000);
        }
        if (refreshTokenData && refreshTokenData.exp) {
            scheduleRefreshTokenUpdate(refreshTokenData.exp * 1000);
        }
    }).catch(error => console.error(error));
}

function getCookie(name) {
    let cookie = document.cookie.split('; ').find(row => row.startsWith(name + '='));
    return cookie ? cookie.split('=')[1] : null;
}

function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function (c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
}