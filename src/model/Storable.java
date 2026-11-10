package model;

/**
 * Anything that can be saved as one line in a text file.
 */
public interface Storable {

    String DELIMITER = "|";

    String getId();

    String toFileLine();
}
