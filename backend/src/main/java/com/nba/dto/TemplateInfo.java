package com.nba.dto;

import java.util.List;
import java.util.Map;

public class TemplateInfo {
    private String id;
    private String name;
    private String description;
    private String graphType;
    private Map<String, Object> defaultValues;
    private List<FieldInfo> fields;
    
    public static class FieldInfo {
        private String name;
        private String label;
        private String type; // "text", "number", "select", "multiselect", "checkbox"
        private String defaultValue;
        private List<String> options; // For select/multiselect
        private boolean required;
        private String description;
        
        public FieldInfo() {}
        
        public FieldInfo(String name, String label, String type, String defaultValue) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.defaultValue = defaultValue;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getLabel() {
            return label;
        }
        
        public void setLabel(String label) {
            this.label = label;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getDefaultValue() {
            return defaultValue;
        }
        
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
        
        public List<String> getOptions() {
            return options;
        }
        
        public void setOptions(List<String> options) {
            this.options = options;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public void setRequired(boolean required) {
            this.required = required;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
    
    public TemplateInfo() {}
    
    public TemplateInfo(String id, String name, String description, String graphType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.graphType = graphType;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getGraphType() {
        return graphType;
    }
    
    public void setGraphType(String graphType) {
        this.graphType = graphType;
    }
    
    public Map<String, Object> getDefaultValues() {
        return defaultValues;
    }
    
    public void setDefaultValues(Map<String, Object> defaultValues) {
        this.defaultValues = defaultValues;
    }
    
    public List<FieldInfo> getFields() {
        return fields;
    }
    
    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }
}

