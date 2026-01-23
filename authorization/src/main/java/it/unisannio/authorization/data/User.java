package it.unisannio.authorization.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class User {

    private String username;
    private String name;
    private String surname;
    private String email;
    private String password;
    private Set<Roles> roles;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate birthDate;

    private Set<Long> generatedBuilds = new HashSet<>();

    public User() { }

    public User(String username, String name, String surname, String email, String password,
                Set<Roles> roles, LocalDate birthDate, Set<Long> generatedBuilds) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.birthDate = birthDate;
        this.generatedBuilds = generatedBuilds != null ? generatedBuilds : new HashSet<>();
    }

    // Costruttore con ruoli di default
    public User(String username, String name, String surname, String email, String password, LocalDate birthDate) {
        this(username, name, surname, email, password, Set.of(Roles.USER), birthDate, new HashSet<>());
    }

    public User(String username, String name, String surname, String email, String password,
                Set<Roles> roles, LocalDate birthDate) {
        this(username, name, surname, email, password, roles, birthDate, new HashSet<>());
    }

    // Getter e Setter per generatedBuilds
    public Set<Long> getGeneratedBuilds() {
        return generatedBuilds;
    }

    public void setGeneratedBuilds(Set<Long> generatedBuilds) {
        this.generatedBuilds = generatedBuilds;
    }

    // Altri getter e setter esistenti
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<Roles> getRoles() { return roles; }
    public void setRoles(Set<Roles> roles) { this.roles = roles; }
    public LocalDate getBirthDate() { return birthDate; }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", birthDate=" + birthDate +
                ", generatedBuilds=" + generatedBuilds +
                '}';
    }
}