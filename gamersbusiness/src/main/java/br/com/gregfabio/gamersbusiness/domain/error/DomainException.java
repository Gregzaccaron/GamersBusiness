package br.com.gregfabio.gamersbusiness.domain.error;

public final class DomainException extends RuntimeException {
    public enum Reason {
        BAD_REQUEST,
        UNAUTHENTICATED,
        FORBIDDEN,
        NOT_FOUND,
        CONFLICT
    }

    private final Reason reason;

    private DomainException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public static DomainException badRequest(String message) {
        return new DomainException(Reason.BAD_REQUEST, message);
    }

    public static DomainException unauthenticated(String message) {
        return new DomainException(Reason.UNAUTHENTICATED, message);
    }

    public static DomainException forbidden(String message) {
        return new DomainException(Reason.FORBIDDEN, message);
    }

    public static DomainException notFound(String message) {
        return new DomainException(Reason.NOT_FOUND, message);
    }

    public static DomainException conflict(String message) {
        return new DomainException(Reason.CONFLICT, message);
    }
}
