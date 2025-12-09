package com.coveros.training.flavorhub.service;

import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing recipes
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    private static final Logger log = LoggerFactory.getLogger(RecipeService.class);
    
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }
    
    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"));
    }
    
    public List<Recipe> getRecipesByDifficulty(String difficultyLevel) {
        return recipeRepository.findByDifficultyLevel(difficultyLevel);
    }
    
    public List<Recipe> getRecipesByCuisine(String cuisineType) {
        return recipeRepository.findByCuisineType(cuisineType);
    }
    
    public List<Recipe> searchRecipes(String searchTerm) {
        if (searchTerm == null) {
            return Collections.emptyList();
        }
        String trimmed = searchTerm.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        return recipeRepository
                .findByNameContainingIgnoreCaseOrCuisineTypeContainingIgnoreCaseOrDifficultyLevelContainingIgnoreCase(
                        trimmed,
                        trimmed,
                        trimmed
                );
    }

    /**
     * Determine the recipe of the day based on the system date.
     *
     * @return optional recipe determined for the current day
     */
    public Optional<Recipe> getRecipeOfTheDay() {
        return getRecipeOfTheDay(LocalDate.now());
    }

    /**
     * Determine the recipe of the day using a deterministic calculation tied to the provided date.
     *
     * @param date the date used to select the recipe of the day
     * @return optional recipe determined for the supplied day
     */
    public Optional<Recipe> getRecipeOfTheDay(LocalDate date) {
        if (date == null) {
            log.warn("Attempted to resolve recipe of the day with a null date");
            return Optional.empty();
        }

        List<Recipe> recipes = recipeRepository.findAll();
        if (recipes.isEmpty()) {
            log.warn("No recipes available when attempting to resolve recipe of the day");
            return Optional.empty();
        }

        recipes.sort(Comparator.comparing(Recipe::getId));
        int index = calculateRecipeIndex(date, recipes.size());
        Recipe selected = recipes.get(index);
        log.debug("Selected recipe '{}' (id={}) as recipe of the day for {}", selected.getName(), selected.getId(), date);
        return Optional.ofNullable(selected);
    }

    private int calculateRecipeIndex(LocalDate date, int recipeCount) {
        int seed = date.getYear() * 1000 + date.getDayOfYear();
        return Math.floorMod(seed, recipeCount);
    }
    
    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(java.util.Objects.requireNonNull(recipe, "recipe must not be null"));
    }
    
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(java.util.Objects.requireNonNull(id, "id must not be null"));
    }
    
    /**
     * Add a rating to a recipe and update its average rating
     * 
     * @param recipeId the ID of the recipe to rate
     * @param rating the rating value (1-5)
     * @return the updated recipe with new rating
     * @throws IllegalArgumentException if rating is not between 1-5 or recipeId is null
     */
    @Transactional
    public Recipe addRating(Long recipeId, Integer rating) {
        if (recipeId == null) {
            throw new IllegalArgumentException("Recipe ID must not be null");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        
        Double currentAverage = recipe.getAverageRating() != null ? recipe.getAverageRating() : 0.0;
        Integer currentCount = recipe.getRatingCount() != null ? recipe.getRatingCount() : 0;
        
        // Calculate new average: ((oldRating * oldCount) + newRating) / (oldCount + 1)
        Double newAverage = ((currentAverage * currentCount) + rating) / (currentCount + 1);
        Integer newCount = currentCount + 1;
        
        recipe.setAverageRating(newAverage);
        recipe.setRatingCount(newCount);
        
        return recipeRepository.save(recipe);
    }
    
    /**
     * Find recipes that can be made based on available ingredients in the pantry
     * NOTE: This method is intentionally left incomplete for workshop participants
     * Participants will use GitHub Copilot to implement this recommendation logic
     */
    // TODO: Implement method to recommend recipes based on pantry ingredients
    
    /**
     * Get recipes that match specific dietary requirements or filters
     * NOTE: This is a more advanced feature to be implemented during the workshop
     */
    // TODO: Implement advanced filtering logic
}
