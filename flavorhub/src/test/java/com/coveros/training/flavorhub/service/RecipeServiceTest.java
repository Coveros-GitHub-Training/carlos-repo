package com.coveros.training.flavorhub.service;

import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeService
 */
@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {
    
    @Mock
    private RecipeRepository recipeRepository;
    
    @InjectMocks
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
    void testAddRating_WhenFirstRating_ThenReturnsRecipeWithCorrectAverage() {
        // Arrange
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Recipe result = recipeService.addRating(1L, 5);
        
        // Assert
        assertNotNull(result);
        assertEquals(5.0, result.getAverageRating());
        assertEquals(1, result.getRatingCount());
        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeRepository, times(1)).save(testRecipe);
    }
    
    @Test
    void testAddRating_WhenMultipleRatings_ThenCalculatesCorrectAverage() {
        // Arrange
        testRecipe.setAverageRating(4.5);
        testRecipe.setRatingCount(4);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Recipe result = recipeService.addRating(1L, 3);
        
        // Assert
        assertNotNull(result);
        // Expected: ((4.5 * 4) + 3) / 5 = 4.2
        assertEquals(4.2, result.getAverageRating(), 0.001);
        assertEquals(5, result.getRatingCount());
        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeRepository, times(1)).save(testRecipe);
    }
    
    @Test
    void testAddRating_WhenRatingLessThan1_ThenThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> recipeService.addRating(1L, 0)
        );
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
        verify(recipeRepository, never()).findById(any());
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenRatingGreaterThan5_ThenThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> recipeService.addRating(1L, 6)
        );
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
        verify(recipeRepository, never()).findById(any());
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenRatingIsNull_ThenThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> recipeService.addRating(1L, null)
        );
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
        verify(recipeRepository, never()).findById(any());
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenRecipeIdIsNull_ThenThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> recipeService.addRating(null, 5)
        );
        assertEquals("Recipe ID must not be null", exception.getMessage());
        verify(recipeRepository, never()).findById(any());
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenRecipeNotFound_ThenThrowsIllegalArgumentException() {
        // Arrange
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> recipeService.addRating(999L, 5)
        );
        assertEquals("Recipe not found with id: 999", exception.getMessage());
        verify(recipeRepository, times(1)).findById(999L);
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenValidRatingBoundary_ThenReturnsRecipeWithCorrectAverage() {
        // Arrange
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act - Test minimum valid rating
        Recipe result1 = recipeService.addRating(1L, 1);
        
        // Assert
        assertEquals(1.0, result1.getAverageRating());
        assertEquals(1, result1.getRatingCount());
        
        // Act - Test maximum valid rating
        testRecipe.setAverageRating(1.0);
        testRecipe.setRatingCount(1);
        Recipe result5 = recipeService.addRating(1L, 5);
        
        // Assert
        // Expected: ((1.0 * 1) + 5) / 2 = 3.0
        assertEquals(3.0, result5.getAverageRating());
        assertEquals(2, result5.getRatingCount());
    }
}
