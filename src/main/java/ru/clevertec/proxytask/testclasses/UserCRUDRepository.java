package ru.clevertec.proxytask.testclasses;

import java.util.Optional;

public interface UserCRUDRepository extends CRUDRepository<User> {
    public Optional<User> getUserByName(String userName);
}
