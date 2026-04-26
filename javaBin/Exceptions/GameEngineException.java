package Exceptions;

public class GameEngineException extends RuntimeException {
    public GameEngineException(String message) {
        super(message);
    }

    public GameEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
