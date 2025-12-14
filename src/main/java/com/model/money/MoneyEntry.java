package com.model.money;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyEntry {
    protected String id;        // 고유 ID
    protected LocalDate date;   // 날짜
    protected int amount;       // 금액
    protected String category;  // 카테고리
    protected String memo;      // 메모
}
