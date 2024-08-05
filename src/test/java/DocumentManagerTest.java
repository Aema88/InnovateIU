import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    public void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    public void testSaveDocument() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Test Title")
                .content("Test Content")
                .author(DocumentManager.Author.builder().id("1").name("Author").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId());
        assertEquals("Test Title", savedDocument.getTitle());
    }

    @Test
    public void testSaveDocumentNullThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            documentManager.save(null);
        });

        assertEquals("Document cannot be null", exception.getMessage());
    }

    @Test
    public void testFindById() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Test Title")
                .content("Test Content")
                .author(DocumentManager.Author.builder().id("1").name("Author").build())
                .created(Instant.now())
                .build();

        documentManager.save(document);
        Optional<DocumentManager.Document> foundDocument = documentManager.findById(document.getId());

        assertTrue(foundDocument.isPresent());
        assertEquals(document.getId(), foundDocument.get().getId());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<DocumentManager.Document> foundDocument = documentManager.findById("nonexistent-id");

        assertFalse(foundDocument.isPresent());
    }

    @Test
    public void testSearch() {
        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title One")
                .content("Content One")
                .author(DocumentManager.Author.builder().id("1").name("Author One").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title Two")
                .content("Content Two")
                .author(DocumentManager.Author.builder().id("2").name("Author Two").build())
                .created(Instant.now())
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Title"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(2, results.size());
    }

    @Test
    public void testSearchByDateRange() {
        Instant now = Instant.now();
        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title One")
                .content("Content One")
                .author(DocumentManager.Author.builder().id("1").name("Author One").build())
                .created(now.minusSeconds(60))
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title Two")
                .content("Content Two")
                .author(DocumentManager.Author.builder().id("2").name("Author Two").build())
                .created(now.plusSeconds(60))
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(now.minusSeconds(30))
                .createdTo(now.plusSeconds(30))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(0, results.size());
    }
}
