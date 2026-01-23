package it.unisannio.authorization.persistence.mapper;

import it.unisannio.authorization.data.Roles;
import it.unisannio.authorization.data.User;
import it.unisannio.authorization.persistence.UserRepository;
import org.bson.Document;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.unisannio.authorization.persistence.UserRepository.BUILDS;

public class DocumentToUserMapper implements Function<Document, User> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public User apply(Document document) {
        // Mappatura dei ruoli
        List<String> roleStrings = document.getList("roles", String.class);
        Set<Roles> roles = roleStrings != null
                ? roleStrings.stream().map(Roles::valueOf).collect(Collectors.toSet())
                : Set.of(Roles.USER); // default

        // Mappatura della data di nascita
        String birthDateString = document.getString(UserRepository.BIRTHDATE);
        LocalDate birthDate = null;
        if (birthDateString != null && !birthDateString.isEmpty()) {
            birthDate = LocalDate.parse(birthDateString, FORMATTER);
        }

        // Mappatura degli ID delle build generate
        List<Long> buildIdsList = document.getList(BUILDS, Long.class);
        Set<Long> generatedBuilds = buildIdsList != null
                ? new HashSet<>(buildIdsList)
                : new HashSet<>();

        // Creazione dell'oggetto User completo
        return new User(
                document.getString(UserRepository.USERNAME),
                document.getString(UserRepository.NAME),
                document.getString(UserRepository.SURNAME),
                document.getString(UserRepository.EMAIL),
                document.getString(UserRepository.PASSWORD),
                roles,
                birthDate,
                generatedBuilds
        );
    }
}