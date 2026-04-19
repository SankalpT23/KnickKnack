package com.sankalp.KnickKnack.repository;

import com.sankalp.KnickKnack.model.Item;
import com.sankalp.KnickKnack.model.enums.ItemAvailabilty;
import com.sankalp.KnickKnack.model.enums.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends MongoRepository<Item,String> {
    //Find all Available items for the main feed -> Pagination
    Page<Item> findByAvailabilityAndIsDeletedFalse(ItemAvailabilty state, Pageable pageable);
    //Filter by Category
    Page<Item> findByCategoryAndAvailabilityAndIsDeletedFalse(ItemCategory category, ItemAvailabilty state, Pageable pageable);
    //Find items by Owner
    List<Item> findByOwnerIdAndIsDeletedFalse(String ownerId);
    //Find specific Item
    Optional<Item> findByIdAndIsDeletedFalse(String id);

    @Query("{ '$or': [ { 'title': { '$regex': ?0, '$options': 'i' } }, { 'description': { '$regex': ?0, '$options': 'i' } } ], 'isDeleted': false, 'availability': 'AVAILABLE' }")
    Page<Item> searchByKeyword(String keyword,Pageable pageable);

    //If Item Is Reserved Or Not
    boolean existsByIdAndAvailability(String id,ItemAvailabilty state);

//    // In ItemRepository, add this custom method
//    @Query("{ '_id': ?0, 'availability': 'AVAILABLE', 'reservationId': null }")
//    @Update("{ '$set': { 'availability': 'RESERVED' } }")
//    Item findAndReserveItem(String itemId);

    //We Might get 1000+ books Therefore, List Will eventually Lag The App -> "Page" Allows frontend to load Page 1,Page 2
    //IsDeletedFalse -> If Owner Deletes an Item it Disappears from app but stays in DB for logs
    //Always Use Optional For Nullable Results
}
