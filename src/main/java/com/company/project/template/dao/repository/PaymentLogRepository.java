package com.company.project.template.dao.repository;

import com.company.project.template.dao.document.PaymentLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentLogRepository extends MongoRepository<PaymentLogDocument, String> {

}
