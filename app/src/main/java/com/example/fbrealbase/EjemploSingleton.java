package com.example.fbrealbase;

public class EjemploSingleton {

    private int valor;
    private static EjemploSingleton instance = null;

    private EjemploSingleton() {
        valor = 1;
    }

    public static EjemploSingleton getInstance(){
        if(instance == null){
            instance = new EjemploSingleton();
        }
        return instance;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }
}
