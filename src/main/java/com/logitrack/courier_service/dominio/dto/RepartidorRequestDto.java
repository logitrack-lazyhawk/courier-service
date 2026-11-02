package com.logitrack.courier_service.dominio.dto;

import com.logitrack.courier_service.dominio.enumerador.EstadoRepartidor;
import com.logitrack.courier_service.dominio.util.NormalizadorUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Objeto de transferencia utilizado para recibir la petición de registro o edición de un repartidor.
 * Aplica limpieza previa en DNI y normalización de formato internacional E.164 desacopladas.
 *
 * @author Equipo LazyHawk
 * @version 1.0.0
 */
public record RepartidorRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El DNI es obligatorio")
        @Size(max = 8, message = "El DNI no puede superar los 8 dígitos")
        @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe contener 7 u 8 dígitos numéricos (sin puntos ni espacios)")
        String dni,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(
                regexp = "^\\+54\\d{8,11}$",
                message = "El teléfono debe tener formato internacional E.164 (+54 seguido del código de área sin 0 y número sin 15, con longitud total entre 10 y 13 dígitos)"
        )
        String telefono,

        @NotBlank(message = "El tipo de vehículo es obligatorio")
        @Size(max = 50, message = "El tipo de vehículo no puede superar los 50 caracteres")
        String tipoVehiculo,

        EstadoRepartidor estado
) {
    public RepartidorRequestDto {
        if (dni != null) {
            dni = NormalizadorUtil.limpiarDni(dni);
        }
        if (telefono != null) {
            telefono = NormalizadorUtil.normalizarTelefono(telefono);
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }

    public RepartidorRequestDto(String nombre, String dni, String email, String telefono, String tipoVehiculo) {
        this(nombre, dni, email, telefono, tipoVehiculo, EstadoRepartidor.ACTIVO);
    }
}