package com.model.ledger;

import lombok.Getter;

@Getter
public class LedgerSum {
    private int income;
    private int expense;

    public void addIncome(int amount) {
        income += amount;
    }

    public void addExpense(int amount) {
        expense += amount;
    }
}
