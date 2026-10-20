package com.arvind.medicine.exception;

public class MedicineImportFailedException extends RuntimeException {

    public MedicineImportFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
