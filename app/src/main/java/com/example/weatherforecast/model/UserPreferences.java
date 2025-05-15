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

    // Enumeración para el género
    public enum Gender {
        MALE, FEMALE, OTHER
    }

    // Enumeración para niveles de tolerancia
    public enum Tolerance {
        LOW, NORMAL, HIGH
    }

    /**
     * Constructor por defecto que establece los valores predeterminados.
     */
    public UserPreferences() {
        // Valores predeterminados
        this.name = "";
        this.surname = "";
        this.gender = Gender.OTHER;
        this.coldTolerance = Tolerance.NORMAL;
        this.heatTolerance = Tolerance.NORMAL;
    }

    /**
     * Constructor completo.
     */
    public UserPreferences(String name, String surname, Gender gender,
                           Tolerance coldTolerance, Tolerance heatTolerance) {
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.coldTolerance = coldTolerance;
        this.heatTolerance = heatTolerance;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Tolerance getColdTolerance() {
        return coldTolerance;
    }

    public void setColdTolerance(Tolerance coldTolerance) {
        this.coldTolerance = coldTolerance;
    }

    public Tolerance getHeatTolerance() {
        return heatTolerance;
    }

    public void setHeatTolerance(Tolerance heatTolerance) {
        this.heatTolerance = heatTolerance;
    }

    /**
     * Crea una copia de las preferencias actuales.
     */
    public UserPreferences copy() {
        return new UserPreferences(
                this.name,
                this.surname,
                this.gender,
                this.coldTolerance,
                this.heatTolerance
        );
    }

    /**
     * Restablece las preferencias a los valores predeterminados.
     */
    public void reset() {
        this.name = "";
        this.surname = "";
        this.gender = Gender.OTHER;
        this.coldTolerance = Tolerance.NORMAL;
        this.heatTolerance = Tolerance.NORMAL;
    }

    /**
     * Compara esta instancia con otra para determinar si son iguales.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        UserPreferences that = (UserPreferences) obj;

        if (!name.equals(that.name)) return false;
        if (!surname.equals(that.surname)) return false;
        if (gender != that.gender) return false;
        if (coldTolerance != that.coldTolerance) return false;
        return heatTolerance == that.heatTolerance;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + surname.hashCode();
        result = 31 * result + gender.hashCode();
        result = 31 * result + coldTolerance.hashCode();
        result = 31 * result + heatTolerance.hashCode();
        return result;
    }
}