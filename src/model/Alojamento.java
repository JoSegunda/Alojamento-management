package model;

public class Alojamento {
    private int id;
    private String nome;
    private String cidade;
    private String codPostal;
    private int capacidade;
    private String estado; //EX: "pendente","Aprovado","recusado","incompleto"

    public Alojamento(String nome, String cidade, String codPostal, int capacidade){
        this.nome = nome;
        this.cidade = cidade;
        this.codPostal = codPostal;
        this.capacidade = capacidade;
        this.estado = "Pendente"; //Estado inicial
    }

    // Construtor ao ler a base de dados
    public Alojamento(int id,String nome, String cidade, String codPostal, int capacidade, String estado){
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.codPostal = codPostal;
        this.capacidade = capacidade;
        this.estado = estado;
    }

    // Getters e Setters
    public int getId(){return id; }
    public void setId(int id){this.id = id; }

    public String getNome(){return nome; }
    public void setNome(String Nome){this.nome = Nome; }

    public String getCidade(){return cidade; }
    public void setCidade(String cidade){this.cidade = cidade; }

    public String getCodPostal(){return codPostal; }
    public void setCodPostal(String cidade){this.codPostal = codPostal; }

    public int getCapacidade(){return capacidade; }
    public void getCapacidade(int Capacidade){this.capacidade = Capacidade; }

    public String getEstado(){return estado;}
    public void getEstado(String Estado){this.estado = Estado;}

    @Override
    public String toString(){
        return "Alojamento "+nome+"\nId: "+id+"\ncidade: "+cidade+"\nEstado: "+estado;
    }

}
