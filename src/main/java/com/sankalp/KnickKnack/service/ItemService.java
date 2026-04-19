package com.sankalp.KnickKnack.service;

import com.sankalp.KnickKnack.dto.request.CreateItemRequest;
import com.sankalp.KnickKnack.dto.request.UpdateItemRequest;
import com.sankalp.KnickKnack.dto.response.ItemResponse;
import com.sankalp.KnickKnack.exception.ResourceNotFoundException;
import com.sankalp.KnickKnack.exception.UnauthorizedException;
import com.sankalp.KnickKnack.exception.ValidationException;
import com.sankalp.KnickKnack.model.Item;
import com.sankalp.KnickKnack.model.User;
import com.sankalp.KnickKnack.model.enums.ItemAvailabilty;
import com.sankalp.KnickKnack.model.enums.ItemCategory;
import com.sankalp.KnickKnack.repository.ItemRepository;
import com.sankalp.KnickKnack.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ItemService {
        @Autowired
        private ItemRepository repository;

        @Autowired
        private UserRepository userRepository;

        public ItemResponse createItem(CreateItemRequest request, String ownerEmail) {
                log.info("Creating item for owner: {}", ownerEmail);
                Optional<User> byEmail = userRepository.findByEmail(ownerEmail);
                if (byEmail.isPresent()) {
                        User user = byEmail.get();
                        Item item = Item.builder()
                                        .ownerId(user.getId().toString())// JWT filter stores email in SECURITY CONTEXT
                                        .title(request.getTitle())
                                        .description(request.getDescription())
                                        .category(request.getCategory())
                                        .condition(request.getCondition())
                                        .images(request.getImages())
                                        .pickupLocation(request.getPickupLocation())
                                        .pickupInstructions(request.getPickupInstructions())
                                        .maxReservationHours(request.getMaxReservationHours())
                                        .maxCheckoutDays(request.getMaxCheckoutDays())

                                        .availability(ItemAvailabilty.AVAILABLE)
                                        .isDeleted(false)
                                        .qrCodeHash("QR_" + UUID.randomUUID())

                                        .build();
                        // Ensures The ItemResponse Contains the Actual Database id
                        Item save = repository.save(item);

                        return ItemResponse.builder()
                                        .id(save.getId())
                                        .title(save.getTitle())
                                        .description(save.getDescription())
                                        .category(save.getCategory())
                                        .condition(save.getCondition())
                                        .imageUrls(save.getImages())
                                        .availability(save.getAvailability())
                                        .owner(ItemResponse.OwnerSummary.builder()
                                                        .id(user.getId().toString())
                                                        .name(user.getName())
                                                        .trustScore(user.getTrustScore())
                                                        .totalTransactions(user.getTotalTransactions())
                                                        .build())
                                        .build();
                } else {
                        throw new ResourceNotFoundException("User not found");
                }
        }

        public Page<ItemResponse> getAvailableItems(Pageable pageable) {
                log.info("Fetching available items");
                return repository.findByAvailabilityAndIsDeletedFalse(ItemAvailabilty.AVAILABLE, pageable)
                                .map(item -> {
                                        User user = userRepository.findById(new ObjectId(item.getOwnerId()))
                                                        .orElse(null);

                                        return ItemResponse.builder()
                                                        .id(item.getId())
                                                        .title(item.getTitle())
                                                        .description(item.getDescription())
                                                        .category(item.getCategory())
                                                        .condition(item.getCondition())
                                                        .imageUrls(item.getImages())
                                                        .availability(item.getAvailability())
                                                        .owner(user != null ? ItemResponse.OwnerSummary.builder()
                                                                        .id(user.getId().toString())
                                                                        .name(user.getName())
                                                                        .trustScore(user.getTrustScore())
                                                                        .totalTransactions(user.getTotalTransactions())
                                                                        .build() : null)
                                                        .build();
                                });
        }

        public ItemResponse updateItem(String itemId, UpdateItemRequest request, String ownerEmail) {
                log.info("Updating item ID: {} for owner: {}", itemId, ownerEmail);
                Item item = repository.findById(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                User user = userRepository.findByEmail(ownerEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                if (!item.getOwnerId().equals(user.getId().toString())) {
                        throw new UnauthorizedException("You can only edit your own items");
                }

                // Cant Change Item Details While someone is borrowing it
                if (item.getAvailability() != ItemAvailabilty.AVAILABLE) {
                        throw new ValidationException("Cannot edit reserved/checked out items");
                }

                if (request.getPickupLocation() != null && !request.getPickupLocation().isEmpty()) {
                        item.setPickupLocation(request.getPickupLocation());
                }
                if (request.getTitle() != null && !request.getTitle().isEmpty()) {
                        item.setTitle(request.getTitle());
                }
                if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                        item.setDescription(request.getDescription());
                }
                if (request.getCondition() != null && !request.getCondition().isEmpty()) {
                        item.setCondition(request.getCondition());
                }
                if (request.getPickupInstructions() != null && !request.getPickupInstructions().isEmpty()) {
                        item.setPickupInstructions(request.getPickupInstructions());
                }

                Item updatedItem = repository.save(item);

                return ItemResponse.builder()
                                .id(updatedItem.getId())
                                .title(updatedItem.getTitle())
                                .description(updatedItem.getDescription())
                                .condition(updatedItem.getCondition())
                                .availability(updatedItem.getAvailability())
                                .owner(ItemResponse.OwnerSummary.builder()
                                                .id(user.getId().toString())
                                                .name(user.getName())
                                                .trustScore(user.getTrustScore())
                                                .build())
                                .build();
        }

        public ItemResponse deleteItem(String itemId, String currentUserEmail) {
                log.info("Deleting item ID: {} for user: {}", itemId, currentUserEmail);
                Item item = repository.findById(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                User currentUser = userRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!item.getOwnerId().equals(currentUser.getId().toString())) {
                        throw new UnauthorizedException("You can only Delete your own items");
                }
                if (item.getAvailability() != ItemAvailabilty.AVAILABLE) {
                        throw new ValidationException("Cannot delete reserved items");
                }

                item.setIsDeleted(true);
                item.setAvailability(ItemAvailabilty.UNAVAILABLE);
                Item save = repository.save(item);

                return ItemResponse.builder()
                                .id(save.getId())
                                .title(save.getTitle())
                                .description(save.getDescription())
                                .category(save.getCategory())
                                .condition(save.getCondition())
                                .availability(save.getAvailability())
                                .owner(ItemResponse.OwnerSummary.builder()
                                                .id(currentUser.getId().toString())
                                                .name(currentUser.getName())
                                                .trustScore(currentUser.getTrustScore())
                                                .build())
                                .build();
        }

        public ItemResponse getItemById(String itemId) {
                log.info("Fetching item by ID: {}", itemId);
                Item item = repository.findByIdAndIsDeletedFalse(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                User user = userRepository.findById(new ObjectId(item.getOwnerId()))
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                return ItemResponse.builder()
                                .id(item.getId())
                                .title(item.getTitle())
                                .description(item.getDescription())
                                .condition(item.getCondition())
                                .availability(item.getAvailability())
                                .owner(ItemResponse.OwnerSummary.builder()
                                                .id(user.getId().toString())
                                                .name(user.getName())
                                                .trustScore(user.getTrustScore())
                                                .build())
                                .build();
        }

        public List<ItemResponse> getMyListings(String ownerEmail) {
                log.info("Fetching listings for user: {}", ownerEmail);
                User user = userRepository.findByEmail(ownerEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                List<Item> userList = repository.findByOwnerIdAndIsDeletedFalse(user.getId().toString());

                return userList.stream().map(item -> ItemResponse.builder()
                                .id(item.getId())
                                .title(item.getTitle())
                                .description(item.getDescription())
                                .imageUrls(item.getImages())
                                .availability(item.getAvailability())
                                .condition(item.getCondition())
                                .owner(ItemResponse.OwnerSummary.builder()
                                                .id(user.getId().toString())
                                                .name(user.getName())
                                                .trustScore(user.getTrustScore())
                                                .build())
                                .build()).toList();
        }

        public Page<ItemResponse> searchItems(String keyword, Pageable pageable) {
                log.info("Searching items with keyword: '{}'", keyword);
                Page<Item> items = repository.searchByKeyword(keyword, pageable);
                return items.map(item -> {
                        User user = userRepository.findById(new ObjectId(item.getOwnerId())).orElse(null);
                        return ItemResponse.builder()
                                        .id(item.getId())
                                        .title(item.getTitle())
                                        .description(item.getDescription())
                                        .imageUrls(item.getImages())
                                        .availability(item.getAvailability())
                                        .condition(item.getCondition())
                                        .owner(user != null ? ItemResponse.OwnerSummary.builder()
                                                        .id(user.getId().toString())
                                                        .name(user.getName())
                                                        .trustScore(user.getTrustScore())
                                                        .build() : null)
                                        .build();
                });
        }
}
