package gr.hua.dit.studyrooms.core.exception;

public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
