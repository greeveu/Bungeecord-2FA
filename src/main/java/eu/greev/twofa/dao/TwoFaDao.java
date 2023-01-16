package eu.greev.twofa.dao;

import eu.greev.twofa.entities.UserData;

public interface TwoFaDao {

    void createTable();

    UserData loadUserData(String uuid);

    void saveUserData(String uuid, UserData user);

    void deleteUser(String uuid);

}
