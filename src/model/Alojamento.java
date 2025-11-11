package model;

public class Alojamento {
    private int id;
    private String nome;
    private String cidade;
    private boolean activo;
    private int capacidade;
    public enum estado{ PENDENTE, APROVADO, RECUSADO, INCOMPLETO };

    public Alojamento(String nome, String cidade, int capacidade, boolean activo){
        this.nome = nome;
        this.cidade = cidade;
        this.activo = activo;
        this.capacidade = capacidade;
        //this.estado = "Pendente"; //Estado inicial
    }

    // Construtor ao ler a base de dados
    public Alojamento(int id,String nome, String cidade,boolean activo, int capacidade, String estado){
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.activo = activo;
        this.capacidade = capacidade;
        //this.estado = estado;
    }

    // Getters e Setters
    public int getId(){return id; }
    public void setId(int id){this.id = id; }

    public String getNome(){return nome; }
    public void setNome(String Nome){this.nome = Nome; }

    public String getCidade(){return cidade; }
    public void setCidade(String cidade){this.cidade = cidade; }

    public boolean getActivo(){return activo; }
    public void setActivo(boolean activo){this.activo = activo; }

    public int getCapacidade(){return capacidade; }
    public void setCapacidade(int Capacidade){this.capacidade = Capacidade; }

    public String getEstado(){return estado;}
    public void setEstado(String estado){this.estado = estado;}

    @Override
    public String toString(){
        return "Alojamento "+nome+"\nCódigo postal: "+codPostal+ "\nId: "+id+"\ncidade: "+cidade+"\nEstado: "+estado;
    }

}
