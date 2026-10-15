
package com.contextgraph.controller;

import com.contextgraph.model.Document;
import com.contextgraph.service.DocumentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // Return only safe document fields, never the owner or password hash.
    private Map<String, Object> toResponse(Document document) {
        return Map.of(
                "id", document.getId(),
                "originalName", document.getOriginalName() == null
                        ? "" : document.getOriginalName(),
                "storedName", document.getStoredName() == null
                        ? "" : document.getStoredName(),
                "contentType", document.getContentType() == null
                        ? "" : document.getContentType(),
                "sizeBytes", document.getSizeBytes() == null
                        ? 0L : document.getSizeBytes(),
                "keywords", document.getKeywords() == null
                        ? "" : document.getKeywords(),
                "entities", document.getEntities() == null
                        ? "" : document.getEntities(),
                "dates", document.getDates() == null
                        ? "" : document.getDates(),
                "topic", document.getTopic() == null
                        ? "" : document.getTopic(),
                "createdAt", document.getCreatedAt() == null
                        ? "" : document.getCreatedAt().toString()
        );
    }

    @GetMapping
    public List<Map<String, Object>> list(Authentication authentication) {
        return service.list(authentication.getName())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public Map<String, Object> upload(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        Document document =
                service.upload(authentication.getName(), file);

        return toResponse(document);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return toResponse(
                service.get(authentication.getName(), id)
        );
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(
            Authentication authentication,
            @PathVariable Long id
    ) throws Exception {
        service.delete(authentication.getName(), id);
        return Map.of("message", "Document deleted.");
    }
}
