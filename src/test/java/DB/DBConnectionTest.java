package DB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DBConnectionTest {
    @Test
    void usesOnlySuppliedCredentialsAndConnectionTimeouts() throws Exception {
        Map<String, String> environment = settings();
        DBConnection.Connector connector = mock(DBConnection.Connector.class);
        Connection expected = mock(Connection.class);
        when(connector.open(anyString(), any(Properties.class))).thenReturn(expected);

        Connection actual = DBConnection.getConnection(environment::get, connector);

        assertSame(expected, actual);
        ArgumentCaptor<Properties> properties = ArgumentCaptor.forClass(Properties.class);
        verify(connector).open(eq(environment.get("DB_URL")), properties.capture());
        assertEquals(environment.get("DB_USER"), properties.getValue().getProperty("user"));
        assertEquals(environment.get("DB_PASSWORD"), properties.getValue().getProperty("password"));
        assertEquals("5000", properties.getValue().getProperty("connectTimeout"));
        assertEquals("5000", properties.getValue().getProperty("socketTimeout"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DB_URL", "DB_USER", "DB_PASSWORD"})
    void missingConfigurationFailsBeforeOpeningAConnection(String key) throws Exception {
        Map<String, String> environment = settings();
        environment.remove(key);
        DBConnection.Connector connector = mock(DBConnection.Connector.class);

        SQLException exception = assertThrows(SQLException.class,
                () -> DBConnection.getConnection(environment::get, connector));

        assertEquals("Database configuration is incomplete.", exception.getMessage());
        assertFalse(exception.getMessage().contains("synthetic-password"));
        verify(connector, never()).open(anyString(), any(Properties.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "http://example.test", "jdbc:postgresql://example.test/menu"})
    void rejectsMissingOrNonMysqlUrlsWithoutConnecting(String url) throws Exception {
        Map<String, String> environment = settings();
        environment.put("DB_URL", url);
        DBConnection.Connector connector = mock(DBConnection.Connector.class);

        assertThrows(SQLException.class, () -> DBConnection.getConnection(environment::get, connector));

        verify(connector, never()).open(anyString(), any(Properties.class));
    }

    @Test
    void allowsAnExplicitlyEmptyPasswordWithoutChoosingADefault() throws Exception {
        Map<String, String> environment = settings();
        environment.put("DB_PASSWORD", "");
        DBConnection.Connector connector = mock(DBConnection.Connector.class);
        when(connector.open(anyString(), any(Properties.class))).thenReturn(mock(Connection.class));

        DBConnection.getConnection(environment::get, connector);

        ArgumentCaptor<Properties> properties = ArgumentCaptor.forClass(Properties.class);
        verify(connector).open(eq(environment.get("DB_URL")), properties.capture());
        assertEquals("", properties.getValue().getProperty("password"));
    }

    @Test
    void passesConnectionFailuresToTheServletErrorHandler() throws Exception {
        Map<String, String> environment = settings();
        DBConnection.Connector connector = mock(DBConnection.Connector.class);
        SQLException expected = new SQLException("synthetic connection failure");
        when(connector.open(anyString(), any(Properties.class))).thenThrow(expected);

        SQLException actual = assertThrows(SQLException.class,
                () -> DBConnection.getConnection(environment::get, connector));

        assertSame(expected, actual);
    }

    private Map<String, String> settings() {
        return new HashMap<>(Map.of(
                "DB_URL", "jdbc:mysql://database.invalid/dmn_demo",
                "DB_USER", "synthetic-user",
                "DB_PASSWORD", "synthetic-password"));
    }
}
