// ---------- HELPERS ----------
const loadedBuilds = {};
const $ = sel => document.querySelector(sel);
const $$ = sel => Array.from(document.querySelectorAll(sel));
function save(key, val){ localStorage.setItem(key, JSON.stringify(val)); }
function load(key, fallback){ try{ return JSON.parse(localStorage.getItem(key)||'null')||fallback }catch(e){ return fallback } }

//----------- FETCH USER PROFILE -----------
async function fetchUserProfile(username) {
    try {
        const res = await authFetch(`/api/login/${username}`);
        if (!res.ok) throw new Error("Error getting user data.");
        return await res.json();
    } catch (err) {
        console.error(err);
        return null;
    }
}

/**
 * Esegue una fetch autenticata, gestendo automaticamente l'access token e il refresh token.
 */
async function authFetch(url, options = {}) {
    // 1️⃣ Prendi un access token valido (usa già la logica di refresh interna)
    let token = await Auth.getValidAccessToken();
    if (!token) {
        Auth.logout();
        return;
    }

    // 2️⃣ Aggiungi il token nell'header della richiesta
    options.headers = options.headers || {};
    options.headers['Authorization'] = `Bearer ${token}`;

    // 3️⃣ Fai la fetch
    let res = await fetch(url, options);

    // 4️⃣ Se il token non è più valido, prova a rinnovarlo e ripeti la chiamata
    if (res.status === 401 || res.status === 403) {
        console.warn("Access token expired, trying refresh...");
        token = await Auth.refreshAccessToken();
        if (!token) {
            Auth.logout();
            return;
        }

        options.headers['Authorization'] = `Bearer ${token}`;
        res = await fetch(url, options);
    }
    return res;
}

function getUsernameFromToken(token) {
    try {
        const payload = token.split('.')[1];
        const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        const data = JSON.parse(decoded);
        return data.sub || 'guest';
    } catch(e) {
        console.error("Error decoding token for username:", e);
        return 'guest';
    }
}

function getRolesFromToken(token) {
    try {
        const payload = token.split('.')[1];          // prendi la seconda parte
        const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        const data = JSON.parse(decoded);
        return data.roles || [];
    } catch(e) {
        console.error("Error during token decodification:", e);
        return [];
    }
}

// ---------- STATE ----------
let currentUser = load('user', { username:'guest', name:'Guest', surname:'', email:'guest@mail.it', dob:'' });
let builds = load('builds', []);
let posts = load('posts', []);

// ---------- NAV / LOADER ----------
const initFunctions = {
    profile: initProfile,
    profile_tech: initProfileTech,
    generator: initGenerator,
    comparator: initComparator,
    forum: initForum,
    chat: initChat
};

async function loadPageFragment(page){
    const container = document.getElementById('main-inner');
    if (page === 'profile') window.profileTechInitialized = window.profileTechInitialized || false;
    try {
        const res = await fetch(`pages/${page}.html`, {cache:'no-store'});
        if(!res.ok) new Error('Not found');
        container.innerHTML = await res.text();

        if (page === 'profile_tech') {
            window.profileTechInitialized = false; // forza reinizializzazione
            window.profileTechListenersAttached = false; // forza riattacco listener// forza reinizializzazione
        }

        // aggiorna sidebar username
        updateSidebarUsername();
        // call init if present
        if(typeof initFunctions[page] === 'function') initFunctions[page]();

        if (page === 'profile' && currentUser?.username || page === 'profile_tech' && currentUser?.username) {
            const usernameLabel = document.getElementById("usernameLabelInline");
            if (usernameLabel) usernameLabel.textContent = currentUser.username;
        }

        // --- Listener per delete account solo se siamo in profile ---
        if (page === 'profile' || page === 'profile_tech') {
            const deleteBtn = document.getElementById('deleteAccountBtn');
            deleteBtn?.addEventListener('click', async () => {
                if (!currentUser || !currentUser.username) {
                    alert("No user logged in, can't delete account.");
                    return;
                }

                const confirmDelete = confirm("⚠️ Are you sure you want to permanently delete your account? All your data, builds, posts, and conversations will be deleted.");
                if (!confirmDelete) return;

                try {
                    // --- Cancella tutte le build ---
                    const buildsRes = await authFetch(`/api/login/${currentUser.username}/builds`);
                    if (!buildsRes.ok) throw new Error("Error fetching user's builds");
                    const userBuilds = await buildsRes.json();

                    for (const buildId of userBuilds) {
                        await authFetch(`/api/login/build/${buildId}`, { method: 'DELETE' });
                    }

                    // --- Cancella tutti i post e i loro likes/commenti ---
                    const postsRes = await authFetch(`/api/login/posts/by/${currentUser.username}`);
                    if (!postsRes.ok) throw new Error("Error fetching users's posts");
                    const userPosts = await postsRes.json();

                    for (const postId of userPosts) {
                        // Likes
                        const likesRes = await authFetch(`/api/login/likes/POST/${postId}`, { method: 'GET' });
                        if (likesRes.ok) {
                            const likes = await likesRes.json();
                            for (const like of likes) {
                                await authFetch(`/api/login/likes/${like.id}`, { method: 'DELETE' });
                            }
                        }

                        // Comments
                        const commentRes = await authFetch(`/api/login/comments/POST/${postId}`, { method: 'GET' });
                        if (commentRes.ok) {
                            const comments = await commentRes.json();
                            for (const comment of comments) {
                                await authFetch(`/api/login/comments/${comment.id}`, { method: 'DELETE' });
                            }
                        }

                        // Cancella post
                        await authFetch(`/api/login/posts/${postId}`, { method: 'DELETE' });
                    }

                    // --- Cancella tutte le conversazioni e messaggi ---
                    try {
                        const convRes = await authFetch(`/api/login/conversations/${currentUser.username}`);
                        if (convRes.ok) {
                            const conversations = await convRes.json();
                            for (const conv of conversations) {
                                const convId = conv.id || conv._id;
                                if (!convId) continue;

                                // Cancella tutti i messaggi della conversazione (se necessario)
                                const msgRes = await authFetch(`/api/login/messages/${convId}`);
                                if (msgRes.ok) {
                                    const messages = await msgRes.json();
                                    for (const msg of messages) {
                                        // Non serve DELETE per singolo messaggio se il backend cancella tutto con DELETE /conversation/{id}
                                        // Ma puoi aggiungere se hai endpoint dedicato
                                    }
                                }

                                // Cancella la conversazione
                                await authFetch(`/api/login/conversation/${convId}`, { method: 'DELETE' });
                            }
                        }
                    } catch (err) {
                        console.warn("Error deleting conversations:", err);
                    }

                    // --- Cancella profilo tecnico se esiste ---
                    try {
                        await authFetch(`/api/login/technicians/${currentUser.username}/deleteTechnician`, { method: 'DELETE' });
                    } catch (err) {
                        console.warn("User is not a technician or error deleting technician:", err);
                    }

                    // --- Cancella utente ---
                    const delUserRes = await authFetch(`/api/login/${currentUser.username}`, { method: 'DELETE' });
                    if (!delUserRes.ok) throw new Error("Error deleting account");

                    // --- Pulizia locale ---
                    localStorage.clear();
                    sessionStorage.clear();

                    alert("✅ Account deleted succesfully.");
                    window.location.href = 'login.html';
                } catch (err) {
                    console.error("Error deleting account:", err);
                    alert("Error deleting account: " + err.message);
                }
            });
        }

    } catch(err) {
        container.innerHTML = `<div class="card"><p>Errore caricamento pagina: ${page}</p></div>`;
        console.error(err);
    }
}

function updateSidebarUsername(){
    const el = document.getElementById('sidebarUsername');
    if(el) el.innerText = currentUser.username || 'guest';
}

// ---------- NAV LISTENERS ----------
document.addEventListener('DOMContentLoaded', async () => {

    // menu clicks
    $$('.menu li').forEach(li => {
        li.addEventListener('click', async () => {
            $$('.menu li').forEach(x => x.classList.remove('active'));
            li.classList.add('active');
            const page = li.dataset.page;
            await loadPageFragment(page);
        });
    });

    // logout (sidebar)
    $('#logout')?.addEventListener('click', () => {
        localStorage.removeItem('user');
        window.location.href = 'login.html';
    });

    // carica default profile
    const defaultLi = document.querySelector('.menu li[data-page="profile"]');
    if (defaultLi) defaultLi.classList.add('active');
    await loadPageFragment('profile');
});

