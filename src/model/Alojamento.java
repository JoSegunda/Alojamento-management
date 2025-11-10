package model;

public class Alojamento {
    private int id;
    private String morada;
    private int capacidade;
    private String estado; //EX: "pendente","Aprovado","recusado","incompleto"

    public Alojamento(String morada, int capacidade){
        this.morada = morada;
        this.capacidade = capacidade;
        this.estado = "Pendente"; //Estado inicial
    }

    // Construtor ao ler a base de dados
    public Alojamento(int id, String morada, int capacidade, String estado){
        this.id = id;
        this.morada = morada;
        this.capacidade = capacidade;
        this.estado = "Pendente"; //Estado inicial
    }

    // Getters e Setters
}
