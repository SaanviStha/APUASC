package ui;

import exception.ValidationException;
import model.User;

import java.util.List;

/**
 * Lets the same user-management screen work for managers (staff accounts)
 * and counter staff (customers).
 */
public interface UserCrudHandler {

    List<? extends User> list();

    void add(User user) throws ValidationException;

    void update(User user) throws ValidationException;

    void delete(String userId) throws ValidationException;
}
