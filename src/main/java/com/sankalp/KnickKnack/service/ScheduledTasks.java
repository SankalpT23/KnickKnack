package com.sankalp.KnickKnack.service;

import com.sankalp.KnickKnack.exception.ResourceNotFoundException;
import com.sankalp.KnickKnack.model.Item;
import com.sankalp.KnickKnack.model.Reservation;
import com.sankalp.KnickKnack.model.User;
import com.sankalp.KnickKnack.model.enums.ItemAvailabilty;
import com.sankalp.KnickKnack.model.enums.ReservationStatus;
import com.sankalp.KnickKnack.repository.ItemRepository;
import com.sankalp.KnickKnack.repository.ReservationRepository;
import com.sankalp.KnickKnack.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ScheduledTasks {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 */15 * * * *")
    public void expirePendingReservations() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Cron job started: Checking for expired reservations...");
        List<Reservation> reservations = reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.PENDING,
                now);
        if (reservations.isEmpty()) {
            log.info("No expired reservations found");
            return;
        }
        int count = 0;
        for (Reservation reservation : reservations) {
            try {
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                Item item = itemRepository.findById(reservation.getItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                item.setAvailability(ItemAvailabilty.AVAILABLE);
                item.setReservationId(null);
                itemRepository.save(item);

                User user = userRepository.findById(new ObjectId(reservation.getBorrowerId()))
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                user.setTrustScore(user.getTrustScore() - 3);
                userRepository.save(user);

                count++;
                log.info("Expired: Reservation {} (Item: {}, Borrower: {}, Trust score: {})",
                        reservation.getId(), item.getTitle(), user.getName(), user.getTrustScore());
            } catch (Exception e) {
                log.error("Error processing reservation {}: {}", reservation.getId(), e.getMessage());
            }
        }
        log.info("Expired reservations found: {}", count);
    }
}
