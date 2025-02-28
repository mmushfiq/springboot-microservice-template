package com.company.project.template.dao.repository;

import com.company.project.template.dao.document.PaymentLogDocument;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentLogRepository extends MongoRepository<PaymentLogDocument, String> {

    @Query("{ 'createdAt' : { $gte: ?0 } }")
    List<PaymentLogDocument> findByCreatedAtAfter(LocalDateTime createdAt);

}
