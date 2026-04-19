package com.sankalp.KnickKnack.repository;

import com.sankalp.KnickKnack.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    //Find User By Email
    Optional<User> findByEmail(String email);
    //Check if the Email Exists
    boolean existsByEmail(String email);
    //Find By CampusId
    Optional<User> findByCampusId(String campusId);//Optional -> Prevents Null Pointer Exceptions

}
