package org.ikigaidigital.calculator;

public class InvalidPlanTypeException extends RuntimeException {
    public InvalidPlanTypeException(String planType) {
        super("Unsupported plan type: '" + planType + "'");
    }
}
