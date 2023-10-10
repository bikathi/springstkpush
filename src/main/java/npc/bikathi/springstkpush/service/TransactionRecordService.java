package npc.bikathi.springstkpush.service;

import npc.bikathi.springstkpush.entity.TransactionRecord;
import npc.bikathi.springstkpush.repository.TransactionRecordRepository;
import npc.bikathi.springstkpush.state.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionRecordService {
    @Autowired
    TransactionRecordRepository transactionRecordRepository;

    public TransactionRecord insertTransaction(TransactionRecord transactionRecord) {
        return transactionRecordRepository.save(transactionRecord);
    }
}
