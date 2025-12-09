package com.coveros.training.flavorhub.controller;

import com.coveros.training.flavorhub.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving the main web pages
 */
@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final RecipeService recipeService;
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recipes", recipeService.getAllRecipes());
        return "index";
    }
    
    /**
     * Display the recipes browsing page
     * TODO: Add model attributes for pre-populated data
     * @return the recipes view template
     */
    @GetMapping("/recipes")
    public String recipes() {
        return "recipes";
    }
}
