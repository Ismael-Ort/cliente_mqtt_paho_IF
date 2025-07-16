package org.javadominicano.cmp;

import org.javadominicano.cmp.dto.StationStatusDTO;
import org.javadominicano.cmp.dto.StationDetailsDTO;
import org.javadominicano.cmp.dto.ReporteRecordDTO;
import org.javadominicano.cmp.dto.AlertaDTO;
import org.javadominicano.cmp.model.RecordModel;
import org.javadominicano.cmp.model.SensorModel;
import org.javadominicano.cmp.model.StationModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.format.annotation.DateTimeFormat;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.util.*;

@Controller
public class EstacionController {

    private final DatabaseManager dbManager;

    public EstacionController() {
        this.dbManager = new DatabaseManager(
            "jdbc:mysql://192.168.100.168/MqttBase",
            "usermqtt",
            "Mqtt1234!"
        );
    }

    @GetMapping("/dashboard/estaciones")
    public String dashboardEstaciones(Model model) {
        List<StationStatusDTO> resumen = getStationStatusSummary();
        List<StationDetailsDTO> estaciones = getStationDetails();

        model.addAttribute("resumen", resumen);
        model.addAttribute("estaciones", estaciones);

        return "dashboard";
    }

    @GetMapping("/api/stations/status-summary")
    @ResponseBody
    public List<StationStatusDTO> getStationStatusSummary() {
        List<StationStatusDTO> result = new ArrayList<>();
        List<StationModel> stations = dbManager.getStations();

        for (StationModel station : stations) {
            StationStatusDTO dto = new StationStatusDTO();
            dto.setStationName(station.getStationModel());

            List<SensorModel> sensores = dbManager.getSensorsByStation(station.getStationId());
            Map<String, Double> data = new HashMap<>();
            Date ultimaFecha = null;

            for (SensorModel sensor : sensores) {
                RecordModel last = dbManager.getLastRecord(sensor.getSensorId());
                if (last != null) {
                    double valor = Math.round(last.getValue() * 10.0) / 10.0;
                    String tipo = sensor.getSensorType().toLowerCase().trim().replace("ó", "o");

                    switch (tipo) {
                        case "temperatura":
                        case "temp":
                            data.put("temperatura", valor);
                            break;
                        case "humedad":
                        case "hum":
                            data.put("humedad", valor);
                            break;
                        case "presion":
                        case "pres":
                        case "presión":
                        case "pressure":
                            data.put("presion", valor);
                            break;
                        case "viento":
                            data.put("viento", valor);
                            break;
                        case "precipitacion":
                        case "precipitación":
                            data.put("precipitacion", valor);
                            break;
                        case "humedad_suelo":
                            data.put("humedad_suelo", valor);
                            break;
                    }

                    if (ultimaFecha == null || last.getRecordDatetime().after(ultimaFecha)) {
                        ultimaFecha = last.getRecordDatetime();
                    }
                }
            }

            dto.setData(data);
            dto.setLastUpdate(ultimaFecha);
            dto.setStatus((ultimaFecha != null && new Date().getTime() - ultimaFecha.getTime() < 3 * 60 * 1000)
                    ? "EN_LINEA" : "DESCONECTADA");

            result.add(dto);
        }

        return result;
    }

    @GetMapping("/api/stations/details")
    @ResponseBody
    public List<StationDetailsDTO> getStationDetails() {
        List<StationModel> estaciones = dbManager.getStations();
        List<StationDetailsDTO> result = new ArrayList<>();

        for (StationModel est : estaciones) {
            StationDetailsDTO dto = new StationDetailsDTO();
            dto.setNombre(est.getStationModel());

            List<SensorModel> sensores = dbManager.getSensorsByStation(est.getStationId());
            Date ultimaFecha = null;

            for (SensorModel sensor : sensores) {
                RecordModel r = dbManager.getLastRecord(sensor.getSensorId());
                if (r != null) {
                    String tipo = sensor.getSensorType().toLowerCase().trim().replace("ó", "o");

                    switch (tipo) {
                        case "temperatura":
                        case "temp":
                            dto.setTemperatura(Math.round(r.getValue() * 10.0) / 10.0);
                            break;
                        case "humedad":
                        case "hum":
                            dto.setHumedad(Math.round(r.getValue() * 10.0) / 10.0);
                            break;
                        case "presion":
                        case "pres":
                        case "presión":
                        case "pressure":
                            dto.setPresion(Math.round(r.getValue() * 10.0) / 10.0);
                            break;
                        case "viento":
                            dto.setViento(Math.round(r.getValue() * 10.0) / 10.0);
                            break;
                        case "precipitacion":
                            dto.setPrecipitacion(Math.round(r.getValue() * 10.0) / 10.0);
                            break;
                        case "humedad_suelo":
                            dto.setHumedadSuelo(Math.round(r.getValue() * 10.0) / 10.0);
                            break;

                    }

                    if (ultimaFecha == null || r.getRecordDatetime().after(ultimaFecha)) {
                        ultimaFecha = r.getRecordDatetime();
                    }
                }
            }

            if (ultimaFecha != null) {
                long minutos = (new Date().getTime() - ultimaFecha.getTime()) / (60 * 1000);
                dto.setMinutosDesdeUltimaLectura(minutos);
                dto.setEnLinea(minutos < 3);
            } else {
                dto.setEnLinea(false);
            }

            result.add(dto);
        }

        return result;
    }

