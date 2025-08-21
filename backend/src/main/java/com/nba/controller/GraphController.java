package com.nba.controller;

import com.nba.dto.GraphRequest;
import com.nba.dto.GraphResponse;
import com.nba.dto.TemplateInfo;
import com.nba.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "http://localhost:3000")
public class GraphController {

    @Autowired
    private GraphService graphService;

    @PostMapping("/generate")
    public ResponseEntity<GraphResponse> generateGraph(@RequestBody GraphRequest request) {
        try {
            System.out.println("Received request:");
            System.out.println("  graphType: " + request.getGraphType());
            System.out.println("  template: " + request.getTemplate());
            System.out.println("  players: " + request.getPlayers());
            System.out.println("  xAxisType: " + request.getXAxisType());
            System.out.println("  yAxisType: " + request.getYAxisType());
            System.out.println("  includeMultiTeamPlayers: " + request.getIncludeMultiTeamPlayers());
            
            GraphResponse response = graphService.generateGraph(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error in controller: " + e.getMessage());
            e.printStackTrace();
            GraphResponse errorResponse = new GraphResponse();
            errorResponse.setGraphType(request.getGraphType());
            errorResponse.setTitle("Error");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<List<TemplateInfo>> getTemplates() {
        try {
            List<TemplateInfo> templates = graphService.getAvailableTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/template/{templateId}")
    public ResponseEntity<TemplateInfo> getTemplate(@PathVariable String templateId) {
        try {
            TemplateInfo template = graphService.getTemplate(templateId);
            if (template != null) {
                return ResponseEntity.ok(template);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats/options")
    public ResponseEntity<List<String>> getAvailableStats() {
        try {
            List<String> stats = graphService.getAvailableStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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

    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getYears() {
        try {
            List<Integer> years = graphService.getYears();
            return ResponseEntity.ok(years);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

