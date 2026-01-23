package com.knrhenry.wordsearch;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

@ApplicationPath("/api")
@OpenAPIDefinition(
    info =
        @Info(
            title = "Word Search API",
            version = "1.0.0",
            description = "API for generating word search puzzles as text or PDF."))
public class WordSearchApplication extends Application {
  // No implementation needed
}
