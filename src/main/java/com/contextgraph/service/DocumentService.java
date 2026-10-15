
package com.contextgraph.service;

import com.contextgraph.model.Document;
import com.contextgraph.model.User;
import com.contextgraph.repository.DocumentRepository;
import com.contextgraph.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository docs;
    private final UserRepository users;
    private final DocumentExtractionService extractor;
    private final MetadataService meta;
    private final Path storage;

    public DocumentService(
            DocumentRepository docs,
            UserRepository users,
            DocumentExtractionService extractor,
            MetadataService meta,
            @Value("${app.storage.local-dir}") String storageDirectory) {

        this.docs = docs;
        this.users = users;
        this.extractor = extractor;
        this.meta = meta;
        this.storage = Path.of(storageDirectory)
                .toAbsolutePath()
                .normalize();
    }

    public List<Document> list(String email) {
        User currentUser = user(email);

        return docs.findByOwnerIdOrderByCreatedAtDesc(
                currentUser.getId());
    }

    public Document upload(String email, MultipartFile file)
            throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "The uploaded file is empty.");
        }

        if (file.getSize() > 25L * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Maximum file size is 25 MB.");
        }

        User currentUser = user(email);

        byte[] fileBytes = file.getBytes();
        String checksum = sha(fileBytes);

        // Prevent the same user from uploading the same file twice.
        boolean duplicate = docs.existsByChecksumAndOwnerId(
                checksum, currentUser.getId());

        if (duplicate) {
            throw new IllegalArgumentException(
                    "This file is already in your ContextGraph.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed";
        }

        String storedName = UUID.randomUUID()
                + "-" + safe(originalName);

        Files.createDirectories(storage);

        Path destination = storage.resolve(storedName).normalize();

        if (!destination.startsWith(storage)) {
            throw new SecurityException("Invalid file path.");
        }

        try {
            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);

            String extractedText = extractor.extractText(file);

            if (extractedText == null) {
                extractedText = "";
            }

            Map<String, Object> metadata =
                    meta.analyze(extractedText, originalName);

            Document document = new Document();

            document.setOriginalName(originalName);
            document.setStoredName(storedName);
            document.setContentType(file.getContentType());
            document.setSizeBytes(file.getSize());
            document.setExtractedText(extractedText);

            document.setKeywords(
                    String.join(",", stringList(metadata.get("keywords"))));

            document.setEntities(
                    String.join(",", stringList(metadata.get("entities"))));

            document.setDates(
                    String.join(",", stringList(metadata.get("dates"))));

            Object topic = metadata.get("topic");

            document.setTopic(
                    topic == null ? "" : topic.toString());

            document.setChecksum(checksum);
            document.setOwner(currentUser);

            return docs.save(document);

        } catch (Exception ex) {
            Files.deleteIfExists(destination);
            throw ex;
        }
    }

    public Document get(String email, Long id) {
        User currentUser = user(email);

        return docs.findByIdAndOwnerId(id, currentUser.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Document not found."));
    }

    public void delete(String email, Long id) throws Exception {
        Document document = get(email, id);

        // Delete the database record first.
        docs.delete(document);

        Path filePath = storage.resolve(
                document.getStoredName()).normalize();

        if (!filePath.startsWith(storage)) {
            throw new SecurityException("Invalid file path.");
        }

        Files.deleteIfExists(filePath);
    }

    private User user(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User account not found."));
    }

    private List<String> stringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }

        return Collections.emptyList();
    }

    private String safe(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String sha(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(bytes));
    }
}
