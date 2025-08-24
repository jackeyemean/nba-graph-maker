package com.nba.controller;

import com.nba.dto.GraphRequest;
import com.nba.dto.GraphResponse;
import com.nba.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = {"http://localhost:3000", "https://nba-graph-generator.onrender.com"})
public class GraphController {

    @Autowired
    private GraphService graphService;

    @PostMapping("/generate")
    public ResponseEntity<GraphResponse> generateGraph(@RequestBody GraphRequest request) {
        try {
            GraphResponse response = graphService.generateGraph(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GraphResponse());
        }
    }



    @GetMapping("/players")
    public ResponseEntity<List<String>> getPlayers(@RequestParam(required = false) String search) {
        try {
            List<String> players = graphService.searchPlayers(search);
            return ResponseEntity.ok(players);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teams")
    public ResponseEntity<List<String>> getTeams() {
        try {
            List<String> teams = graphService.getTeams();
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


}

