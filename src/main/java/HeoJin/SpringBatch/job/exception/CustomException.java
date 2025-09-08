package HeoJin.SpringBatch.job.exception;

public class CustomException extends Exception {
    // Spring Batch 전용 예외
    public CustomException(String message) {
        super(message);
    }
    
    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
