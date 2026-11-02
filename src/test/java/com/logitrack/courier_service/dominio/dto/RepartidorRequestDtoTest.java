package com.logitrack.courier_service.dominio.dto;

import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RepartidorRequestDtoTest {

    @Test
    @DisplayName("Debe limpiar el DNI removiendo puntos, guiones y espacios")
    void limpiarDni_Exitoso() {
        RepartidorRequestDto dto = new RepartidorRequestDto(
                "Carlos Tevez",
                " 35.123-456 ",
                "carlos@email.com",
                "+541123456789",
                "MOTO"
        );
        assertEquals("35123456", dto.dni());
    }

    @Test
    @DisplayName("Debe normalizar email a minúsculas y trim")
    void normalizarEmail_Exitoso() {
        RepartidorRequestDto dto = new RepartidorRequestDto(
                "Carlos Tevez",
                "35123456",
                "  Carlos.Tevez@Email.COM  ",
                "+541123456789",
                "MOTO"
        );
        assertEquals("carlos.tevez@email.com", dto.email());
    }

    @Test
    @DisplayName("Debe normalizar teléfono con 0 y 15 para AMBA (11)")
    void normalizarTelefono_AmbaCon15_Exitoso() {
        RepartidorRequestDto dto = new RepartidorRequestDto(
                "Carlos Tevez",
                "35123456",
                "carlos@email.com",
                "011 15 1234-5678",
                "MOTO"
        );
        assertEquals("+541112345678", dto.telefono());
    }

    @Test
    @DisplayName("Debe normalizar teléfono AMBA que comienza con 1115")
    void normalizarTelefono_AmbaInicio1115_Exitoso() {
        RepartidorRequestDto dto = new RepartidorRequestDto(
                "Carlos Tevez",
                "35123456",
                "carlos@email.com",
                "111512345678",
                "MOTO"
        );
        assertEquals("+541112345678", dto.telefono());
    }

    @Test
    @DisplayName("Debe normalizar teléfono del interior (área 3 dígitos: 351) con 15")
    void normalizarTelefono_InteriorArea3Digitos_Exitoso() {
        RepartidorRequestDto dto = new RepartidorRequestDto(
                "Carlos Tevez",
                "35123456",
                "carlos@email.com",
                "0351 15 1234567",
                "MOTO"
        );
        assertEquals("+543511234567", dto.telefono());
    }

    @Test
    @DisplayName("Debe normalizar teléfono del interior (área 4 dígitos: 2966) con 15")
    void normalizarTelefono_InteriorArea4Digitos_Exitoso() {
        RepartidorRequestDto dto = new RepartidorRequestDto(
                "Carlos Tevez",
                "35123456",
                "carlos@email.com",
                "02966 15 123456",
                "MOTO"
        );
        assertEquals("+542966123456", dto.telefono());
    }

    @Test
    @DisplayName("Debe preservar prefijo internacional con 9 (+549)")
    void normalizarTelefono_PrefijoMovil549_Exitoso() {
        RepartidorRequestDto dto = new RepartidorRequestDto(
                "Carlos Tevez",
                "35123456",
                "carlos@email.com",
                "+54 9 11 1234-5678",
                "MOTO"
        );
        assertEquals("+5491112345678", dto.telefono());
    }

    @Test
    @DisplayName("Constructor de conveniencia asigna EstadoRepartidor.ACTIVO")
    void constructorConveniencia_AsignaEstadoActivo() {
        RepartidorRequestDto dto = new RepartidorRequestDto(
                "Carlos Tevez",
                "35123456",
                "carlos@email.com",
                "+541123456789",
                "MOTO"
        );
        assertEquals(EstadoRepartidor.ACTIVO, dto.estado());
    }
}