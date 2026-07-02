package it.unisannio.authorization.application;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import it.unisannio.authorization.data.Roles;
import it.unisannio.authorization.data.User;
import it.unisannio.authorization.persistence.UserRepository;
import it.unisannio.authorization.persistence.UserRepositoryMongo;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])" +          // almeno un numero
                    "(?=.*[a-z])" +           // almeno una lettera minuscola
                    "(?=.*[A-Z])" +           // almeno una lettera maiuscola
                    "(?=.*[@#$%^&+=!])" +     // almeno un carattere speciale
                    "(?=\\S+$)" +             // nessuno spazio bianco
                    ".{8,}$";                 // almeno 8 caratteri

    private final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserService() {
        this.userRepository = UserRepositoryMongo.getInstance();
    }

    // ✅ Nuova funzione di validazione della data di nascita
    private boolean isBirthDateValid(LocalDate birthDate) {
        if (birthDate == null) return true; // opzionale: se non obbligatoria, accetta null
        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) return false; // non può essere nel futuro
        int age = Period.between(birthDate, today).getYears();
        return age >= 13; // ad esempio, minimo 13 anni
    }

    public String createUser(User user) {
        if (userRepository.findUserOrNull(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exist: " + user.getUsername());
        }

        if (!isPasswordValid(user.getPassword())) {
            throw new IllegalArgumentException("Invalid password: must contain at least 8 characters, 1 uppercase letter, 1 lowercase letter, 1 number and 1 special character");
        }

        if (!isBirthDateValid(user.getBirthDate())) {
            throw new IllegalArgumentException("Invalid birth date: must be at least 13 years old");
        }

        user.setPassword(encoder.encode(user.getPassword()));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of(Roles.USER));
        }

        return userRepository.createUser(user).getUsername();
    }

    public List<User> getAllUsers() {
        return userRepository.findall();
    }

    public User getUser(String username) {
        return userRepository.findUser(username);
    }

    public User updateUser(String username, User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // Se la password NON è già cifrata, allora cifrala
            if (!user.getPassword().startsWith("$2a$") &&
                    !user.getPassword().startsWith("$2b$") &&
                    !user.getPassword().startsWith("$2y$")) {

                if (!isPasswordValid(user.getPassword())) {
                    throw new IllegalArgumentException("Invalid password: must contain at least 8 characters, 1 uppercase letter, 1 lowercase letter, 1 number and 1 special character");
                }

                user.setPassword(encoder.encode(user.getPassword()));
            }
        }


        if (!isBirthDateValid(user.getBirthDate())) {
            throw new IllegalArgumentException("Invalid birth date: must be at least 13 years old");
        }

        // Mantieni i ruoli esistenti se non vengono passati
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            User existing = userRepository.findUser(username);
            user.setRoles(existing.getRoles());
        }

        return userRepository.updateUser(username, user);
    }

    public List<User> findAllByRole(String role) {
        return userRepository.findAllByRole(role);
    }

    public boolean deleteUser(String username) {
        return userRepository.deleteUser(username);
    }

    public boolean authenticateUser(String username, String rawPassword) {
        User user = userRepository.findUser(username);
        if (user == null) return false;
        return encoder.matches(rawPassword, user.getPassword());
    }

    @SuppressWarnings("null")
    public List<String> getUserRolesAsString(String username) {
        User user = userRepository.findUser(username);
        return user.getRoles().stream().map(Enum::name).collect(Collectors.toList());
    }

    public boolean isPasswordValid(String password) {
        if (password == null) return false;
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    /**
     * Aggiunge l'ID di una build al set dell'utente.
     */
    public void addGeneratedBuild(String username, Long buildId) {
        if (!(userRepository instanceof UserRepositoryMongo mongoRepo)) {
            throw new UnsupportedOperationException("Operation supported only by UserRepositoryMongo");
        }
        mongoRepo.addGeneratedBuild(username, buildId);
    }

    /**
     * Rimuove l'ID di una build dal set dell'utente.
     */
    public void removeGeneratedBuild(String username, Long buildId) {
        if (!(userRepository instanceof UserRepositoryMongo mongoRepo)) {
            throw new UnsupportedOperationException("Operation supported only by UserRepositoryMongo");
        }
        mongoRepo.removeGeneratedBuild(username, buildId);
    }

    /**
     * Recupera tutte le build generate dall'utente.
     */
    public Set<Long> getGeneratedBuilds(String username) {
        User user = userRepository.findUser(username);
        return user.getGeneratedBuilds(); // Assumendo che User abbia un HashSet<Long> generatedBuilds
    }

}