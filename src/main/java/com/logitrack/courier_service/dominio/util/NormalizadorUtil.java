package com.logitrack.courier_service.dominio.util;

import com.logitrack.courier_service.config.NormalizacionProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utilidad desacoplada para la normalización de DNI y teléfono de repartidores.
 * Utiliza propiedades dinámicas leídas de configserver-ms y optimiza las transformaciones mediante StringBuilder.
 *
 * @author Equipo LazyHawk
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class NormalizadorUtil {

    private final NormalizacionProperties normalizacionProperties;
    private static NormalizacionProperties INSTANCE_PROPS;

    @PostConstruct
    public void init() {
        INSTANCE_PROPS = this.normalizacionProperties;
    }

    public static void setConfiguracion(NormalizacionProperties properties) {
        INSTANCE_PROPS = properties;
    }

    public static NormalizacionProperties getConfiguracion() {
        return INSTANCE_PROPS;
    }

    /**
     * Limpia caracteres no numéricos del DNI según el patrón configurado.
     *
     * @param raw Cadena de DNI en formato bruto.
     * @return DNI limpio sin puntos, guiones ni espacios.
     */
    public static String limpiarDni(String raw) {
        if (raw == null) {
            return null;
        }
        String regex = (INSTANCE_PROPS != null && INSTANCE_PROPS.getDni() != null && INSTANCE_PROPS.getDni().getRegexLimpieza() != null)
                ? INSTANCE_PROPS.getDni().getRegexLimpieza()
                : "[.\\-\\s]";
        return raw.replaceAll(regex, "").trim();
    }

    /**
     * Normaliza un número telefónico al estándar internacional E.164.
     * Utiliza StringBuilder para construir el resultado sin generar objetos String inmutables intermedios.
     *
     * @param raw Número telefónico de entrada.
     * @return Teléfono normalizado con prefijo internacional.
     */
    public static String normalizarTelefono(String raw) {
        if (raw == null) {
            return null;
        }

        var telefonoProps = (INSTANCE_PROPS != null) ? INSTANCE_PROPS.getTelefono() : null;
        String regexLimpieza = (telefonoProps != null && telefonoProps.getRegexLimpieza() != null)
                ? telefonoProps.getRegexLimpieza()
                : "[.\\-\\s()]";

        String limpio = raw.replaceAll(regexLimpieza, "").trim();
        if (limpio.isBlank()) {
            return limpio;
        }

        boolean teniaSignoMas = limpio.startsWith("+");
        if (teniaSignoMas) {
            limpio = limpio.substring(1);
        }

        String codigoPais = (telefonoProps != null && telefonoProps.getCodigoPais() != null)
                ? telefonoProps.getCodigoPais()
                : "54";
        String prefijoInternacional = (telefonoProps != null && telefonoProps.getPrefijoInternacional() != null)
                ? telefonoProps.getPrefijoInternacional()
                : "+54";
        String prefijoMovil = (telefonoProps != null && telefonoProps.getPrefijoMovil() != null)
                ? telefonoProps.getPrefijoMovil()
                : "+549";
        String prefijoInterurbano = (telefonoProps != null && telefonoProps.getPrefijoInterurbano() != null)
                ? telefonoProps.getPrefijoInterurbano()
                : "0";
        String prefijoMovilNueve = "9";

        // Si comienza con código de país (ej: 54)
        if (limpio.startsWith(codigoPais)) {
            String resto = limpio.substring(codigoPais.length());
            boolean tieneNueve = resto.startsWith(prefijoMovilNueve);
            if (tieneNueve) {
                resto = resto.substring(prefijoMovilNueve.length());
            }
            if (resto.startsWith(prefijoInterurbano)) {
                resto = resto.substring(prefijoInterurbano.length());
            }
            resto = removerPrefijoQuince(resto, telefonoProps);

            StringBuilder sb = new StringBuilder(prefijoMovil.length() + resto.length());
            sb.append(tieneNueve ? prefijoMovil : prefijoInternacional)
              .append(resto);
            return sb.toString();
        }

        if (limpio.startsWith(prefijoInterurbano)) {
            limpio = limpio.substring(prefijoInterurbano.length());
        }

        limpio = removerPrefijoQuince(limpio, telefonoProps);

        if (limpio.matches("\\d{8,11}")) {
            StringBuilder sb = new StringBuilder(prefijoInternacional.length() + limpio.length());
            sb.append(prefijoInternacional).append(limpio);
            return sb.toString();
        }

        if (teniaSignoMas) {
            StringBuilder sb = new StringBuilder(1 + limpio.length());
            sb.append("+").append(limpio);
            return sb.toString();
        }

        return limpio;
    }

    /**
     * Remueve el prefijo 15 de telefonía móvil local argentina utilizando StringBuilder.delete.
     *
     * @param numero        Número telefónico nacional en análisis.
     * @param telefonoProps Propiedades de telefonía configuradas.
     * @return Número sin el prefijo local 15.
     */
    private static String removerPrefijoQuince(String numero, NormalizacionProperties.TelefonoProperties telefonoProps) {
        if (numero == null) {
            return null;
        }

        String prefijoAmba15 = (telefonoProps != null && telefonoProps.getPrefijoAmbaConQuince() != null)
                ? telefonoProps.getPrefijoAmbaConQuince()
                : "1115";
        String areaAmba = (telefonoProps != null && telefonoProps.getAreaAmba() != null)
                ? telefonoProps.getAreaAmba()
                : "11";
        String prefijo15 = (telefonoProps != null && telefonoProps.getPrefijoMovilLocal() != null)
                ? telefonoProps.getPrefijoMovilLocal()
                : "15";

        if (numero.length() == 12 && numero.matches("\\d+")) {
            if (numero.startsWith(prefijoAmba15)) {
                StringBuilder sb = new StringBuilder(10);
                sb.append(areaAmba).append(numero.substring(prefijoAmba15.length()));
                return sb.toString();
            }
            if (prefijo15.equals(numero.substring(3, 5))) {
                return new StringBuilder(numero).delete(3, 5).toString();
            }
            if (prefijo15.equals(numero.substring(4, 6))) {
                return new StringBuilder(numero).delete(4, 6).toString();
            }
        }
        return numero;
    }
}