// ---------- INIT: PROFILE ----------
async function initProfile() {
    // Mostra subito username dal local/session storage
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    let username = token ? getUsernameFromToken(token) : 'guest';
    currentUser.username = username; // aggiorna currentUser per coerenza
    document.querySelectorAll("#usernameLabelInline").forEach(el => el.textContent = username);
    updateSidebarUsername();
    // Assicuriamoci che currentUser.roles esista
    // Assicuriamoci che currentUser.roles esista
    if (!currentUser.roles) {
        const token1 = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        currentUser.roles = token1 ? getRolesFromToken(token1) : [];
    }

// --- MOSTRA / NASCONDI BOTTONE "Become Technician" ---
    const becomeTechBtn1 = document.getElementById('becomeTechBtn');
    if (becomeTechBtn1) {
        if (currentUser.roles.includes("TECHNICIAN")) {
            becomeTechBtn1.style.display = 'none'; // Nascondi bottone se è già TECH
        } else {
            becomeTechBtn1.style.display = 'inline-block'; // Mostra bottone se non è TECH
        }
    }

// Se l’utente è TECHNICIAN, carica la sezione tecnico solo una volta
    if (currentUser.roles.includes("TECHNICIAN")) {

        // Carica frammento HTML solo se non esiste
        if (!document.getElementById('techFormInline')) {
            await loadPageFragment('profile_tech');
            await initProfileTech();
        }

        // Se non inizializzato, chiama initProfileTech e setta flag
        if (!window.profileTechInitialized) {
            await initProfileTech();
            window.profileTechInitialized = true;
        } else {
            // Se già inizializzato, aggiorna i dati senza riattaccare listener
            if (typeof refreshTechData === 'function') {
                await refreshTechData();
            }
        }
    }

    const profileEmail = document.getElementById('profileEmailInline');
    const profileName = document.getElementById('nameInline');
    const profileSurname = document.getElementById('surnameInline');
    const profileDob = document.getElementById('dobInline');
    const profileForm = document.getElementById('profileFormInline');
    const editBtn = document.getElementById('editProfileBtn');
    const saveBtn = document.getElementById('saveProfileBtn');

    // Recupera i dati dal backend prima di popolare i campi
    try {
        const data = await fetchUserProfile(username);
        if (data) {
            if(profileEmail) profileEmail.value = data.email || '';
            if(profileName) profileName.value = data.name || '';
            if(profileSurname) profileSurname.value = data.surname || '';
            if(profileDob && data.birthDate) {
                const [dd, mm, yyyy] = data.birthDate.split('/');
                profileDob.value = `${yyyy}-${mm.padStart(2,'0')}-${dd.padStart(2,'0')}`;
            }

            // aggiorna currentUser così i salvataggi funzionano
            currentUser = { username, ...data };
            save('user', currentUser);
        }
    } catch(err) {
        console.error("Error loading profile:", err);
    }

    document.getElementById('logoutBtnInline')?.addEventListener('click', () => {
        localStorage.removeItem('user');
        sessionStorage.removeItem('user');
        window.location.href = 'login.html';
    });

    // --- Modalità di editing ---
    let editing = false;
    editBtn?.addEventListener('click', () => {
        editing = !editing;
        const inputs = [profileEmail, profileName, profileSurname, profileDob];
        inputs.forEach(inp => inp.readOnly = !editing);
        saveBtn.disabled = !editing;

        const icon = editBtn.querySelector('i');
        if (editing) {
            icon.classList.remove('ph-pencil');
            icon.classList.add('ph-x');
            editBtn.title = 'Cancel editing';
        } else {
            icon.classList.remove('ph-x');
            icon.classList.add('ph-pencil');
            editBtn.title = 'Edit profile';
        }
    });

    // --- Salvataggio profilo ---
    if(profileForm){
        profileForm.onsubmit = async (e) => {
            e.preventDefault();

            const updatedUser = {
                username: currentUser.username,
                email: profileEmail.value,
                name: profileName.value,
                surname: profileSurname.value,
                password: currentUser.password, // 🔹 mantieni la password
                birthDate: profileDob.value.split('-').reverse().join('/'),
                generatedBuilds: currentUser.generatedBuilds || []
            };

            try {
                const res = await authFetch(`/api/login/${username}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(updatedUser)
                });

                if (!res.ok) throw new Error('Error during updating');

                // Aggiorna currentUser locale
                currentUser = { ...updatedUser };
                save('user', currentUser);
                updateSidebarUsername();

                // Feedback come prima
                let msg = profileForm.querySelector('.profile-msg');
                if(!msg){
                    msg = document.createElement('div');
                    msg.className = 'profile-msg';
                    msg.style.textAlign = 'center';
                    msg.style.color = '#0172dd';
                    profileForm.appendChild(msg);
                }
                msg.innerText = 'Profile updated successfully!';
                setTimeout(()=> msg.innerText='', 3000);

                // Torna in modalità read-only
                editing = false;
                [profileEmail, profileName, profileSurname, profileDob].forEach(inp => inp.readOnly = true);
                saveBtn.disabled = true;

                const icon = editBtn.querySelector('i');
                icon.classList.remove('ph-x');
                icon.classList.add('ph-pencil');
                editBtn.title = 'Edit profile';

            } catch(err) {
                console.error(err);
                alert('Error during saving profile.');
            }
        };
    }

// --- Diventa tecnico ---
    const becomeTechBtn = document.getElementById('becomeTechBtn');
    becomeTechBtn?.addEventListener('click', async () => {
        if (!currentUser) return alert("User not logged in");

        // Assicuriamoci che l'array dei ruoli esista
        if (!currentUser.roles) currentUser.roles = [];

        // Aggiungi TECHNICIAN se non presente
        if (!currentUser.roles.includes("TECHNICIAN")) {
            currentUser.roles.push("TECHNICIAN");
        }

        try {
            // Recupera token dal localStorage o sessionStorage
            const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
            if (!token) return alert("Missing token, login");

            // Prepara payload aggiornato
            const userPayload = {
                username: currentUser.username,
                name: currentUser.name,
                surname: currentUser.surname,
                email: currentUser.email,
                password: currentUser.password,
                birthDate: currentUser.birthDate,
                generatedBuilds: currentUser.generatedBuilds || [],
                roles: currentUser.roles
            };

            // 🔹 PUT al backend
            const res = await authFetch(`/api/login/${currentUser.username}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify(userPayload)
            });

            if (!res.ok) throw new Error("Error upgrading role");

            // 🔹 Backend deve restituire nuovo accessToken
            const updatedData = await res.json();
            if (updatedData.accessToken) {
                localStorage.setItem('accessToken', updatedData.accessToken);
                currentUser.roles = getRolesFromToken(updatedData.accessToken);
                save('user', currentUser); // salva sempre il ruolo aggiornato
            } else {
                alert("Backend didn't gave back updated token.");
                return;
            }

            // Mostra sezione TECH subito
            await loadPageFragment('profile_tech');
            if (!window.profileTechInitialized) {
                await initProfileTech();
                window.profileTechInitialized = true;
            }

            alert("You've succesfully requested the Technician role upgrade!");

        } catch (err) {
            console.error(err);
            alert("Error applying Technician role: " + err.message);
        }
    });

}

function populateBaseProfileFields() {
    if (!currentUser) return;

    const profileEmail = document.getElementById('profileEmailInline');
    const profileName = document.getElementById('nameInline');
    const profileSurname = document.getElementById('surnameInline');
    const profileDob = document.getElementById('dobInline');

    // Campi base
    if (profileEmail) profileEmail.value = currentUser.email || '';
    if (profileName) profileName.value = currentUser.name || '';
    if (profileSurname) profileSurname.value = currentUser.surname || '';

    // Trasforma DD/MM/YYYY in YYYY-MM-DD per input type="date"
    if (profileDob && currentUser.birthDate) {
        const [dd, mm, yyyy] = currentUser.birthDate.split('/');
        profileDob.value = `${yyyy}-${mm.padStart(2,'0')}-${dd.padStart(2,'0')}`;
    }

    // Aggiorna username in sidebar o header
    document.querySelectorAll("#usernameLabelInline").forEach(el => {
        el.textContent = currentUser.username || 'guest';
    });

}

