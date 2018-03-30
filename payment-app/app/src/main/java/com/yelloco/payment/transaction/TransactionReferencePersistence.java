package com.yelloco.payment.transaction;

public abstract class TransactionReferencePersistence {

    private static final int INITIAL_REF = 1;

    private int maximumValue;

    public TransactionReferencePersistence(int maximumValue) {
        this.maximumValue = maximumValue;
    }

    protected abstract int getNewValue();

    public abstract int getCurrentValue();

    protected abstract void setValue(int value);

    public int getAndIncrementRef() {
        int newRef = getNewValue();

        setValue(newRef);
        if (newRef > maximumValue) {
            setValue(INITIAL_REF);
            return INITIAL_REF;
        }
        return newRef;
    }
}