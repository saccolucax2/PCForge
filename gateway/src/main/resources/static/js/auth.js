const API_URL = "/api/login";
const Auth = {
    /**
     * Esegue il login e restituisce i token JWT
     */
    async login(username, password, rememberMe = false) {
        const res = await fetch(`${API_URL}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        const text = await res.text();
        let data = {};
        try {
            data = text ? JSON.parse(text) : {};
        } catch (err) {
            console.warn("Answer was not JSON:", text);
        }

        if (!res.ok) {
            throw new Error(data.message || text || "Login failed");
        }

        const { accessToken, refreshToken, roles } = data;

        // Scegli lo storage
        const storage = rememberMe ? localStorage : sessionStorage;
        storage.setItem("accessToken", accessToken);
        storage.setItem("refreshToken", refreshToken);
        storage.setItem("username", username);
        storage.setItem("roles", roles);

        return data;
    },

    /**
     * Registra un nuovo utente
     */
    async register(user) {
        // Converti birthDate in formato dd/MM/yyyy se serve
        if (user.birthDate) {
            const parts = user.birthDate.split("-");
            if (parts.length === 3) {
                user.birthDate = `${parts[2]}/${parts[1]}/${parts[0]}`;
            }
        }

        const res = await fetch(`${API_URL}/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(user)
        });

        const text = await res.text();
        let data = {};
        try {
            data = text ? JSON.parse(text) : { username: user.username };
        } catch {
            data = { username: user.username };
        }

        if (!res.ok) {
            throw new Error(data.message || text || "Registration failed");
        }

        return data;
    },

    /**
     * Rinnova il token di accesso usando il refresh token
     */
    async refreshAccessToken() {
        const refreshToken = localStorage.getItem("refreshToken") || sessionStorage.getItem("refreshToken");
        if (!refreshToken) return null;

        const res = await fetch(`${API_URL}/refresh`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + refreshToken
            },
            body: JSON.stringify({ refreshToken })
        });

        const text = await res.text();
        let data = {};
        try {
            data = text ? JSON.parse(text) : {};
        } catch {
            data = {};
        }

        if (!res.ok) {
            this.logout();
            throw new Error(data.message || "Refresh token invalid or expired");
        }

        const { accessToken } = data;

        const storage = localStorage.getItem("refreshToken") ? localStorage : sessionStorage;
        storage.setItem("accessToken", accessToken);

        return accessToken;
    },

    /**
     * Effettua logout e pulisce i dati
     */
    logout() {
        localStorage.clear();
        sessionStorage.clear();
        window.location.href = "login.html";
    },

    /**
     * Restituisce il token JWT valido (effettua refresh automatico se necessario)
     */
    async getValidAccessToken() {
        const token = localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken");
        if (!token) return null;

        try {
            const payload = JSON.parse(atob(token.split(".")[1]));
            const exp = payload.exp * 1000;
            const now = Date.now();

            if (exp - now < 2 * 60 * 1000) { // meno di 2 minuti
                return await this.refreshAccessToken();
            }
        } catch (err) {
            console.warn("Invalid Token:", err);
            this.logout();
            return null;
        }

        return token;
    }
};

//