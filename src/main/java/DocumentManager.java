import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> storage = new ConcurrentHashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }
        storage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(document -> matchesRequest(document, request))
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    private boolean matchesRequest(Document document, SearchRequest request) {
        if (request == null) return true;

        boolean matches = true;

        matches &= checkCollection(request.getTitlePrefixes(), prefix -> document.getTitle().startsWith(prefix));
        matches &= checkCollection(request.getContainsContents(), content -> document.getContent().contains(content));
        matches &= checkCollection(request.getAuthorIds(), authorId -> document.getAuthor().getId().equals(authorId));

        matches &= checkDateRange(document.getCreated(), request.getCreatedFrom(), request.getCreatedTo());

        return matches;
    }

    private <T> boolean checkCollection(Collection<T> collection, Predicate<T> predicate) {
        return collection == null || collection.isEmpty() || collection.stream().anyMatch(predicate);
    }
    private boolean checkDateRange(Instant created, Instant createdFrom, Instant createdTo) {
        boolean matches = true;

        if (createdFrom != null) {
            matches &= created.isAfter(createdFrom) || created.equals(createdFrom);
        }

        if (createdTo != null) {
            matches &= created.isBefore(createdTo) || created.equals(createdTo);
        }

        return matches;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
