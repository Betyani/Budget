package com.model.money;

import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
public class Income extends MoneyEntry {

    @Builder
    public Income(String id,
                  LocalDate date,
                  int amount,
                  String category,
                  String memo) {
        super(id, date, amount, category, memo);
    }
}


