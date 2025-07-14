package org.javadominicano.cmp.dto;

public class StationDetailsDTO {
    private String nombre;
    private boolean enLinea;
    private Double temperatura;
    private Double humedad;
    private Double presion;
    private Double viento;
    private Double precipitacion;
    private Double humedadSuelo;
    private long minutosDesdeUltimaLectura;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isEnLinea() { return enLinea; }
    public void setEnLinea(boolean enLinea) { this.enLinea = enLinea; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public Double getHumedad() { return humedad; }
    public void setHumedad(Double humedad) { this.humedad = humedad; }

    public Double getPresion() { return presion; }
    public void setPresion(Double presion) { this.presion = presion; }

    public Double getViento() { return viento; }
    public void setViento(Double viento) { this.viento = viento; }

    public Double getPrecipitacion() { return precipitacion; }
    public void setPrecipitacion(Double precipitacion) { this.precipitacion = precipitacion; }

    public Double getHumedadSuelo() { return humedadSuelo; }
    public void setHumedadSuelo(Double humedadSuelo) { this.humedadSuelo = humedadSuelo; }

    public long getMinutosDesdeUltimaLectura() { return minutosDesdeUltimaLectura; }
    public void setMinutosDesdeUltimaLectura(long minutos) { this.minutosDesdeUltimaLectura = minutos; }
}
