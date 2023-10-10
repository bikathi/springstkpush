package npc.bikathi.springstkpush.service;

import npc.bikathi.springstkpush.entity.TransactionRecord;
import npc.bikathi.springstkpush.repository.TransactionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TransactionRecordService {
    @Autowired
    TransactionRecordRepository transactionRecordRepository;

    public TransactionRecord insertTransaction(TransactionRecord transactionRecord) {
        return transactionRecordRepository.save(transactionRecord);
    }

    public Optional<TransactionRecord> retrieveExistingTransaction(Long transactionId) {
        return transactionRecordRepository.findById(transactionId);
    }
}
