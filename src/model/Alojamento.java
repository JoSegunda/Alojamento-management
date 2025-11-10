package model;

public class Alojamento {
    private int id;
    private String nome;
    private String morada;
    private int capacidade;
    private String estado; //EX: "pendente","Aprovado","recusado","incompleto"

    public Alojamento(String nome, String morada, int capacidade){
        this.nome = nome;
        this.morada = morada;
        this.capacidade = capacidade;
        this.estado = "Pendente"; //Estado inicial
    }

    // Construtor ao ler a base de dados
    public Alojamento(int id,String nome, String morada, int capacidade, String estado){
        this.id = id;
        this.nome = nome;
        this.morada = morada;
        this.capacidade = capacidade;
        this.estado = estado;
    }

    // Getters e Setters
    public int getId(){return id; }
    public void setId(int id){this.id = id; }

    public String getNome(){return nome; }
    public void setNome(String Nome){this.nome = Nome; }

    public String getMorada(){return morada; }
    public void setMorada(String Morada){this.morada = Morada; }

    public int getCapacidade(){return capacidade; }
    public void getCapacidade(int Capacidade){this.capacidade = Capacidade; }

    public String getEstado(){return estado;}
    public void getEstado(String Estado){this.estado = Estado;}

    @Override
    public String toString(){
        return "Alojamento "+nome+"\nId: "+id+"\nMorada: "+morada+"\nEstado: "+estado;
    }

}
