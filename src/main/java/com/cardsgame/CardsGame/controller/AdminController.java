package com.cardsgame.CardsGame.controller;

import com.cardsgame.CardsGame.dto.CreateCardRequest;
import com.cardsgame.CardsGame.entity.PackTemplate;
import com.cardsgame.CardsGame.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") //Controlla che l'utente abbia il ruolo di ADMIN per accedere a questo controller
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/pack-templates")
    public ResponseEntity<PackTemplate> createPackTemplate(@RequestBody PackTemplate packTemplate) {
        PackTemplate createdPackTemplate = adminService.createPackTemplate(packTemplate);
        return ResponseEntity.ok(createdPackTemplate);
    }

    @PutMapping("/pack-templates/{id}")
    public ResponseEntity<PackTemplate> updatePackTemplate(@PathVariable Long id, @RequestBody PackTemplate packTemplate) {
        PackTemplate updatedPackTemplate = adminService.updatePackTemplate(id, packTemplate);
        return ResponseEntity.ok(updatedPackTemplate);
    }

    @DeleteMapping("/pack-templates/{id}")
    public ResponseEntity<Void> deletePackTemplate(@PathVariable Long id) {
        adminService.deletePackTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cards")
    public ResponseEntity<Void> addCard(@RequestBody CreateCardRequest cardRequest) {
        adminService.addCard(cardRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> removeCard(@PathVariable Long id) {
        adminService.removeCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(adminService.getStatistics());
    }
}
