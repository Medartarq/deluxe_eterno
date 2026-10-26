package com.deluxeterno.service;

/** Conflicto de negocio recuperable en el formulario de categorías. */
public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException() {
        super("Ya existe una categoría con ese nombre.");
    }
}
