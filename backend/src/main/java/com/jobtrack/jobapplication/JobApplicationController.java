package com.jobtrack.jobapplication;

import com.jobtrack.auth.CurrentUserProvider;
import com.jobtrack.jobapplication.dto.JobApplicationRequest;
import com.jobtrack.jobapplication.dto.JobApplicationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Couche HTTP : traduit les requêtes en appels de service et choisit les codes de statut.
 * Aucune logique métier ni accès direct au repository ici.
 */
@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService service;
    private final CurrentUserProvider currentUser;

    public JobApplicationController(JobApplicationService service, CurrentUserProvider currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<JobApplicationResponse> findAll() {
        return service.findAll(currentUser.currentUserId());
    }

    @GetMapping("/{id}")
    public JobApplicationResponse findById(@PathVariable Long id) {
        return service.findById(currentUser.currentUserId(), id);
    }

    /** 201 Created + en-tête Location vers la nouvelle ressource, comme le prévoit HTTP. */
    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(@Valid @RequestBody JobApplicationRequest request) {
        JobApplicationResponse created = service.create(currentUser.currentUserId(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public JobApplicationResponse update(@PathVariable Long id, @Valid @RequestBody JobApplicationRequest request) {
        return service.update(currentUser.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(currentUser.currentUserId(), id);
    }
}
