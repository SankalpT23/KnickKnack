package com.sankalp.KnickKnack.repository;

import com.sankalp.KnickKnack.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends MongoRepository<Rating, String> {
    Optional<Rating> findByReservationId(String reservationId);

    List<Rating> findByBorrowerRatingRatedByNotNull();

    List<Rating> findByOwnerRatingRatedByNotNull();

    List<Rating> findByReservationIdIn(List<String> reservationIds);
}
