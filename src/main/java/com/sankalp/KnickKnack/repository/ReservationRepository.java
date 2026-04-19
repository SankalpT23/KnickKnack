package com.sankalp.KnickKnack.repository;

import com.sankalp.KnickKnack.model.Reservation;
import com.sankalp.KnickKnack.model.enums.ReservationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {
    // Find pending reservations for an item
    Optional<Reservation> findByItemIdAndStatus(String itemId, ReservationStatus status);

    // Find user's reservations
    List<Reservation> findByBorrowerIdOrderByReservedAtDesc(String borrowerId);

    // Find owner's lent items
    List<Reservation> findByOwnerIdOrderByReservedAtDesc(String ownerId);

    // Find by QR nonce
    Optional<Reservation> findByCheckoutQrNonce(String nonce);

    Optional<Reservation> findByReturnQrNonce(String nonce);

    // Find expired reservations (for cron job)
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime expiryTime);

    // Count expired reservations in last 24h (for cooldown)
    int countByBorrowerIdAndStatusAndReservedAtAfter(
            String borrowerId,
            ReservationStatus status,
            LocalDateTime since);

}
