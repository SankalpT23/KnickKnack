package com.sankalp.KnickKnack.controller;

import com.sankalp.KnickKnack.dto.request.CreateItemRequest;
import com.sankalp.KnickKnack.dto.request.UpdateItemRequest;
import com.sankalp.KnickKnack.dto.response.ItemResponse;
import com.sankalp.KnickKnack.repository.UserRepository;
import com.sankalp.KnickKnack.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/items")
// Adding new gear and browsing what's available
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("/search")
    public ResponseEntity<?> searchItem(String search, Pageable pageable) {
        log.info("Search requested with query: '{}'", search);
        if (search == null || search.trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Page<ItemResponse> itemResponses = itemService.searchItems(search, pageable);
        return new ResponseEntity<>(itemResponses, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@RequestBody @Valid CreateItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("User {} creating new item: {}", email, request.getTitle());
        ItemResponse item = itemService.createItem(request, email);

        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ItemResponse>> getAvailableItems(Pageable pageable) {
        Page<ItemResponse> availableItems = itemService.getAvailableItems(pageable);
        return new ResponseEntity<>(availableItems, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable String id) {
        ItemResponse itemById = itemService.getItemById(id);
        return new ResponseEntity<>(itemById, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable String id,
            @RequestBody @Valid UpdateItemRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("User {} updating item ID: {}", email, id);
        ItemResponse item = itemService.updateItem(id, request, email);
        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("User {} attempting to delete item ID: {}", email, id);
        itemService.deleteItem(id, email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/my-listings")
    public ResponseEntity<List<ItemResponse>> getMyListings(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<ItemResponse> myListings = itemService.getMyListings(email);
        return new ResponseEntity<>(myListings, HttpStatus.OK);
    }

}
