package net.forthecrown.user;

public interface BooleanDataAccessor<T> extends UserDataAccessor {
    boolean getStatus(UserDataContainer c, T val);
    void setStatus(UserDataContainer c, T val, boolean state);
}