    @GetMapping("/api/stations")
    @ResponseBody
    public List<StationModel> getAllStations() {
        return dbManager.getStations();
    }

    @GetMapping("/api/stations/{stationId}/sensors")
    @ResponseBody
    public List<SensorModel> getSensorsByStation(@PathVariable int stationId) {
        return dbManager.getSensorsByStation(stationId);
    }

    @GetMapping("/api/sensors/{sensorId}/records")
    @ResponseBody
    public List<RecordModel> getRecordsBySensor(@PathVariable int sensorId) {
        return dbManager.getRecordsBySensor(sensorId);
    }

    @GetMapping("/api/sensors/{sensorId}/last")
    @ResponseBody
    public RecordModel getLastRecord(@PathVariable int sensorId) {
        return dbManager.getLastRecord(sensorId);
    }

    @GetMapping("/api/stations/summary")
    @ResponseBody
    public Map<String, Object> getSummary() {
        List<StationModel> estaciones = dbManager.getStations();
        int total = estaciones.size();
        int enLinea = 0;
        int desconectadas = 0;
        int alertas = dbManager.countActiveAlerts();

        for (StationModel est : estaciones) {
            Date last = dbManager.getLastRecordTimeByStation(est.getStationId());
            if (last != null && new Date().getTime() - last.getTime() < 3 * 60 * 1000) {
                enLinea++;
            } else {
                desconectadas++;
            }
        }

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("total", total);
        resumen.put("enLinea", enLinea);
        resumen.put("desconectadas", desconectadas);
        resumen.put("alertasActivas", alertas);
        return resumen;
    }

    /*
    @GetMapping("/dashboard/administrar-estaciones")
    public String administrarEstaciones(Model model) {
        List<StationModel> estaciones = dbManager.getStations();
        model.addAttribute("estaciones", estaciones);
        return "stations";
    }
    */

    @GetMapping("/dashboard/administrar-estaciones")
    @PreAuthorize("hasRole('ADMIN')")
    public String administrarEstaciones(Model model) {
        List<StationModel> estaciones = dbManager.getStations();

        for (StationModel estacion : estaciones) {
            estacion.setSensores(dbManager.getSensorsByStation(estacion.getStationId()));  // 👈 ESTO es clave
        }

        model.addAttribute("estaciones", estaciones);
        return "stations";
    }


    @PostMapping("/dashboard/estaciones/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarEstacion(@RequestParam String stationModel) {
        int stationId = dbManager.getOrCreateStation(stationModel);

        // Sensores por defecto
        dbManager.getOrCreateSensor(stationId, "Sensor_Temp", "temperatura", "°C");
        dbManager.getOrCreateSensor(stationId, "Sensor_Hum", "humedad", "%");
        dbManager.getOrCreateSensor(stationId, "Sensor_Pres", "presion", "hPa");
        dbManager.getOrCreateSensor(stationId, "Sensor_Viento", "viento", "m/s");
        dbManager.getOrCreateSensor(stationId, "Sensor_Precipitacion", "precipitacion", "mm");
        dbManager.getOrCreateSensor(stationId, "Sensor_HumedadSuelo", "humedad_suelo", "%");

        return "redirect:/dashboard/administrar-estaciones";
    }


    @GetMapping("/dashboard/estaciones/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editarEstacion(@PathVariable int id, Model model) {
        StationModel estacion = dbManager.getStationById(id);
        model.addAttribute("estacion", estacion);
        return "editar-estacion";
    }

    @PostMapping("/dashboard/estaciones/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public String actualizarEstacion(@ModelAttribute StationModel estacion) {
        dbManager.updateStation(estacion);
        return "redirect:/dashboard/administrar-estaciones";
    }

    @PostMapping("/dashboard/estaciones/borrar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String borrarEstacion(@PathVariable int id) {
        dbManager.deleteStation(id);
        return "redirect:/dashboard/administrar-estaciones";
    }

    @GetMapping("/dashboard/reportes")
    public String verReportes(
            @RequestParam(required = false) Integer stationId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        int pageSize = 20;

        // Usamos el DTO que ya tiene toda la información enriquecida
        List<ReporteRecordDTO> allFiltered = dbManager.getReporteRecords(stationId, fechaDesde, fechaHasta);
        // La consulta a la BD ya ordena por fecha descendente, no es necesario ordenar aquí.

        int totalPages = (int) Math.ceil((double) allFiltered.size() / pageSize);
        int fromIndex = Math.min(page * pageSize, allFiltered.size());
        int toIndex = Math.min(fromIndex + pageSize, allFiltered.size());

        List<ReporteRecordDTO> registrosPaginados = allFiltered.subList(fromIndex, toIndex);
        List<StationModel> estaciones = dbManager.getStations();

        model.addAttribute("registros", registrosPaginados);
        model.addAttribute("estaciones", estaciones);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("stationId", stationId);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        return "reportes";
    }

