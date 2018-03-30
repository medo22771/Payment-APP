package com.yelloco.payment.transaction;

public enum TransactionResult {
    APPROVED("approved", "01"),
    DECLINED("declined", "02"),
    CANCELED("canceled", "99");

    private String result;
    private String code;

    TransactionResult(String result, String code) {
        this.result = result;
        this.code = code;
    }

    public static TransactionResult getByCode(String code) {
        for (TransactionResult transactionResult: TransactionResult.values()) {
            if (transactionResult.getCode().equalsIgnoreCase(code)) {
                return transactionResult;
            }
        }
        return null;
    }

    public static TransactionResult getByResult(String resultCode) {
        for (TransactionResult transactionResult: TransactionResult.values()) {
            if (transactionResult.getResult().equalsIgnoreCase(resultCode)) {
                return transactionResult;
            }
        }
        return null;
    }

    public String getResult() {
        return result;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "result='" + result + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}