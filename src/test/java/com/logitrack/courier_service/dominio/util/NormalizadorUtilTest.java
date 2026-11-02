package com.logitrack.courier_service.dominio.util;

import com.logitrack.courier_service.config.NormalizacionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizadorUtilTest {

    private NormalizacionProperties properties;

    @BeforeEach
    void setUp() {
        properties = new NormalizacionProperties();
        properties.getDni().setRegexLimpieza("[.\\-\\s]");

        NormalizacionProperties.TelefonoProperties tel = properties.getTelefono();
        tel.setCodigoPais("54");
        tel.setPrefijoInternacional("+54");
        tel.setPrefijoMovil("+549");
        tel.setPrefijoInterurbano("0");
        tel.setPrefijoMovilLocal("15");
        tel.setAreaAmba("11");
        tel.setPrefijoAmbaConQuince("1115");
        tel.setRegexLimpieza("[.\\-\\s()]");

        NormalizadorUtil.setConfiguracion(properties);
    }

    @Test
    @DisplayName("Debe limpiar el DNI con configuración inyectada")
    void limpiarDni_ConPropiedades() {
        String resultado = NormalizadorUtil.limpiarDni("40.987.654");
        assertEquals("40987654", resultado);
    }

    @Test
    @DisplayName("Debe manejar DNI null de forma segura")
    void limpiarDni_Null() {
        assertNull(NormalizadorUtil.limpiarDni(null));
    }

    @Test
    @DisplayName("Debe manejar teléfono null o en blanco de forma segura")
    void normalizarTelefono_NullYVacio() {
        assertNull(NormalizadorUtil.normalizarTelefono(null));
        assertEquals("", NormalizadorUtil.normalizarTelefono("   "));
    }

    @Test
    @DisplayName("Debe usar StringBuilder para normalizar números locales con 15")
    void normalizarTelefono_Remover15ConStringBuilder() {
        String resultado = NormalizadorUtil.normalizarTelefono("011-15-4433-2211");
        assertEquals("+541144332211", resultado);
    }

    @Test
    @DisplayName("Debe normalizar teléfono que ya posee prefijo internacional sin signo más inicial")
    void normalizarTelefono_ComienzaCon54SinSigno() {
        String resultado = NormalizadorUtil.normalizarTelefono("54 11 4433 2211");
        assertEquals("+541144332211", resultado);
    }
}