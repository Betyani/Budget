package com.model.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerItem {
    private LocalDate date;
    private TxType type;
    private String category;
    private int amount;
    private String memo;

}
