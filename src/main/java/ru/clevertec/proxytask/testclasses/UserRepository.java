package ru.clevertec.proxytask.testclasses;


import ru.clevertec.proxytask.annotation.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UserRepository implements UserCRUDRepository {

    private static Long id = 0L;

    private static Map<Long, User> users = new HashMap<>();

    static {
        Role user = new Role(1L, "USER");
        Role admin = new Role(2L, "ADMIN");
        users.put(1L, new User(1, "Sema", "123", Set.of(user)));
        users.put(2L, new User(2, "John", "789", Set.of(admin)));
        id = 2L;
    }

   // @Override
    public User create(User user) {
        user.setId(++id);
        users.put(id, user);
        return users.get(id);
    }

  //  @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Log
    @Override
    public User getById(Long id) {
        return users.get(id);
    }

    @Override
    public boolean deleteById(Long id) {
        return users.remove(id) != null;
    }

    @Log
    public Optional<User> getUserByName(String userName) {
        return users.values().stream().filter(u -> u.getName().equals(userName)).findFirst();
    }
}