// ---------- INIT: PROFILE TECH ----------
async function initProfileTech() {
    // Recupera username
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    const username = token ? getUsernameFromToken(token) : 'guest';
    currentUser.username = username;
    updateSidebarUsername();

    // Fetch dati tecnico dal backend
    let techData = null;
    try {
        const res = await authFetch(`/api/login/technicians/${username}`);
        if (res && res.ok) techData = await res.json();
    } catch (err) {
        console.error("Error fetching technician data:", err);
    }

    // Popola campi
    populateTechFields(techData);

    // --- Recupera e mostra rating medio del tecnico ---
    try {
        const ratingRes = await authFetch(`/api/login/technicians/${username}/rating`);
        if (ratingRes.ok) {
            const data = await ratingRes.json();
            const rating = data.ratingPoints || 0;
            showStarRating(rating);
        } else {
            console.warn("No rating found for the technician:", username);
            showStarRating(0);
        }
    } catch (err) {
        console.error("Error fetching rating:", err);
        showStarRating(0);
    }

    // Reset stato form (sempre)
    window.editingTech = false;
    const bioField = document.getElementById('bioInline');
    const saveTechBtn = document.getElementById('saveTechBtn');
    const addSkillBtn = document.getElementById('addSkillBtn');
    const addCertificationBtn = document.getElementById('addCertificationBtn');

    if (bioField) bioField.readOnly = true;
    if (saveTechBtn) saveTechBtn.disabled = true;
    if (addSkillBtn) addSkillBtn.style.display = "none";
    if (addCertificationBtn) addCertificationBtn.style.display = "none";

    // Attacca listener solo la prima volta
    if (!window.profileTechListenersAttached) {
        attachTechListeners();
        window.profileTechListenersAttached = true;
    }
}

// ---------- ATTACH LISTENERS ----------
function attachTechListeners() {
    const editTechBtn = document.getElementById('editTechBtn');
    const saveTechBtn = document.getElementById('saveTechBtn');
    const addSkillBtn = document.getElementById('addSkillBtn');
    const addCertificationBtn = document.getElementById('addCertificationBtn');
    const bioField = document.getElementById('bioInline');
    const skillsList = document.getElementById('skillsListInline');
    const certificationsList = document.getElementById('certificationsListInline');
    const profilePhoto = document.querySelector('.profile-photo');
    const profileImage = document.getElementById('techPhoto');
    const overlay = profilePhoto.querySelector('.edit-overlay');
    const profileImageInput = document.getElementById('profileImageInput') || createProfileImageInput();

    // --- Cambio immagine ---
    profileImageInput.addEventListener("change", e => {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = ev => profileImage.src = ev.target.result;
        reader.readAsDataURL(file);
    });

    // --- Aggiungi skill ---
    addSkillBtn.addEventListener('click', () => {
        const skill = prompt("Enter new skill:");
        if (!skill) return;
        if (skillsList.querySelector('.empty')) skillsList.innerHTML = '';
        const li = createLi(skill);
        if (window.editingTech) attachRemoveBtn(li, skillsList, certificationsList);
        skillsList.appendChild(li);
    });

    // --- Aggiungi certificazione ---
    addCertificationBtn.addEventListener('click', () => {
        const cert = prompt("Enter new certification:");
        if (!cert) return;
        if (certificationsList.querySelector('.empty')) certificationsList.innerHTML = '';
        const li = createLi(cert);
        if (window.editingTech) attachRemoveBtn(li, skillsList, certificationsList);
        certificationsList.appendChild(li);
    });

    // --- Edit tecnico ---
    editTechBtn.addEventListener('click', () => {
        window.editingTech = !window.editingTech;
        const editing = window.editingTech;

        bioField.readOnly = !editing;
        saveTechBtn.disabled = !editing;
        addSkillBtn.style.display = editing ? 'inline-block' : 'none';
        addCertificationBtn.style.display = editing ? 'inline-block' : 'none';

        const icon = editTechBtn.querySelector('i');
        icon.classList.toggle('ph-pencil', !editing);
        icon.classList.toggle('ph-x', editing);

        if (editing) {
            profilePhoto.classList.add('editable');
            overlay.style.display = 'flex';
            profilePhoto.onclick = () => profileImageInput.click();
        } else {
            profilePhoto.classList.remove('editable');
            overlay.style.display = 'none';
            profilePhoto.onclick = null;
        }

        // Aggiorna delete button su skills/certifications
        skillsList.querySelectorAll(".delete-icon").forEach(btn => btn.remove());
        certificationsList.querySelectorAll(".delete-icon").forEach(btn => btn.remove());

        if (editing) {
            skillsList.querySelectorAll("li:not(.empty)").forEach(li => attachRemoveBtn(li, skillsList, certificationsList));
            certificationsList.querySelectorAll("li:not(.empty)").forEach(li => attachRemoveBtn(li, skillsList, certificationsList));
        }
    });

    // --- Submit tecnico ---
    document.getElementById("techFormInline")?.addEventListener("submit", async e => {
        e.preventDefault();
        const skills = Array.from(skillsList.querySelectorAll("li:not(.empty)")).map(li => li.firstChild.nodeValue.trim());
        const certifications = Array.from(certificationsList.querySelectorAll("li:not(.empty)")).map(li => li.firstChild.nodeValue.trim());
        const technicianProfile = {
            userId: currentUser.username,
            bio: bioField.value,
            photoUrl: profileImage.src,
            skills,
            certifications,
            pointsBalance: 0,
            transactions: []
        };
        try {
            const res = await authFetch(`/api/login/technicians`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(technicianProfile)
            });
            if (!res.ok) throw new Error(await res.text() || "Error saving");
            const saved = await res.json();
            currentUser = { ...currentUser, ...saved };
            save('user', currentUser);
            alert("Technician profile saved successfully!");
            editTechBtn.click(); // chiudi editing
        } catch(err) {
            console.error(err);
            alert("Error saving technician profile: " + err.message);
        }
    });
}

// ---------- POPOLARE CAMPi ----------
function populateTechFields(data) {
    const bioField = document.getElementById('bioInline');
    const profileImage = document.getElementById('techPhoto');
    const skillsList = document.getElementById('skillsListInline');
    const certificationsList = document.getElementById('certificationsListInline');

    const userData = data || currentUser?.technicianProfile || {};

    if (bioField) bioField.value = userData.bio || '';
    if (profileImage) profileImage.src = userData.photoUrl || '/img/default-avatar.jpg';

    // Skills
    if (Array.isArray(userData.skills) && userData.skills.length) {
        skillsList.innerHTML = '';
        userData.skills.forEach(skill => skillsList.appendChild(createLi(skill)));
    } else skillsList.innerHTML = `<li class="empty">No skills</li>`;

    // Certifications
    if (Array.isArray(userData.certifications) && userData.certifications.length) {
        certificationsList.innerHTML = '';
        userData.certifications.forEach(cert => certificationsList.appendChild(createLi(cert)));
    } else certificationsList.innerHTML = `<li class="empty">No certifications</li>`;
}

// ---------- CREATE LI ----------
function createLi(text) {
    const li = document.createElement('li');
    li.textContent = text;
    return li;
}

// ---------- ATTACH REMOVE BUTTON ----------
function attachRemoveBtn(li, skillsList, certificationsList) {
    const removeBtn = document.createElement("i");
    removeBtn.className = "ph ph-trash delete-icon";
    removeBtn.style.cursor = "pointer";
    removeBtn.style.marginLeft = "auto";
    removeBtn.onclick = () => {
        li.remove();
        if (skillsList.children.length === 0) skillsList.innerHTML = `<li class="empty">No skills</li>`;
        if (certificationsList.children.length === 0) certificationsList.innerHTML = `<li class="empty">No certifications</li>`;
    };
    li.appendChild(removeBtn);
}

// ---------- CREATE PROFILE IMAGE INPUT ----------
function createProfileImageInput() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.id = 'profileImageInput';
    input.style.display = 'none';
    document.body.appendChild(input);
    return input;
}

