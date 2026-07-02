package it.unisannio.authorization.persistence.mapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.Document;

import it.unisannio.authorization.data.User;
import it.unisannio.authorization.persistence.UserRepository;
import static it.unisannio.authorization.persistence.UserRepository.BUILDS;

public class UserToDocumentMapper implements Function<User, Document> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @SuppressWarnings("null")
    @Override
    public Document apply(User user) {
        // Mappatura dei ruoli
        List<String> roleStrings = user.getRoles() != null
                ? user.getRoles().stream().map(Enum::name).collect(Collectors.toList())
                : List.of("USER"); // default

        // Creazione del documento di base
        Document document = new Document(UserRepository.USERNAME, user.getUsername())
                .append(UserRepository.NAME, user.getName())
                .append(UserRepository.SURNAME, user.getSurname())
                .append(UserRepository.EMAIL, user.getEmail())
                .append(UserRepository.PASSWORD, user.getPassword())
                .append("roles", roleStrings);

        // Aggiunta della data di nascita se presente
        if (user.getBirthDate() != null) {
            document.append(UserRepository.BIRTHDATE, user.getBirthDate().format(FORMATTER));
        }

        // Aggiunta degli ID delle build generate
        Set<Long> buildIds = user.getGeneratedBuilds();
        if (buildIds != null && !buildIds.isEmpty()) {
            document.append(BUILDS, new ArrayList<>(buildIds));
        }

        return document;
    }
}