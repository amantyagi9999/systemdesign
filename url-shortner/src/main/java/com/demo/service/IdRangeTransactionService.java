package com.demo.service;

import com.demo.entity.IdRangeCounter;
import com.demo.repository.IdRangeCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdRangeTransactionService {

    private final IdRangeCounterRepository counterRepository;

    @Transactional
    public IdRangeBlock allocateNewBlock(long blockSize) {
        IdRangeCounter counter = counterRepository.findIdForUpdate()
                .orElseGet(() -> counterRepository.save(new IdRangeCounter(1L, 1L)));

        long blockStart = counter.getNextAvailableId();
        long blockEnd = blockStart + blockSize - 1;
        counter.setNextAvailableId(blockEnd + 1);
        counterRepository.save(counter);

        return new IdRangeBlock(blockStart, blockEnd);
    }

    public record IdRangeBlock(long blockStart, long blockEnd) {
    }
}
