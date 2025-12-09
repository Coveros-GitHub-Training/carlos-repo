package com.coveros.training.flavorhub.controller;

import com.coveros.training.flavorhub.dto.RatingRequest;
import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RecipeController rating endpoint
 */
@WebMvcTest(RecipeController.class)
class RecipeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private RecipeService recipeService;
    
    private Recipe testRecipe;
    
    @BeforeEach
    void setUp() {
        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setName("Test Recipe");
        testRecipe.setAverageRating(0.0);
        testRecipe.setRatingCount(0);
    }
    
    @Test
    void testAddRating_WhenValidRating_ThenReturnsOkWithUpdatedRecipe() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(5);
        testRecipe.setAverageRating(5.0);
        testRecipe.setRatingCount(1);
        when(recipeService.addRating(eq(1L), eq(5))).thenReturn(testRecipe);
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/1/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Recipe"))
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
        
        verify(recipeService, times(1)).addRating(1L, 5);
    }
    
    @Test
    void testAddRating_WhenRatingLessThan1_ThenReturnsBadRequest() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(0);
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/1/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(recipeService, never()).addRating(any(), any());
    }
    
    @Test
    void testAddRating_WhenRatingGreaterThan5_ThenReturnsBadRequest() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(6);
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/1/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(recipeService, never()).addRating(any(), any());
    }
    
    @Test
    void testAddRating_WhenRatingIsNull_ThenReturnsBadRequest() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(null);
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/1/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(recipeService, never()).addRating(any(), any());
    }
    
    @Test
    void testAddRating_WhenRecipeNotFound_ThenReturnsBadRequest() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(5);
        when(recipeService.addRating(eq(999L), eq(5)))
                .thenThrow(new IllegalArgumentException("Recipe not found with id: 999"));
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/999/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(recipeService, times(1)).addRating(999L, 5);
    }
    
    @Test
    void testAddRating_WhenMultipleRatings_ThenUpdatesAverage() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(3);
        testRecipe.setAverageRating(4.2);
        testRecipe.setRatingCount(5);
        when(recipeService.addRating(eq(1L), eq(3))).thenReturn(testRecipe);
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/1/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.2))
                .andExpect(jsonPath("$.ratingCount").value(5));
        
        verify(recipeService, times(1)).addRating(1L, 3);
    }
}
