package botBank.model;

public enum CardType {
    DEBIT, CREDIT;

    @Override
    public String toString() {
        return name();
    }
}
