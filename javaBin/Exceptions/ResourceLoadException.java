package Exceptions;

public class ResourceLoadException extends GameEngineException {
    public ResourceLoadException(String resourcePath, Throwable cause) {
        super("Failed to load resource: " + resourcePath, cause);
    }

    public ResourceLoadException(String resourcePath) {
        super("Failed to load resource: " + resourcePath);
    }
}
