package com.model.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerItem {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    private LocalDate date;
    private TxType type;
    private String category;
    private int amount;
    private String memo;

}
