package com.michael.aadproject;

public class User {
    private String name;
    private String surname;
    private String email;

    public User() {
        // mandatory default constructor for DataSnapshot calls
    }

    public User (String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public String getName() {
        return formatName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return formatName(surname);
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String formatName(String name) {
        String result = "";
        String[] splitName = name.split(" ");
        int arrayLength = splitName.length;
        for (int i = 0; i < arrayLength; i++) {
            System.out.println("i = " + i);
            String first = splitName[i].substring(0, 1).toUpperCase();
            String remaining = splitName[i].substring(1).toLowerCase();
            if (i == (arrayLength - 1)) {
                result += (first + remaining);
            } else {
                result += (first + remaining + " ");
            }
        }
        return result;
    }
}
