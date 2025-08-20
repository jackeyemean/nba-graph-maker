package com.nba.controller;

import com.nba.dto.GraphRequest;
import com.nba.dto.GraphResponse;
import com.nba.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GraphController {
    
    @Autowired
    private GraphService graphService;
    
    @PostMapping("/generate")
    public ResponseEntity<GraphResponse> generateGraph(@RequestBody GraphRequest request) {
        GraphResponse response = graphService.generateGraph(request);
        
        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}

