package com.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements Approach A from the design discussion: range-based id allocation.
 *
 * Each app server instance claims a BLOCK of ids (default 1000) from the central
 * counter row in one DB round trip, then serves ids out of that block from
 * local memory - no DB call needed per short-code generation. When the local
 * block is exhausted, it claims the next block.
 *
 * This avoids both:
 *  - a single shared auto-increment counter becoming a bottleneck/SPOF, and
 *  - Snowflake-style complexity (machine ids, clock sync) which isn't
 *    justified at our write volume (~232 QPS peak).
 *
 * Gaps in the sequence (e.g. if a server crashes mid-block) are acceptable -
 * we only need uniqueness, not strict sequential order.
 */

@Service
@RequiredArgsConstructor
public class IdRangeAllocatorService {

    private static final long BLOCK_SIZE = 100L;
    private final IdRangeTransactionService idRangeTransactionService;
    private final AtomicLong currentId = new AtomicLong(-1);
    private final AtomicLong maxIdInBlock = new AtomicLong(-1);

    public synchronized long getNextId(){
        if(currentId.get() >= maxIdInBlock.get()){
            allocateNewBlock();
        }
        return currentId.incrementAndGet();
    }

    private void allocateNewBlock() {
        IdRangeTransactionService.IdRangeBlock block = idRangeTransactionService.allocateNewBlock(BLOCK_SIZE);
        currentId.set(block.blockStart() - 1);
        maxIdInBlock.set(block.blockEnd());
    }

}
