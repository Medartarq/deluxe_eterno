package com.deluxeterno.service;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() { super("Ya existe una cuenta con ese nombre de usuario."); }
}
