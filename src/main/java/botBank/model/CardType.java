package botBank.model;

/**
 * CardType represents the type of a bank card, which can be either DEBIT or CREDIT.
 */

public enum CardType {
    DEBIT, CREDIT;

    @Override
    public String toString() {
        return name();
    }
}
