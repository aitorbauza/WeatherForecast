package com.example.weatherforecast.model;

/**
 * Clase que representa el perfil y preferencias del usuario.
 */
public class UserPreferences {
    private String name;
    private String surname;
    private Gender gender;
    private Tolerance coldTolerance;
    private Tolerance heatTolerance;

    // Enum para el género
    public enum Gender {
        MALE, FEMALE, OTHER
    }

    // Enum para niveles de tolerancia
    public enum Tolerance {
        LOW, NORMAL, HIGH
    }

    public UserPreferences() {
        // Valores predeterminados
        this.name = "User";
        this.surname = "1234";
        this.gender = Gender.OTHER;
        this.coldTolerance = Tolerance.NORMAL;
        this.heatTolerance = Tolerance.NORMAL;
    }

    public UserPreferences(String name, String surname, Gender gender,
                           Tolerance coldTolerance, Tolerance heatTolerance) {
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.coldTolerance = coldTolerance;
        this.heatTolerance = heatTolerance;
    }

    // Getters y Setters
    public String getName() {
        return name;
    }
    public String getSurname() {
        return surname;
    }
    public Gender getGender() {
        return gender;
    }
    public Tolerance getColdTolerance() {
        return coldTolerance;
    }
    public Tolerance getHeatTolerance() {
        return heatTolerance;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    public void setColdTolerance(Tolerance coldTolerance) {
        this.coldTolerance = coldTolerance;
    }
    public void setHeatTolerance(Tolerance heatTolerance) {
        this.heatTolerance = heatTolerance;
    }

    // Método para clonar la instancia
    public UserPreferences copy() {
        return new UserPreferences(
                this.name,
                this.surname,
                this.gender,
                this.coldTolerance,
                this.heatTolerance
        );
    }

}