    /*@GetMapping("/dashboard/reportes")
    public String verReportes(
            @RequestParam(required = false) Integer stationId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            Model model
    ) {
        List<RecordModel> registros = dbManager.getFilteredRecords(stationId, fechaDesde, fechaHasta);
        List<StationModel> estaciones = dbManager.getStations();

        model.addAttribute("registros", registros);
        model.addAttribute("estaciones", estaciones);
        return "reportes";
    }*/


    @GetMapping("/api/reportes/descargar")
    public void descargarReporte(
            @RequestParam(required = false) Integer stationId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(defaultValue = "csv") String formato,
            HttpServletResponse response) throws IOException {

        List<ReporteRecordDTO> registros = dbManager.getReporteRecords(stationId, fechaDesde, fechaHasta);

        switch (formato.toLowerCase()) {
            case "pdf":
                generarPdf(registros, response);
                break;
            case "xlsx":
                generarXlsx(registros, response);
                break;
            default:
                generarCsv(registros, response);
                break;
        }
    }

    private void generarCsv(List<ReporteRecordDTO> registros, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=reporte.csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Estacion,Sensor,Tipo,Valor,Unidad,Fecha");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (ReporteRecordDTO r : registros) {
                writer.printf("\"%s\",\"%s\",\"%s\",%.2f,\"%s\",\"%s\"%n",
                        r.getNombreEstacion(),
                        r.getModeloSensor(),
                        r.getTipoSensor(),
                        r.getValor(),
                        r.getUnidad(),
                        sdf.format(r.getFecha()));
            }
        }
    }

    private void generarXlsx(List<ReporteRecordDTO> registros, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=reporte.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte");

            // Encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Estacion", "Sensor", "Tipo", "Valor", "Unidad", "Fecha"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Datos
            int rowNum = 1;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (ReporteRecordDTO r : registros) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getNombreEstacion());
                row.createCell(1).setCellValue(r.getModeloSensor());
                row.createCell(2).setCellValue(r.getTipoSensor());
                row.createCell(3).setCellValue(r.getValor());
                row.createCell(4).setCellValue(r.getUnidad());
                row.createCell(5).setCellValue(sdf.format(r.getFecha()));
            }

            workbook.write(response.getOutputStream());
        }
    }

    private void generarPdf(List<ReporteRecordDTO> registros, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte.pdf");

        try (Document document = new Document(PageSize.A4.rotate())) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            document.add(new Paragraph("Reporte de Registros Meteorológicos"));
            document.add(new Paragraph(" ")); // Espacio

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            String[] headers = {"Estacion", "Sensor", "Tipo", "Valor", "Unidad", "Fecha"};
            for (String header : headers) {
                table.addCell(header);
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (ReporteRecordDTO r : registros) {
                table.addCell(r.getNombreEstacion());
                table.addCell(r.getModeloSensor());
                table.addCell(r.getTipoSensor());
                table.addCell(String.format("%.2f", r.getValor()));
                table.addCell(r.getUnidad());
                table.addCell(sdf.format(r.getFecha()));
            }

            document.add(table);
        } catch (DocumentException e) {
            throw new IOException(e.getMessage());
        }
    }

    @GetMapping("/api/alertas")
    @ResponseBody
    public List<AlertaDTO> obtenerAlertas() {
        return dbManager.getAlertasActivas();
    }

    @PostMapping("/dashboard/sensor/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleSensorActivo(@PathVariable int id) {
        dbManager.toggleSensorActivo(id);
        return "redirect:/dashboard/administrar-estaciones";
    }
    @PostMapping("/api/alertas/manual")
    @ResponseBody
    public void crearAlertaManual(@RequestParam int stationId,
                                  @RequestParam int sensorId,
                                  @RequestParam double umbral,
                                  @RequestParam String mensaje) {
        // "mensaje" contiene el tipo: ALTA o BAJA
        dbManager.insertAlertRule(stationId, sensorId, mensaje, umbral);
    }

    @GetMapping("/dashboard/alertas")
    public String verAlertas(Model model) {
        List<StationModel> estaciones = dbManager.getStations();
        model.addAttribute("estaciones", estaciones);
        return "alertas";
    }


    @GetMapping("/dashboard/graficos")
    public String verGraficos(Model model) {
        List<StationModel> estaciones = dbManager.getStations();
        // Cargamos los sensores de cada estación para los menús desplegables
        for (StationModel estacion : estaciones) {
            estacion.setSensores(dbManager.getSensorsByStation(estacion.getStationId()));
        }
        model.addAttribute("estaciones", estaciones);
        return "graficos";
    }




}