async function refreshTechData() {
    const username = localStorage.getItem('username') || sessionStorage.getItem('username');
    if (!username) return;

    try {
        const res = await authFetch(`/api/login/technicians/${username}`);
        if (!res.ok) throw new Error('Failed to fetch technician data');
        const techData = await res.json();

        document.getElementById('bioInline').value = techData.bio || '';
        document.getElementById('techPhoto').src = techData.photoUrl || '/img/default-avatar.jpg';

        const skillsList = document.getElementById('skillsListInline');
        const certificationsList = document.getElementById('certificationsListInline');

        skillsList.innerHTML = techData.skills?.length
            ? techData.skills.map(s => `<li>${s}</li>`).join('')
            : `<li class="empty">No skills</li>`;

        certificationsList.innerHTML = techData.certifications?.length
            ? techData.certifications.map(c => `<li>${c}</li>`).join('')
            : `<li class="empty">No certifications</li>`;

        currentUser.bio = techData.bio || '';
        currentUser.skills = techData.skills || [];
        currentUser.certifications = techData.certifications || [];
        currentUser.photo = techData.photoUrl || '';
        save('user', currentUser);

        // Se siamo in editing, riattacca delete button
        if (window.editingTech) {
            skillsList.querySelectorAll("li:not(.empty)").forEach(li => {
                const btn = li.querySelector(".delete-icon");
                if (!btn) li.appendChild(createRemoveBtn(li, skillsList, certificationsList));
            });
            certificationsList.querySelectorAll("li:not(.empty)").forEach(li => {
                const btn = li.querySelector(".delete-icon");
                if (!btn) li.appendChild(createRemoveBtn(li, skillsList, certificationsList));
            });
        }

    } catch (err) {
        console.error('Error refreshing tech data:', err);
    }
}

// ---------- MOSTRA RATING STELLE STATICO ----------
function showStarRating(rating) {
    const stars = document.querySelectorAll('.reviews-section i.ph-star');
    const ratingLabel = document.querySelector('.reviews-section h4');

    if (!stars || !ratingLabel) return;

    // Assicurati che sia un numero valido
    const ratingInt = Math.max(0, Math.min(5, Math.round(rating)));

    // Colora le stelle in base al valore
    stars.forEach((star, index) => {
        if (index < ratingInt) {
            star.classList.remove('ph', 'ph-star');
            star.classList.add('ph-fill', 'ph-star');
            star.style.color = '#ccc';
        } else {
            star.classList.remove('ph-fill', 'ph-star');
            star.classList.add('ph', 'ph-star');
            star.style.color = '#ccc';
        }
    });

    // Aggiorna il numero accanto alle stelle
    ratingLabel.textContent = `[${ratingInt}]`;
}

