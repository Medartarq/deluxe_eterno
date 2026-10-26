package com.deluxeterno;

import com.deluxeterno.demo.DemoDatabaseIdentity;
import org.junit.jupiter.api.Test;
import java.sql.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DemoDatabaseGuardTests {
 @Test void mainDatabaseIsRejectedBeforeAnyStatementCanRun() throws Exception {
  Connection connection=mock(Connection.class);when(connection.getCatalog()).thenReturn("deluxe_eterno");
  assertThatThrownBy(()->DemoDatabaseIdentity.verify(connection)).isInstanceOf(IllegalStateException.class).hasMessageContaining("solo puede utilizar");
  verify(connection,never()).prepareStatement(anyString());
 }
 @Test void schemaNameAloneIsNotEnoughWithoutProjectMarker() throws Exception {
  Connection connection=mock(Connection.class);when(connection.getCatalog()).thenReturn("deluxe_eterno_demo");
  PreparedStatement statement=mock(PreparedStatement.class);ResultSet rows=mock(ResultSet.class);
  when(connection.prepareStatement(anyString())).thenReturn(statement);when(statement.executeQuery()).thenReturn(rows);
  when(rows.next()).thenReturn(true);when(rows.getString(1)).thenReturn("otra-aplicacion");
  assertThatThrownBy(()->DemoDatabaseIdentity.verify(connection)).isInstanceOf(IllegalStateException.class).hasMessageContaining("identificación");
 }
}
