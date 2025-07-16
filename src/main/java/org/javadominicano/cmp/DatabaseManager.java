package org.javadominicano.cmp;

import org.javadominicano.cmp.model.StationModel;
import org.javadominicano.cmp.model.SensorModel;
import org.javadominicano.cmp.model.RecordModel;
import org.javadominicano.cmp.model.AlertRuleModel;
import org.javadominicano.cmp.dto.ReporteRecordDTO;
import org.javadominicano.cmp.dto.AlertaDTO;
import org.javadominicano.cmp.dto.AlertRuleDTO;

import org.javadominicano.cmp.dto.ReporteResumenDTO;

import java.util.Map;
import java.util.LinkedHashMap;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseManager {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    public DatabaseManager() {
        this("jdbc:mysql://192.168.100.168/MqttBase", "usermqtt", "Mqtt1234!");
    }

    public DatabaseManager(String dbUrl, String dbUser, String dbPass) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        createAlertRuleTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    public List<StationModel> getStations() {
        List<StationModel> list = new ArrayList<>();
        String query = "SELECT * FROM Station";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                StationModel s = new StationModel();
                s.setStationId(rs.getInt("station_id"));
                s.setStationModel(rs.getString("station_model"));
                s.setUbicacion(rs.getString("ubicacion"));
                s.setLatitud(rs.getObject("latitud") != null ? rs.getDouble("latitud") : null);
                s.setLongitud(rs.getObject("longitud") != null ? rs.getDouble("longitud") : null);
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SensorModel> getSensorsByStation(int stationId) {
        List<SensorModel> list = new ArrayList<>();
        String query = "SELECT * FROM Sensor WHERE station_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, stationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SensorModel s = new SensorModel();
                    s.setSensorId(rs.getInt("sensor_id"));
                    s.setStationId(rs.getInt("station_id"));
                    s.setSensorModel(rs.getString("sensor_model"));
                    s.setSensorType(rs.getString("sensor_type"));
                    s.setUnit(rs.getString("unit"));
                    s.setActivo(rs.getBoolean("activo"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RecordModel> getRecordsBySensor(int sensorId) {
        List<RecordModel> list = new ArrayList<>();
        String query = "SELECT * FROM Record WHERE sensor_id = ? ORDER BY record_datetime DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, sensorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecordModel r = new RecordModel();
                    r.setRecordId(rs.getInt("record_id"));
                    r.setSensorId(rs.getInt("sensor_id"));
                    r.setValue(rs.getFloat("value"));
                    r.setRecordDatetime(rs.getTimestamp("record_datetime"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public RecordModel getLastRecord(int sensorId) {
        String query = "SELECT * FROM Record WHERE sensor_id = ? ORDER BY record_datetime DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, sensorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RecordModel r = new RecordModel();
                    r.setRecordId(rs.getInt("record_id"));
                    r.setSensorId(rs.getInt("sensor_id"));
                    r.setValue(rs.getFloat("value"));
                    r.setRecordDatetime(rs.getTimestamp("record_datetime"));
                    return r;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getOrCreateStation(String stationModel) {
    try (Connection conn = getConnection()) {
        // Buscar estación existente
        String query = "SELECT station_id FROM Station WHERE station_model = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, stationModel);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("station_id");
            }
        }

        // Insertar nueva estación
        String insert = "INSERT INTO Station (station_model) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, stationModel);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}

public int getOrCreateSensor(int stationId, String sensorModel, String sensorType, String unit) {
    try (Connection conn = getConnection()) {
        // Buscar sensor existente
        String query = "SELECT sensor_id FROM Sensor WHERE station_id = ? AND sensor_model = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, stationId);
            stmt.setString(2, sensorModel);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("sensor_id");
            }
        }

        // Insertar nuevo sensor con activo = 1
        String insert = "INSERT INTO Sensor (station_id, sensor_model, sensor_type, unit, activo) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, stationId);
            stmt.setString(2, sensorModel);
            stmt.setString(3, sensorType);
            stmt.setString(4, unit);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}


/*
public void insertRecord(int sensorId, double value, Date date) {
    String insert = "INSERT INTO Record (sensor_id, value, record_datetime) VALUES (?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(insert)) {
        stmt.setInt(1, sensorId);
        stmt.setDouble(2, value);
        stmt.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}*/

public void insertRecord(int sensorId, double value, Date date) {
    String check = "SELECT activo FROM Sensor WHERE sensor_id = ?";
    String insert = "INSERT INTO Record (sensor_id, value, record_datetime) VALUES (?, ?, ?)";

    try (Connection conn = getConnection()) {
        try (PreparedStatement checkStmt = conn.prepareStatement(check)) {
            checkStmt.setInt(1, sensorId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getBoolean("activo")) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insert)) {
                    insertStmt.setInt(1, sensorId);
                    insertStmt.setDouble(2, value);
                    insertStmt.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
                    insertStmt.executeUpdate();
                }
            } else {
                System.out.println("⚠️ Registro ignorado: sensor deshabilitado (ID=" + sensorId + ")");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


public Date getLastRecordTimeByStation(int stationId) {
    String query = """
        SELECT MAX(r.record_datetime) AS last_time
        FROM Record r
        JOIN Sensor s ON r.sensor_id = s.sensor_id
        WHERE s.station_id = ?
    """;

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setInt(1, stationId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Timestamp ts = rs.getTimestamp("last_time");
            return (ts != null) ? new Date(ts.getTime()) : null;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}

public int countActiveAlerts() {
    String query = "SELECT COUNT(*) FROM WeatherAlert";
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        if (rs.next()) {
            return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

public List<RecordModel> getLatestRecordsByStation() {
    List<RecordModel> list = new ArrayList<>();
    String query = """
        SELECT r.*
        FROM Record r
        JOIN (
            SELECT sensor_id, MAX(record_datetime) AS max_date
            FROM Record
            GROUP BY sensor_id
        ) latest ON r.sensor_id = latest.sensor_id AND r.record_datetime = latest.max_date
    """;

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            RecordModel r = new RecordModel();
            r.setRecordId(rs.getInt("record_id"));
            r.setSensorId(rs.getInt("sensor_id"));
            r.setValue(rs.getFloat("value"));
            r.setRecordDatetime(rs.getTimestamp("record_datetime"));
            list.add(r);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
    public StationModel getStationById(int stationId) {
        String query = "SELECT * FROM Station WHERE station_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, stationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                StationModel s = new StationModel();
                s.setStationId(rs.getInt("station_id"));
                s.setStationModel(rs.getString("station_model"));
                s.setUbicacion(rs.getString("ubicacion"));
                s.setLatitud(rs.getObject("latitud") != null ? rs.getDouble("latitud") : null);
                s.setLongitud(rs.getObject("longitud") != null ? rs.getDouble("longitud") : null);
                return s;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateStation(StationModel station) {
        String update = "UPDATE Station SET station_model = ?, ubicacion = ?, latitud = ?, longitud = ? WHERE station_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setString(1, station.getStationModel());
            stmt.setString(2, station.getUbicacion());
            if (station.getLatitud() != null) {
                stmt.setDouble(3, station.getLatitud());
            } else {
                stmt.setNull(3, Types.DOUBLE);
            }
            if (station.getLongitud() != null) {
                stmt.setDouble(4, station.getLongitud());
            } else {
                stmt.setNull(4, Types.DOUBLE);
            }
            stmt.setInt(5, station.getStationId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

public void deleteStation(int stationId) {
    try (Connection conn = getConnection()) {
        // Deshabilitar autocommit para tratarlo como una transacción
        conn.setAutoCommit(false);

        // 1. Borrar las alertas asociadas a la estación
        try (PreparedStatement deleteAlerts = conn.prepareStatement("DELETE FROM WeatherAlert WHERE station_id = ?")) {
            deleteAlerts.setInt(1, stationId);
            deleteAlerts.executeUpdate();
        }
        // 2. Borrar los records de los sensores de la estación
        try (PreparedStatement deleteRecords = conn.prepareStatement(
                "DELETE FROM Record WHERE sensor_id IN (SELECT sensor_id FROM Sensor WHERE station_id = ?)")) {
            deleteRecords.setInt(1, stationId);
            deleteRecords.executeUpdate();
        }
        // 3. Borrar los sensores de la estación
        try (PreparedStatement deleteSensors = conn.prepareStatement(
                "DELETE FROM Sensor WHERE station_id = ?")) {
            deleteSensors.setInt(1, stationId);
            deleteSensors.executeUpdate();
        }
        // 4. Finalmente, borrar la estación
        try (PreparedStatement deleteStation = conn.prepareStatement(
                "DELETE FROM Station WHERE station_id = ?")) {
            deleteStation.setInt(1, stationId);
            deleteStation.executeUpdate();
        }

        // Si todo fue bien, confirmar la transacción
        conn.commit();
    } catch (SQLException e) {
        e.printStackTrace();
        // Si algo falla, no se hace ningún cambio en la BD
    }
}

public List<RecordModel> getFilteredRecords(Integer stationId, Date desde, Date hasta) {
    List<RecordModel> list = new ArrayList<>();

    StringBuilder query = new StringBuilder("""
        SELECT r.* FROM Record r
        JOIN Sensor s ON r.sensor_id = s.sensor_id
        WHERE 1=1
    """);

    List<Object> params = new ArrayList<>();

    if (stationId != null) {
        query.append(" AND s.station_id = ?");
        params.add(stationId);
    }

    if (desde != null) {
        query.append(" AND r.record_datetime >= ?");
        params.add(new java.sql.Timestamp(desde.getTime()));
    }

    if (hasta != null) {
        query.append(" AND r.record_datetime <= ?");
        params.add(new java.sql.Timestamp(hasta.getTime()));
    }

    query.append(" ORDER BY r.record_datetime DESC");

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(query.toString())) {

        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            RecordModel r = new RecordModel();
            r.setRecordId(rs.getInt("record_id"));
            r.setSensorId(rs.getInt("sensor_id"));
            r.setValue(rs.getFloat("value"));
            r.setRecordDatetime(rs.getTimestamp("record_datetime"));
            list.add(r);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return list;
}

public List<AlertaDTO> getAlertasActivas() {
    List<AlertaDTO> alertas = new ArrayList<>();

    String sql = """
        SELECT 
            wa.alert_datetime AS fecha,
            s.station_model AS nombreEstacion,
            se.sensor_model AS sensorNombre,
            se.sensor_type AS tipoSensor,
            wa.value AS valor,
            wa.message AS mensaje
        FROM WeatherAlert wa
        JOIN Sensor se ON wa.sensor_id = se.sensor_id
        JOIN Station s ON wa.station_id = s.station_id
        ORDER BY wa.alert_datetime DESC
        LIMIT 20
    """;

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            AlertaDTO alerta = new AlertaDTO();
            alerta.setFecha(rs.getTimestamp("fecha"));
            alerta.setNombreEstacion(rs.getString("nombreEstacion"));
            alerta.setSensorNombre(rs.getString("sensorNombre"));
            alerta.setTipoSensor(rs.getString("tipoSensor"));
            alerta.setValor(rs.getDouble("valor"));
            alerta.setMensaje(rs.getString("mensaje"));
            alertas.add(alerta);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return alertas;
}

    public List<ReporteRecordDTO> getReporteRecords(Integer stationId, Date desde, Date hasta) {
        List<ReporteRecordDTO> list = new ArrayList<>();

    StringBuilder query = new StringBuilder("""
        SELECT
            st.station_model AS nombreEstacion,
            s.sensor_model AS modeloSensor,
            s.sensor_type AS tipoSensor,
            r.value AS valor,
            s.unit AS unidad,
            r.record_datetime AS fecha
        FROM Record r
        JOIN Sensor s ON r.sensor_id = s.sensor_id
        JOIN Station st ON s.station_id = st.station_id
        WHERE 1=1
    """);

    List<Object> params = new ArrayList<>();

    if (stationId != null) {
        query.append(" AND st.station_id = ?");
        params.add(stationId);
    }

    if (desde != null) {
        query.append(" AND r.record_datetime >= ?");
        params.add(new java.sql.Timestamp(desde.getTime()));
    }

    if (hasta != null) {
        // Para incluir todo el día de "hasta", ajustamos la hora a 23:59:59
        Calendar cal = Calendar.getInstance();
        cal.setTime(hasta);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        query.append(" AND r.record_datetime <= ?");
        params.add(new java.sql.Timestamp(cal.getTimeInMillis()));
    }

    query.append(" ORDER BY r.record_datetime DESC");

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(query.toString())) {

        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            ReporteRecordDTO dto = new ReporteRecordDTO();
            dto.setNombreEstacion(rs.getString("nombreEstacion"));
            dto.setModeloSensor(rs.getString("modeloSensor"));
            dto.setTipoSensor(rs.getString("tipoSensor"));
            dto.setValor(rs.getDouble("valor"));
            dto.setUnidad(rs.getString("unidad"));
            dto.setFecha(rs.getTimestamp("fecha"));
            list.add(dto);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return list;
}

public void insertAlert(int stationId, int sensorId, double value, String message) {
    String sql = "INSERT INTO WeatherAlert (station_id, sensor_id, value, message) VALUES (?, ?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, stationId);
        stmt.setInt(2, sensorId);
        stmt.setDouble(3, value);
        stmt.setString(4, message);
        stmt.executeUpdate();
        System.out.printf("⚠️ Alerta guardada: %s - Valor=%.2f\n", message, value);
    } catch (SQLException e) {
        System.out.println("❌ Error insertando alerta:");
        e.printStackTrace();
    }
}

    public void toggleSensorActivo(int sensorId) {
        String sql = "UPDATE Sensor SET activo = NOT activo WHERE sensor_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sensorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==== Alert Rule management ====

    public void insertAlertRule(int stationId, int sensorId, String tipo, double umbral) {
        String sql = "INSERT INTO AlertRule (station_id, sensor_id, tipo, umbral, activa) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stationId);
            stmt.setInt(2, sensorId);
            stmt.setString(3, tipo);
            stmt.setDouble(4, umbral);
            stmt.executeUpdate();
            System.out.printf("📝 Regla de alerta creada: %s %.2f (%d-%d)\n", tipo, umbral, stationId, sensorId);
        } catch (SQLException e) {
            System.out.println("❌ Error insertando regla de alerta:");
            e.printStackTrace();
        }
    }

    public List<AlertRuleModel> getAlertRulesBySensor(int stationId, int sensorId) {
        List<AlertRuleModel> reglas = new ArrayList<>();
        String sql = "SELECT * FROM AlertRule WHERE station_id = ? AND sensor_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stationId);
            stmt.setInt(2, sensorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AlertRuleModel r = new AlertRuleModel();
                    r.setRuleId(rs.getInt("rule_id"));
                    r.setStationId(rs.getInt("station_id"));
                    r.setSensorId(rs.getInt("sensor_id"));
                    r.setTipo(rs.getString("tipo"));
                    r.setUmbral(rs.getDouble("umbral"));
                    r.setActiva(rs.getBoolean("activa"));
                    reglas.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reglas;
    }

    public void updateAlertRuleState(int ruleId, boolean activa) {
        String sql = "UPDATE AlertRule SET activa = ? WHERE rule_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, activa);
            stmt.setInt(2, ruleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<AlertRuleDTO> getAlertRuleDTOs() {
        List<AlertRuleDTO> list = new ArrayList<>();
        String sql = """
            SELECT ar.*, st.station_model, se.sensor_model
            FROM AlertRule ar
            JOIN Station st ON ar.station_id = st.station_id
            JOIN Sensor se ON ar.sensor_id = se.sensor_id
            ORDER BY ar.rule_id DESC
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AlertRuleDTO dto = new AlertRuleDTO();
                dto.setRuleId(rs.getInt("rule_id"));
                dto.setStationId(rs.getInt("station_id"));
                dto.setSensorId(rs.getInt("sensor_id"));
                dto.setTipo(rs.getString("tipo"));
                dto.setUmbral(rs.getDouble("umbral"));
                dto.setActiva(rs.getBoolean("activa"));
                dto.setNombreEstacion(rs.getString("station_model"));
                dto.setSensorNombre(rs.getString("sensor_model"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ReporteResumenDTO> getResumenMaxMin(Integer stationId, Date desde, Date hasta) {
        Map<Integer, ReporteResumenDTO> resumenMap = new LinkedHashMap<>();

        StringBuilder query = new StringBuilder("""
            SELECT
                st.station_model AS nombreEstacion,
                s.sensor_model AS sensorNombre,
                s.sensor_type AS tipoSensor,
                r.value AS valor,
                r.record_datetime AS fecha,
                s.sensor_id AS sensorId
            FROM Record r
            JOIN Sensor s ON r.sensor_id = s.sensor_id
            JOIN Station st ON s.station_id = st.station_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (stationId != null) {
            query.append(" AND st.station_id = ?");
            params.add(stationId);
        }

        if (desde != null) {
            query.append(" AND r.record_datetime >= ?");
            params.add(new java.sql.Timestamp(desde.getTime()));
        }

        if (hasta != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(hasta);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            query.append(" AND r.record_datetime <= ?");
            params.add(new java.sql.Timestamp(cal.getTimeInMillis()));
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int sId = rs.getInt("sensorId");
                double valor = rs.getDouble("valor");
                Date fecha = rs.getTimestamp("fecha");
                ReporteResumenDTO dto = resumenMap.get(sId);
                if (dto == null) {
                    dto = new ReporteResumenDTO();
                    dto.setNombreEstacion(rs.getString("nombreEstacion"));
                    dto.setSensorNombre(rs.getString("sensorNombre"));
                    dto.setTipoSensor(rs.getString("tipoSensor"));
                    dto.setValorMax(valor);
                    dto.setFechaMax(fecha);
                    dto.setValorMin(valor);
                    dto.setFechaMin(fecha);
                    resumenMap.put(sId, dto);
                } else {
                    if (valor > dto.getValorMax()) {
                        dto.setValorMax(valor);
                        dto.setFechaMax(fecha);
                    }
                    if (valor < dto.getValorMin()) {
                        dto.setValorMin(valor);
                        dto.setFechaMin(fecha);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(resumenMap.values());
    }


    public List<AlertRuleModel> getAllAlertRules() {
        List<AlertRuleModel> reglas = new ArrayList<>();
        String sql = "SELECT * FROM AlertRule";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AlertRuleModel r = new AlertRuleModel();
                r.setRuleId(rs.getInt("rule_id"));
                r.setStationId(rs.getInt("station_id"));
                r.setSensorId(rs.getInt("sensor_id"));
                r.setTipo(rs.getString("tipo"));
                r.setUmbral(rs.getDouble("umbral"));
                r.setActiva(rs.getBoolean("activa"));
                reglas.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reglas;
    }

    private void createAlertRuleTable() {
        String sql = "CREATE TABLE IF NOT EXISTS AlertRule (" +
                "rule_id INT AUTO_INCREMENT PRIMARY KEY," +
                "station_id INT NOT NULL," +
                "sensor_id INT NOT NULL," +
                "tipo VARCHAR(10) NOT NULL," +
                "umbral DOUBLE NOT NULL," +
                "activa BOOLEAN NOT NULL DEFAULT 0," +
                "fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }




}