// ---------- INIT: GENERATOR ----------
function initGenerator(){
    const form = document.getElementById('generatorFormInline');
    const ul = document.getElementById('generatedBuildInline');
    const saveBtn = document.getElementById('saveBuildBtnInline');

    // Budget controls
    const incBtn = document.querySelector('.btn-inc');
    const decBtn = document.querySelector('.btn-dec');
    const input = document.getElementById('budgetInline');

    if (input && incBtn && decBtn) {
        incBtn.addEventListener('click', () => {
            let val = parseInt(input.value || 0);
            input.value = val + 10;
        });

        decBtn.addEventListener('click', () => {
            let val = parseInt(input.value || 0);
            input.value = Math.max(0, val - 10);
        });
    }

    if(form){
        form.onsubmit = async (e) => {
            e.preventDefault();
            const budget = Number(document.getElementById('budgetInline')?.value || 0);

            if (!budget || budget <= 0) {
                alert("Inavlid Budget");
                return;
            }

            try {
                const res = await authFetch(`api/login/build/generate?budget=${budget}`, {
                    method: "GET",
                    headers: { "Accept": "application/json" }
                });

                if (!res.ok) {
                    const text = await res.text();
                    throw new Error(text || "Server Error");
                }

                const generatedBuild = await res.json();

                // 🔹 Salva correttamente la build globalmente
                window.generatedBuild = generatedBuild;

                // Mostra componenti
                ul.innerHTML = "";
                generatedBuild.components.forEach(c => {
                    // 🔹 Normalizzazione dei tipi
                    let displayType = c.type;
                    if (displayType.toUpperCase().startsWith("DDR")) displayType = "RAM";
                    if (displayType.toUpperCase().includes("COOLER")) displayType = "COOLER";

                    const li = document.createElement("li");
                    li.textContent = `${displayType}: ${c.model} (${c.price} €)`;
                    ul.appendChild(li);
                });


                // Totale
                const totalLi = document.createElement("li");
                totalLi.innerHTML = `<strong>Totale: ${generatedBuild.totalPrice}€</strong>`;
                ul.appendChild(totalLi);

                saveBtn.style.display = "block";

            } catch (err) {
                console.error(err);
                alert("Error during Build Generating: " + err.message);
            }
        };
    }

    if (saveBtn) {
        saveBtn.onclick = async () => {
            const username =
                localStorage.getItem("username") ||
                sessionStorage.getItem("username");

            const build = window.generatedBuild;

            if (!username || !build) {
                alert("Missing data");
                return;
            }

            // ✅ Build pronta per essere salvata
            const buildToSave = {
                components: build.components.map(c => {
                    let type = c.type;

                    // Forza i tipi conosciuti dal backend
                    if (type.toUpperCase().startsWith("DDR")) type = "RAM"; // tutte le RAM
                    if (type.toUpperCase().includes("COOLER")) type = "COOLER"; // qualsiasi cooler

                    return {
                        id: c.id,
                        type: type,
                        brand: c.brand || "",
                        model: c.model,
                        price: c.price,
                        // campi specifici
                        ...(type === "CPU" && {
                            cores: c.cores,
                            threads: c.threads,
                            socketType: c.socketType,
                            cacheSize: c.cacheSize,
                            freqBase: c.freqBase,
                            freqBoost: c.freqBoost
                        }),
                        ...(type === "GPU" && {
                            memory: c.memory,
                            CUDACores: c.CUDACores || c.cudacores,
                            memoryBus: c.memoryBus,
                            baseClock: c.baseClock,
                            boostClock: c.boostClock,
                            TDP: c.TDP || c.tdp
                        }),
                        ...(type === "RAM" && {
                            typeRAM: c.type || c.typeRAM,
                            capacity: c.capacity,
                            frequency: c.frequency,
                            CASLatency: c.CASLatency || c.caslatency,
                            tension: c.tension
                        }),
                        ...(type === "MOTHERBOARD" && {
                            chipset: c.chipset,
                            socket: c.socket,
                            ramSupport: c.ramSupport || c.ramsupport,
                            slotM2: c.slotM2
                        }),
                        ...(type === "PSU" && {
                            wattage: c.wattage,
                            efficiencyRating: c.efficiencyRating,
                            ventola: c.ventola,
                            connectors: c.connectors || c.principalConnector || []
                        }),
                        ...(type === "COOLER" && {
                            typeCooler: c.typeCooler || c.type,
                            socketCompatibility: c.socketCompatibility,
                            ventola: c.ventola,
                            heatPipes: c.heatPipes
                        }),
                        ...(type === "SSD" && {
                            formFactor: c.formFactor,
                            interfaceType: c.interfaceType,
                            capacity: c.capacity,
                            readSpeed: c.readSpeed,
                            writeSpeed: c.writeSpeed
                        }),
                        ...(type === "CASE" && {
                            formFactor: c.formFactor,
                            Bay: c.Bay || c.bay,
                            Pannelli: c.Pannelli || c.panelType,
                            Ventola: c.Ventola || c.ventola
                        })
                    };
                })
            };

            try {
                // 1️⃣ Salva la build nel DB
                const createRes = await authFetch(`/api/login/build/create`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(buildToSave)
                });

                if (!createRes.ok) {
                    const errText = await createRes.text();
                    throw new Error(errText);
                }

                const createdBuild = await createRes.json();
                const buildId = createdBuild.id;

                // 2️⃣ Aggiungi la build all'utente
                const addRes = await authFetch(`/api/login/${username}/builds/${buildId}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: "{}"
                });

                if (!addRes.ok) {
                    const errText = await addRes.text();
                    throw new Error(errText);
                }

                alert("Build saved succesfully!");
            } catch (err) {
                console.error(err);
                alert("Error during build saving: " + err.message);
            }
        };
    }
}

// ---------- INIT: COMPARATOR ----------
async function initComparator() {
    await renderBuildsInline();

    const compareForm = document.getElementById('compareFormInline');
    const out = document.getElementById('compareResultInline'); // contenitore del risultato
    if (compareForm) {
        compareForm.onsubmit = async (e) => {
            e.preventDefault();
            const id1 = parseInt(document.getElementById('compareSelect1Inline')?.value);
            const id2 = parseInt(document.getElementById('compareSelect2Inline')?.value);

            if (!id1 || !id2) {
                if (out) out.innerHTML = '<p>Select two valid Builds.</p>';
                return;
            }

            try {
                // chiama il backend per confrontare le build
                const res = await authFetch(`/api/login/build/compare?id1=${id1}&id2=${id2}`);
                if (!res.ok) throw new Error('Errore nel confronto delle build');
                const comparisonText = await res.text(); // backend restituisce un messaggio testuale formattato

                // visualizza sotto i menu a tendina
                if (out) {
                    // usa <pre> per mantenere la formattazione originale (a capo, spazi)
                    out.innerHTML = `<pre>${comparisonText}</pre>`;
                }
            } catch (err) {
                console.error(err);
                if (out) out.innerHTML = `<p>Error comparing builds: ${err.message}</p>`;
            }
        };
    }

    // click handler per copia/cestino rimane invariato
    document.getElementById('savedBuildsInline')?.addEventListener('click', async (ev) => {
        const li = ev.target.closest('li');
        if (!li) return;
        const id = Number(li.dataset.id);
        if (!id) return;

        // --- DELETE BUILD ---
        if (ev.target.classList.contains('delete-icon')) {
            if (confirm('Delete this build?')) {
                try {
                    const username = getUsernameFromToken(localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken")); // prendi username dal token
                    // 1️⃣ Rimuovi la build dal servizio build
                    const res1 = await authFetch(`/api/login/build/${id}`, { method: "DELETE" });
                    if (!res1.ok) {
                        const errText = await res1.text();
                        throw new Error("Error deleting build: " + errText);
                    }

                    // 2️⃣ Rimuovi la build dall'utente
                    const res2 = await authFetch(`/api/login/${username}/builds/${id}`, { method: "DELETE" });
                    if (!res2.ok) {
                        const errText = await res2.text();
                        throw new Error("Error removing build from user: " + errText);
                    }

                    // ✅ Aggiorna la lista locale
                    builds = builds.filter(b => b.id !== id);
                    save('builds', builds);
                    await renderBuildsInline();

                    // feedback visivo
                    ev.target.classList.add('deleted');
                    ev.target.title = 'Deleted!';
                    setTimeout(() => {
                        ev.target.classList.remove('deleted');
                        ev.target.title = 'Delete build';
                    }, 1500);
                } catch (err) {
                    console.error(err);
                    alert(err.message);
                }
            }
            ev.stopPropagation();
        }

        // --- COPY BUILD ---
        if (ev.target.classList.contains('copy-icon')) {
            const buildId = li.dataset.id;
            const build = loadedBuilds[buildId];
            if (!build) {
                alert('Build data not loaded.');
                return;
            }

            const componentsText = build.components
                .map(c => {
                    let displayType = c.type;
                    if (displayType.toUpperCase().startsWith("DDR")) displayType = "RAM";
                    if (displayType.toUpperCase().includes("COOLER")) displayType = "COOLER";
                    return `• ${displayType}: ${c.model}`;
                })
                .join('\n');

            const textToCopy = `💻 Build ID: ${build.id}\n${componentsText}`;

            try {
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    await navigator.clipboard.writeText(textToCopy);
                } else {
                    const tempInput = document.createElement('textarea');
                    tempInput.value = textToCopy;
                    document.body.appendChild(tempInput);
                    tempInput.select();
                    document.execCommand('copy');
                    document.body.removeChild(tempInput);
                }

                // ✅ Feedback visivo
                ev.target.classList.add('copied');
                ev.target.title = 'Copied!';
                setTimeout(() => {
                    ev.target.classList.remove('copied');
                    ev.target.title = 'Copy build';
                }, 1500);

            } catch (err) {
                console.error('Copy failed:', err);
                alert('Copy failed: ' + err.message);
            }

            ev.stopPropagation();
        }
    });
}

// ---------- RENDER BUILD LIST ----------
async function renderBuildsInline() {
    const ul = document.getElementById('savedBuildsInline');
    const sel1 = document.getElementById('compareSelect1Inline');
    const sel2 = document.getElementById('compareSelect2Inline');
    if (!ul || !sel1 || !sel2) return;

    ul.innerHTML = '';
    sel1.innerHTML = '';
    sel2.innerHTML = '';

    const username = currentUser?.username || 'guest';
    let buildIds = [];
    try {
        const res = await authFetch(`/api/login/${username}/builds`);
        if (!res.ok) throw new Error('Error fetching builds ID');
        buildIds = await res.json(); // array di ID
    } catch (err) {
        console.error(err);
    }

    if (buildIds.length === 0) {
        ul.innerHTML = '<li class="empty">No saved builds</li>';
        return;
    }

    for (const id of buildIds) {
        let fullBuild = null;
        try {
            const res = await authFetch(`/api/login/build/${id}`);
            if (!res.ok) throw new Error(`Error fetching build ${id}`);
            fullBuild = await res.json();
            loadedBuilds[fullBuild.id] = fullBuild;
        } catch (err) {
            console.error(err);
            continue;
        }

        const li = document.createElement('li');
        li.dataset.id = fullBuild.id;
        li.classList.add('build-item');

        // sezione principale
        const header = document.createElement('div');
        header.classList.add('build-header');

        const idSpan = document.createElement('span');
        idSpan.textContent = `💻 Build ID: ${fullBuild.id}`;
        header.appendChild(idSpan);

        // icone
        const copy = document.createElement('i');
        copy.classList.add('ph', 'ph-copy', 'copy-icon');
        copy.title = 'Copy build';

        const del = document.createElement('i');
        del.classList.add('ph', 'ph-trash', 'delete-icon');
        del.title = 'Delete build';

        const iconsDiv = document.createElement('div');
        iconsDiv.appendChild(copy);
        iconsDiv.appendChild(del);
        header.appendChild(iconsDiv);

        // sezione dettagli (nascosta di default)
        const details = document.createElement('div');
        details.classList.add('build-details');
        details.innerHTML = `🧩 Components:<br>${fullBuild.components
            .map(c => {
                let displayType = c.type;
                if (displayType.toUpperCase().startsWith("DDR")) displayType = "RAM"; // tutte le RAM
                if (displayType.toUpperCase().includes("COOLER")) displayType = "COOLER"; // tutti i cooler
                return `• ${displayType}: ${c.model}`;
            })
            .join(';<br>')}`;

        li.appendChild(header);
        li.appendChild(details);
        ul.appendChild(li);

        // opzioni menu confronto
        const o1 = document.createElement('option');
        o1.value = fullBuild.id;
        o1.innerText = `💻 Build ID: ${fullBuild.id}`;
        sel1.appendChild(o1);

        const o2 = document.createElement('option');
        o2.value = fullBuild.id;
        o2.innerText = `💻 Build ID: ${fullBuild.id}`;
        sel2.appendChild(o2);
    }
}

// ---------- INIT: FORUM ----------
async function initForum() {
    const postsList = document.getElementById('postsListInline');
    const postForm = document.getElementById('postFormInline');

    // === CARICA POST DAL BACKEND ===
    let posts = [];
    try {
        const res = await authFetch(`/api/login/posts`);
        if (res.ok) {
            const data = await res.json();
            posts = await Promise.all(
                data.map(async p => {
                    const [likes, comments] = await Promise.all([
                        fetchLikes('POST', p.id),
                        fetchComments('POST', p.id)
                    ]);

                    return {
                        id: p.id,
                        title: p.title,
                        body: p.content,
                        author: p.authorId,
                        likes: likes.length,
                        likedBy: likes.map(l => l.userId),
                        comments: comments.map(c => ({
                            id: c.id,
                            author: c.authorId,
                            text: c.content,
                            at: c.createdAt
                        })),
                        createdAt: p.createdAt || Date.now()
                    };
                })
            );
            save('posts', posts);
        }
    } catch (err) {
        console.error("Errore caricamento post:", err);
        posts = load('posts') || [];
    }

    renderPosts();

    // === CREAZIONE NUOVO POST ===
    if (postForm) {
        postForm.onsubmit = async (e) => {
            e.preventDefault();
            const title = document.getElementById('postTitleInline')?.value?.trim();
            const content = document.getElementById('postBodyInline')?.value?.trim();
            if (!title || !content) {
                alert('Title and text required');
                return;
            }

            const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
            const authorId = token ? getUsernameFromToken(token) : 'guest';
            const newPost = { authorId, title, content };

            try {
                const res = await authFetch(`/api/login/posts`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(newPost)
                });

                if (!res.ok) throw new Error(await res.text() || "Errore creazione post");

                const savedPost = await res.json();
                posts.unshift({
                    id: savedPost.id || Date.now(),
                    title: savedPost.title,
                    body: savedPost.content,
                    author: savedPost.authorId,
                    likes: 0,
                    likedBy: [],
                    comments: [],
                    createdAt: Date.now()
                });

                save('posts', posts);
                renderPosts();
                postForm.reset();
            } catch (err) {
                console.error("Errore creazione post:", err);
                alert("Errore creazione post: " + err.message);
            }
        };
    }

    // === GESTIONE LIKE / COMMENTI ===
    postsList?.addEventListener('click', async (ev) => {
        const likeBtn = ev.target.closest('.like-btn');
        const commentBtn = ev.target.closest('.comment-btn');
        const postContainer = ev.target.closest('.post-item');

        // === LIKE ===
        if (likeBtn) {
            const id = likeBtn.dataset.id;
            const post = posts.find(p => p.id === id);
            if (!post) return;

            const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
            const userId = token ? getUsernameFromToken(token) : 'guest';
            const alreadyLiked = post.likedBy.includes(userId);

            try {
                if (alreadyLiked) {
                    const likes = await fetchLikes('POST', id);
                    const myLike = likes.find(l => l.userId === userId);
                    if (myLike) await deleteLike(myLike.id);
                    post.likedBy = post.likedBy.filter(u => u !== userId);
                    post.likes--;
                } else {
                    await createLike(userId, 'POST', id);
                    post.likedBy.push(userId);
                    post.likes++;
                }
                save('posts', posts);
                renderPosts();
            } catch (err) {
                console.error("Error fetching likes:", err);
            }
            return;
        }

        // === MOSTRA / NASCONDI COMMENTI ===
        if (commentBtn) {
            const commentsSection = postContainer.querySelector('.comments-section');
            commentsSection.classList.toggle('visible');
        }

        // === AGGIUNGI COMMENTO ===
        if (ev.target.classList.contains('submit-comment')) {
            const id = ev.target.dataset.id;
            const post = posts.find(p => p.id === id);
            const input = postContainer.querySelector('.comment-input');
            const text = input.value.trim();
            if (!text) return;

            const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
            const authorId = token ? getUsernameFromToken(token) : 'guest';
            const comment = {
                authorId,
                targetType: 'POST',
                targetId: id,
                content: text,
                createdAt: new Date().toISOString()
            };

            try {
                const res = await authFetch(`/api/login/comments`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(comment)
                });
                if (!res.ok) throw new Error("Error creating comment");

                const savedComment = await res.json();
                post.comments.push({
                    id: savedComment.id,
                    author: savedComment.authorId,
                    text: savedComment.content,
                    at: savedComment.createdAt
                });

                save('posts', posts);
                renderPosts();
                const newPost = document.querySelector(`.comment-btn[data-id="${id}"]`)?.closest('.post-item');
                newPost?.querySelector('.comments-section')?.classList.add('visible');
            } catch (err) {
                console.error("Error creating comment:", err);
                alert("Error creating comment: " + err.message);
            }
        }
    });

    // === FUNZIONI BACKEND ===
    async function fetchLikes(targetType, targetId) {
        try {
            const res = await authFetch(`/api/login/likes/${targetType}/${targetId}`);
            if (res.ok) return await res.json();
        } catch (err) {
            console.warn("Error fetching Likes:", err);
        }
        return [];
    }

    async function createLike(userId, targetType, targetId) {
        const like = { userId, targetType, targetId, createdAt: new Date().toISOString() };
        const res = await authFetch(`/api/login/likes`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(like)
        });
        if (!res.ok) throw new Error("Error creating likes");
        return await res.json();
    }

    async function deleteLike(id) {
        const res = await authFetch(`/api/login/likes/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error("Error deleting likes");
    }

    async function fetchComments(targetType, targetId) {
        try {
            const res = await authFetch(`/api/login/comments/${targetType}/${targetId}`);
            if (res.ok) return await res.json();
        } catch (err) {
            console.warn("Error fetching Comments:", err);
        }
        return [];
    }

    // === RENDER POST ===
    function renderPosts() {
        if (!postsList) return;
        postsList.innerHTML = '';

        posts.forEach(p => {
            const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
            const userId = token ? getUsernameFromToken(token) : 'guest';
            const liked = p.likedBy.includes(userId);

            const commentsHTML = p.comments.length
                ? p.comments.map(c => `
                    <div class="comment">
                        <strong>${c.author}</strong>
                        <span style="opacity:0.8; font-size:0.8rem;">${new Date(c.at).toLocaleString()}</span>
                        <div>${c.text}</div>
                    </div>
                  `).join('')
                : `<div class="no-comments">No comments yet</div>`;

            const li = document.createElement('li');
            li.classList.add('post-item');
            li.innerHTML = `
                <strong>${p.title}</strong>
                <div style="font-size:0.9rem;color:#ccc;margin-top:4px;white-space:pre-wrap;">${p.body}</div>
                <div style="margin-top:8px;display:flex;gap:8px;align-items:center">
                    <button class="like-btn icon-btn" data-id="${p.id}" title="Like">
                        <i class="${liked ? 'ph-fill' : 'ph'} ph-heart" style="${liked ? 'color: #ff6666' : 'color: white'}"></i>
                        <span>${p.likes}</span>
                    </button>
                    <button class="comment-btn icon-btn" data-id="${p.id}" title="Comments">
                        <i class="ph ph-chat-circle"></i>
                        <span>${p.comments.length}</span>
                    </button>
                    <span style="opacity:0.7;font-size:0.85rem;margin-left:auto">${p.author}</span>
                </div>

                <div class="comments-section">
                    <div class="comments-list">${commentsHTML}</div>
                    <div class="comment-input-row">
                        <input type="text" class="comment-input" placeholder="Write a comment...">
                        <button class="submit-comment icon-btn" data-id="${p.id}">
                            <i class="ph ph-paper-plane-tilt"></i>
                        </button>
                    </div>
                </div>
            `;
            postsList.appendChild(li);
        });
    }
}

// ---------- INIT: CHAT ----------
async function initChat() {
    let allTechnicians = [];
    const chatList = document.getElementById('chatList');
    const chatContainer = document.getElementById('chatContainer');
    const chatBackBtn = document.getElementById('chatBackBtn');
    const win = document.getElementById('chatWindowInline');
    const input = document.getElementById('chatInputInline');
    const send = document.getElementById('chatSendInline');

    if (!chatList || !win || !input || !send || !chatBackBtn) return;

    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    if (!token) return;

    const currentUserId = getUsernameFromToken(token);
    const roles = getRolesFromToken(token) || [];
    const isTechnician = roles.includes('TECHNICIAN');

    let refreshInterval;
    let chatRefreshInterval;
    let renderedMessages = [];
    let currentOpenConversationId = null;

    chatBackBtn.addEventListener('click', () => {
        chatContainer.style.display = 'none';
        chatList.style.display = 'block';
        win.innerHTML = '';
        input.value = '';
        renderedMessages = [];
        currentOpenConversationId = null;
        if (refreshInterval) clearInterval(refreshInterval);
    });

    await loadChatList();

    async function loadChatList() {
        chatList.innerHTML = `<div>Loading...</div>`;
        chatContainer.style.display = 'none';

        try {
            let list = [];
            let technicianProfiles = [];

            if (isTechnician) {
                const res = await authFetch(`/api/login/conversations/${encodeURIComponent(currentUserId)}`);
                if (!res.ok) throw new Error("Error loading conversations");
                list = await res.json();
            } else {
                const allTechsRes = await authFetch(`/api/login/technicians`);
                if (allTechsRes.ok) technicianProfiles = await allTechsRes.json();

                const techniciansRes = await authFetch(`/api/login/users/role/TECHNICIAN`);
                if (!techniciansRes.ok) throw new Error("Error loading technicians");
                const technicians = await techniciansRes.json();

                const convRes = await authFetch(`/api/login/conversations/${currentUserId}`);
                if (!convRes.ok) throw new Error("Error loading conversations");
                const conversations = await convRes.json();

                list = technicians.map(t => {
                    const conv = conversations.find(c =>
                        Array.isArray(c.participants) &&
                        c.participants.includes(t.username) &&
                        c.participants.includes(currentUserId)
                    );
                    const techProfile = technicianProfiles.find(tp => tp.userId === t.username);

                    if (conv) {
                        if (techProfile?.photoUrl) conv.profilePic = techProfile.photoUrl;
                        conv.isConversation = true;
                        return conv;
                    } else {
                        if (techProfile?.photoUrl) t.profilePic = techProfile.photoUrl;
                        t.conversationId = null;
                        t.isConversation = false;
                        return t;
                    }
                });
            }

            chatList.innerHTML = '';
            if (!list.length) {
                chatList.innerHTML = `<div style="font-style: italic;">No chat available.</div>`;
                return;
            }

            list.forEach(item =>{
                createChatItem(item);

                if (!item.isConversation) {
                    (async () => {
                        if (!allTechnicians.length) await loadTechnicians();
                        attachTechHover(chatItem, item);
                    })();
                }
            });

            if (chatRefreshInterval) clearInterval(chatRefreshInterval);
            chatRefreshInterval = setInterval(updateUnreadIndicators, 3000);
            await updateUnreadIndicators();

        } catch (err) {
            console.error(err);
            chatList.innerHTML = `<div style="opacity:0.7;">⚠️ Error loading chat list.</div>`;
        }
    }

    function createChatItem(item) {
        const chatItem = document.createElement('div');
        chatItem.className = 'chatItem';

        const profileImg = document.createElement('img');
        profileImg.src = item.profilePic || '../img/default-avatar.jpg';
        profileImg.className = 'chatProfilePic';

        const chatContent = document.createElement('div');
        chatContent.className = 'chatContent';

        const chatInfo = document.createElement('div');
        chatInfo.className = 'chatInfo';

        let displayName, targetId, conversationId;
        if (isTechnician) {
            // 👨‍🔧 lato tecnico → mostra solo utenti (chat già avviate)
            displayName = item.participants.find(p => p !== currentUserId) || "Utente";
            targetId = displayName;
            conversationId = item.id;
        } else {
            // 👤 lato utente → può avere chat già avviate o lista tecnici
            if (item.participants) {
                // Chat già avviata
                displayName = item.participants.find(p => p !== currentUserId);
                targetId = displayName;
                conversationId = item.id;
            } else {
                // Tecnico non ancora contattato
                displayName = item.username;
                targetId = item.username;
                conversationId = item.conversationId || null;
            }
        }

        chatInfo.innerHTML = `
        <div class="chatName">${displayName}</div>
        <div class="chatLastMsg" style="font-size:0.8em; color:#aaa; margin-top:2px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;"></div>
    `;

        const chatDetails = document.createElement('div');
        chatDetails.className = 'chatDetails';

        chatContent.appendChild(chatInfo);
        chatContent.appendChild(chatDetails);
        chatItem.appendChild(profileImg);
        chatItem.appendChild(chatContent);
        chatList.appendChild(chatItem);
        chatItem.dataset.id = conversationId || '';

        // 🔹 Mostra ultimo messaggio se esiste
        if (conversationId) updateLastMessage(conversationId, chatItem);

        // 🔹 Click per aprire chat
        chatItem.addEventListener('click', async () => {
            try {
                renderedMessages = [];
                currentOpenConversationId = conversationId;
                window.currentOpenConversationId = conversationId;
                chatItem.classList.remove('unread');

                // Disattiva eventuali hover
                chatItem.onmouseenter = null;
                chatItem.onmouseleave = null;

                await openChat(conversationId, targetId, chatItem);
            } catch (err) {
                console.error("Error opening chat", err);
            }
        });

        // 🔹 SOLO lato utente → aggiungi hover se NON è una conversazione già avviata
        if (!isTechnician && !conversationId && !chatItem.dataset.hoverAttached) {
            (async () => {
                if (!allTechnicians.length) await loadTechnicians();
                attachTechHover(chatItem, item);
                chatItem.dataset.hoverAttached = "true"; // Evita duplicati
            })();
        }
    }

    async function updateLastMessage(conversationId, chatItem) {
        try {
            const res = await authFetch(`/api/login/messages/${conversationId}`);
            if (!res.ok) return;
            const messages = await res.json();
            if (!messages.length) return;

            const lastMsg = messages[messages.length - 1];
            const lastMsgDiv = chatItem.querySelector('.chatLastMsg');
            if (lastMsgDiv) {
                lastMsgDiv.innerText = lastMsg.content.length > 50 ? lastMsg.content.slice(0, 50) + '…' : lastMsg.content;
            }
            chatItem.dataset.lastMessage = lastMsg.content;

        } catch (err) {
            console.warn("Error fetching last message for conversation", conversationId, err);
        }
    }

    async function updateUnreadIndicators() {
        if (!chatList.children.length) return;

        const items = Array.from(chatList.children);
        for (let chatItem of items) {
            const conversationId = chatItem.dataset.id;
            if (!conversationId) continue;
            if (conversationId === currentOpenConversationId) continue;

            try {
                const resUnread = await authFetch(`/api/login/conversation/${conversationId}/unread/${currentUserId}`);
                if (!resUnread.ok) continue;
                const dataUnread = await resUnread.json();
                const hasUnread = dataUnread?.unreadCount > 0;
                chatItem.classList.toggle('unread', hasUnread);

                await updateLastMessage(conversationId, chatItem);

            } catch (err) {
                console.warn("Error updating unread/last message for conversation", conversationId, err);
            }
        }
    }

    async function openChat(conversationId, targetId, chatItem) {
        chatList.style.display = 'none';
        chatContainer.style.display = 'block';
        win.innerHTML = `<div style="opacity:0.6;">Loading conversation...</div>`;

        if (refreshInterval) clearInterval(refreshInterval);

        send.onclick = async (e) => {
            e.preventDefault();
            await onSend(conversationId, targetId, chatItem);
        };

        if (!conversationId) {
            try {
                const convRes = await authFetch(`/api/login/conversations/${currentUserId}`);
                if (convRes.ok) {
                    const convList = await convRes.json();
                    const existingConv = convList.find(c =>
                        Array.isArray(c.participants) &&
                        c.participants.includes(targetId) &&
                        c.participants.includes(currentUserId)
                    );
                    if (existingConv) {
                        conversationId = existingConv.id;
                        chatItem.dataset.id = conversationId;
                        currentOpenConversationId = conversationId;
                        window.currentOpenConversationId = conversationId;
                    }
                }
            } catch (err) {
                console.warn("Could not fetch existing conversation", err);
            }
        }

        if (conversationId) {
            await loadChatHistory(conversationId);
            await markChatAsRead(conversationId);
        } else {
            win.innerHTML = '';
        }

        refreshInterval = setInterval(async () => {
            if (conversationId) {
                await loadChatHistory(conversationId, true);
                await markChatAsRead(conversationId);
            }
        }, 3000);
    }

    async function onSend(conversationId, targetId, chatItem) {
        const text = input.value.trim();
        if (!text) return;
        input.value = '';

        try {
            if (!conversationId) {
                const payload = { participants: [currentUserId, targetId] };
                const createRes = await authFetch(`/api/login/conversation`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                if (!createRes.ok) throw new Error("Error creating conversation");
                const newConversation = await createRes.json();
                conversationId = newConversation.id;
                chatItem.dataset.id = conversationId;
                currentOpenConversationId = conversationId;
            }

            const payload = { conversationId, fromUserId: currentUserId, content: text };
            const res = await authFetch(`/api/login/message`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error('Error sending message');

            await loadChatHistory(conversationId, true);
            if (chatItem) await updateLastMessage(conversationId, chatItem);

        } catch (err) {
            console.error(err);
            appendMsg("⚠️ Error sending message.", false);
        }
    }

    async function markChatAsRead(conversationId) {
        try {
            await authFetch(`/api/login/conversation/${conversationId}/read/${currentUserId}`, { method: 'PUT' });
        } catch (e) { console.warn(e); }
    }

    async function loadChatHistory(conversationId, preserveScroll = false) {
        try {
            const res = await authFetch(`/api/login/messages/${conversationId}`);
            if (!res.ok) throw new Error();
            const messages = await res.json();
            messages.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

            const newMessages = messages.filter(m => !renderedMessages.some(rm => rm.id === m.id));
            if (!preserveScroll) win.innerHTML = '';

            newMessages.forEach(m => {
                const mine = m.fromUserId === currentUserId;
                const time = m.timestamp ? new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '';
                appendMsg(m.content, mine, time);
            });

            renderedMessages.push(...newMessages);

            if (newMessages.length) win.scrollTop = win.scrollHeight;

        } catch (err) {
            console.error(err);
            if (!preserveScroll) win.innerHTML = `<div style="opacity:0.7;">⚠️ Error loading messages.</div>`;
        }
    }

    function appendMsg(text, right, timeStr) {
        const msgWrapper = document.createElement('div');
        msgWrapper.style.display = 'flex';
        msgWrapper.style.flexDirection = 'column';
        msgWrapper.style.alignItems = right ? 'flex-end' : 'flex-start';
        msgWrapper.style.margin = '6px 0';

        const bubble = document.createElement('div');
        bubble.style.display = 'inline-block';
        bubble.style.padding = '8px 12px 16px';
        bubble.style.borderRadius = '10px';
        bubble.style.maxWidth = '75%';
        bubble.style.wordBreak = 'break-word';
        bubble.style.whiteSpace = 'pre-wrap';
        bubble.style.background = right ? 'linear-gradient(90deg,#00c6ff,#0072ff)' : 'rgba(255,255,255,0.08)';
        bubble.style.color = '#fff';
        bubble.style.position = 'relative';

        const msgText = document.createElement('div');
        msgText.innerText = text;

        const time = document.createElement('span');
        time.innerText = timeStr || '';
        time.style.position = 'absolute';
        time.style.bottom = '4px';
        time.style.right = '8px';
        time.style.fontSize = '0.7rem';
        time.style.opacity = '0.7';

        bubble.appendChild(msgText);
        if (timeStr) bubble.appendChild(time);
        msgWrapper.appendChild(bubble);
        win.appendChild(msgWrapper);
    }

    async function authFetch(url, options = {}) {
        options.headers = {
            ...(options.headers || {}),
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        };
        return fetch(url, options);
    }

    // Mostra o nascondi il bottone "Termina Conversazione" SOLO per utenti non tecnici
    const delBtn = document.getElementById('chatDelBtn');
    if (delBtn) {
        delBtn.style.display = isTechnician ? 'none' : 'inline-block';
        delBtn.onclick = async () => {
            if (!currentOpenConversationId) return alert("No open conversation to delete.");

            // Mostra popup valutazione
            const rating = await showStarRating();
            if (rating === null) return; // L'utente ha annullato

            let token2;
            try {
                // 1️⃣ Recupera i partecipanti della conversazione dal gateway
                const resParticipants = await authFetch(`/api/login/conversation/${currentOpenConversationId}/participants`);
                if (!resParticipants.ok) throw new Error("Error loading participants");
                const participants = await resParticipants.json(); // ["simo", "fera"]

                token2 = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');

                // 2️⃣ Trova l'altro partecipante (il tecnico)
                const currentUsername = getUsernameFromToken(token2); // funzione che decodifica il token
                const technicianUsername = participants.find(u => u !== currentUsername);
                if (!technicianUsername) throw new Error("No technician found");

                // 3️⃣ Assegna punti al tecnico (1-5 punti in base alle stelle)
                const resPoints = await authFetch(`/api/login/technicians/${technicianUsername}/rating/add?amount=${rating}&reason=rating`, {
                    method: 'POST'
                });
                if (!resPoints.ok) console.warn("Error assigning points");

                // 4️⃣ Cancella conversazione
                const resDelete = await authFetch(`/api/login/conversation/${currentOpenConversationId}`, {
                    method: 'DELETE'
                });
                if (!resDelete.ok) throw new Error("Error deleting conversation");

                alert("Conversation terminated successfully!");
                chatBackBtn.click();
                await loadChatList();

            } catch (err) {
                console.error(err);
                alert("Error terminating conversation: " + err.message);
            }
        };
    }

    // Carica tutti i tecnici una volta
    async function loadTechnicians() {
        try {
            const res = await fetch('/api/login/technicians'); // GET per prendere tutti i profili
            if (!res.ok) throw new Error("Error loading technicians");
            allTechnicians = await res.json();
        } catch (err) {
            console.error(err);
        }
    }

    // Aggiunge l'espansione hover alla riga di un tecnico
    function attachTechHover(chatItem, technician) {
        const details = chatItem.querySelector('.chatDetails');
        if (!details) return;

        chatItem.addEventListener('mouseenter', () => {
            if (details.dataset.visible === "true") return;
            const tech = allTechnicians.find(t => t.userId === technician.username);
            if (!tech) return;

            // 🔹 Calcolo rating con stelle
            const rating = Math.max(0, Math.min(tech.pointsBalance || 0, 5));
            const fullStars = Math.floor(rating);
            const halfStar = rating % 1 >= 0.5 ? 1 : 0;
            const emptyStars = 5 - fullStars - halfStar;
            const starsHtml = '★'.repeat(fullStars) + (halfStar ? '½' : '') + '☆'.repeat(emptyStars);

            // 🔹 Inserisci i dettagli nel div già esistente
            details.innerHTML = `<div style="display:flex; flex-direction:column; align-items:flex-start; gap:4px;">
            <div style="margin:4px 0 -4px 0;"><b>Bio:</b> ${tech.bio || 'No bio available'}</div>
            <div><b>Rating:</b> ${starsHtml}</div>
            <div><b>Skills:</b> ${tech.skills?.length ? tech.skills.join(', ') : 'Zero Skills'}</div>
            <div><b>Certifications:</b> ${tech.certifications?.length ? tech.certifications.join(', ') : 'Zero Certifications'}</div>
            </div>
            `;
            chatItem.classList.add("expanded");
        });

        chatItem.addEventListener('mouseleave', () => {
            chatItem.classList.remove("expanded");
        });
    }

    function showStarRating() {
        return new Promise((resolve) => {
            // 🔹 Crea overlay trasparente
            const overlay = document.createElement('div');
            overlay.style.position = 'fixed';
            overlay.style.top = '0';
            overlay.style.left = '0';
            overlay.style.width = '100vw';
            overlay.style.height = '100vh';
            overlay.style.background = 'rgba(0, 0, 0, 0.5)';
            overlay.style.display = 'flex';
            overlay.style.alignItems = 'center';
            overlay.style.justifyContent = 'center';
            overlay.style.zIndex = '1000';

            // 🔹 Popup principale
            const popup = document.createElement('div');
            popup.style.background = '#222';
            popup.style.padding = '20px 20px 10px 20px';
            popup.style.borderRadius = '10px';
            popup.style.textAlign = 'center';
            popup.style.color = '#fff';
            popup.style.boxShadow = '0 0 15px rgba(0,0,0,0.3)';
            popup.innerHTML = `<div style="font-family: 'Orbitron', sans-serif; margin-bottom:10px; font-size:1em;">Review the techincian's help</div>`;

            // 🔹 Contenitore delle stelle
            const starsContainer = document.createElement('div');
            starsContainer.style.display = 'flex';
            starsContainer.style.justifyContent = 'center';
            starsContainer.style.gap = '8px';
            starsContainer.style.marginBottom = '5px';

            let selected = 0;

            // 🔹 Crea 5 stelle cliccabili
            for (let i = 1; i <= 5; i++) {
                const star = document.createElement('span');
                star.innerHTML = '☆';
                star.style.fontSize = '2rem';
                star.style.cursor = 'pointer';
                star.style.transition = 'transform 0.2s';

                star.addEventListener('mouseenter', () => {
                    updateStars(i);
                    star.style.transform = 'scale(1.2)';
                });

                star.addEventListener('mouseleave', () => {
                    updateStars(selected);
                    star.style.transform = 'scale(1)';
                });

                star.addEventListener('click', () => {
                    selected = i;
                    resolveSelection();
                });

                starsContainer.appendChild(star);
            }

            // 🔹 Aggiorna visuale stelle (piene/vuote)
            function updateStars(count) {
                Array.from(starsContainer.children).forEach((s, idx) => {
                    s.innerHTML = idx < count ? '★' : '☆'; });
            }

            // 🔹 Risolve la Promise e chiude popup
            function resolveSelection() {
                document.body.removeChild(overlay);
                resolve(selected);
            }

            popup.appendChild(starsContainer);

            // 🔹 Pulsante annulla
            const cancelBtn = document.createElement('button');
            cancelBtn.type = 'button';
            cancelBtn.id = 'undoBtn';
            cancelBtn.className = 'btn';
            cancelBtn.innerHTML = `<div><span>Undo</span></div>`;

            cancelBtn.addEventListener('click', () => {
                document.body.removeChild(overlay);
                resolve(null);
            });

            popup.appendChild(cancelBtn);
            overlay.appendChild(popup);
            document.body.appendChild(overlay);

        });
    }
}

// ---------- UTIL: load initial resources if any ----------
(function bootstrap(){
    // ensure state exists
    if(!Array.isArray(builds)) builds = [];
    if(!Array.isArray(posts)) posts = [];
})();