package com.deluxeterno.demo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Identidad comprobada antes de que Hibernate pueda crear o modificar tablas. */
public final class DemoDatabaseIdentity {
    public static final String SCHEMA = "deluxe_eterno_demo";
    public static final String APP_KEY = "deluxe-eterno-demo";
    private DemoDatabaseIdentity() {}

    public static void verify(DataSource source) {
        try (Connection connection = source.getConnection()) {
            verify(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo verificar la base demo. Ejecuta scripts/demo.ps1 -Action Initialize.", exception);
        }
    }

    public static void verify(Connection connection) throws SQLException {
        if (!SCHEMA.equalsIgnoreCase(connection.getCatalog()))
            throw new IllegalStateException("El perfil demo solo puede utilizar la base deluxe_eterno_demo. Arranque cancelado antes de modificar el esquema.");
        try (var statement = connection.prepareStatement("select app_key from demo_environment where id = 1");
             var rows = statement.executeQuery()) {
            if (!rows.next() || !APP_KEY.equals(rows.getString(1)))
                throw new IllegalStateException("La base no tiene la identificación de este entorno demo. Operación cancelada.");
        }
    }
}
