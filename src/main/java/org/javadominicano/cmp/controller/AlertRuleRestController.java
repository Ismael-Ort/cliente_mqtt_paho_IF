package org.javadominicano.cmp.controller;

import org.javadominicano.cmp.DatabaseManager;
import org.javadominicano.cmp.dto.AlertRuleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlertRuleRestController {

    private final DatabaseManager dbManager;

    @Autowired
    public AlertRuleRestController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @GetMapping("/api/reglas-alerta")
    public List<AlertRuleDTO> listarReglas() {
        return dbManager.getAlertRuleDTOs();
    }

    @DeleteMapping("/api/reglas-alerta/{id}")
    public ResponseEntity<Void> eliminarRegla(@PathVariable int id) {
        dbManager.deleteAlertRule(id);
        return ResponseEntity.noContent().build();
    }
}
