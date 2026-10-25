package com.logitrack.curier_service.dominio.dto;

import com.logitrack.curier_service.dominio.enumerador.EstadoRepartidor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RepartidorRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El DNI es obligatorio")
        @Size(max = 8, message = "El DNI no puede superar los 8 dígitos")
        @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe contener 7 u 8 dígitos numéricos (sin puntos ni espacios)")
        String dni,

        // NUEVO CAMPO: Validación de formato de email
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
            dni = limpiarDni(dni);
        }
        if (telefono != null) {
            telefono = normalizarTelefono(telefono);
        }
        // Opcional: Normalizar el email a minúsculas para evitar duplicados lógicos
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }

    // Constructor de conveniencia actualizado
    public RepartidorRequestDto(String nombre, String dni, String email, String telefono, String tipoVehiculo) {
        this(nombre, dni, email, telefono, tipoVehiculo, EstadoRepartidor.ACTIVO);
    }

    private static String limpiarDni(String raw) {
        return raw.replaceAll("[.\\-\\s]", "").trim();
    }

    private static String normalizarTelefono(String raw) {
        String limpio = raw.replaceAll("[.\\-\\s()]", "").trim();
        if (limpio.isBlank()) {
            return limpio;
        }

        boolean teniaSignoMas = limpio.startsWith("+");
        if (teniaSignoMas) {
            limpio = limpio.substring(1);
        }

        if (limpio.startsWith("54")) {
            String resto = limpio.substring(2);
            boolean tieneNueve = resto.startsWith("9");
            if (tieneNueve) {
                resto = resto.substring(1);
            }
            if (resto.startsWith("0")) {
                resto = resto.substring(1);
            }
            resto = removerPrefijoQuince(resto);
            return "+" + (tieneNueve ? "549" : "54") + resto;
        }

        if (limpio.startsWith("0")) {
            limpio = limpio.substring(1);
        }

        limpio = removerPrefijoQuince(limpio);

        if (limpio.matches("\\d{8,11}")) {
            return "+54" + limpio;
        }

        return teniaSignoMas ? "+" + limpio : limpio;
    }

    private static String removerPrefijoQuince(String numero) {
        if (numero == null) {
            return null;
        }
        if (numero.length() == 12 && numero.matches("\\d+")) {
            if (numero.startsWith("1115")) {
                return "11" + numero.substring(4);
            }
            if ("15".equals(numero.substring(3, 5))) {
                return numero.substring(0, 3) + numero.substring(5);
            }
            if ("15".equals(numero.substring(4, 6))) {
                return numero.substring(0, 4) + numero.substring(6);
            }
        }
        return numero;
    }
}
