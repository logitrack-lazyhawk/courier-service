package com.logitrack.courier_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de normalización para courier-service cargadas externamente desde configserver-ms.
 * Desacopla las configuraciones de formato y no posee valores hardcodeados en código.
 *
 * @author Equipo LazyHawk
 * @version 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "app.normalizacion")
@Getter
@Setter
public class NormalizacionProperties {

    private TelefonoProperties telefono = new TelefonoProperties();
    private DniProperties dni = new DniProperties();

    @Getter
    @Setter
    public static class TelefonoProperties {
        private String codigoPais;
        private String prefijoInternacional;
        private String prefijoMovil;
        private String prefijoInterurbano;
        private String prefijoMovilLocal;
        private String areaAmba;
        private String prefijoAmbaConQuince;
        private String regexLimpieza;
    }

    @Getter
    @Setter
    public static class DniProperties {
        private String regexLimpieza;
    }
}