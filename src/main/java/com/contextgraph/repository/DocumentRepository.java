
package com.contextgraph.repository;

import com.contextgraph.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    List<Document> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    boolean existsByChecksumAndOwnerId(
            String checksum, Long ownerId);

    Optional<Document> findByIdAndOwnerId(
            Long id, Long ownerId);
}
