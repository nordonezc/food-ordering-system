package com.food.ordering.system.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class Money {
    private static final int SCALE = 2;
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    private final BigDecimal amount;

    /**
     * CompareTo instead of equals, because 0.00 needs more specificity
     *
     * @return
     */
    public boolean isGreaterThanZero() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public Boolean isGreaterThan(Money money) {
        return this.amount != null && this.amount.compareTo(money.getAmount()) > 0;
    }

    public Money add(Money money) {
        return new Money(setScale(this.amount.add(money.getAmount())));
    }

    public Money substract(Money money) {
        return new Money(setScale(this.amount.subtract(money.getAmount())));
    }

    public Money multiply(int times) {
        return new Money(setScale(this.amount.multiply(BigDecimal.valueOf(times))));
    }

    private BigDecimal setScale(BigDecimal amount) {
        return amount.setScale(SCALE, RoundingMode.HALF_EVEN);
    }
}