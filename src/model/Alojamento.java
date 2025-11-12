package model;

public class Alojamento {
    private int id;
    private String nome;
    private String cidade;
    private boolean active;
    private int capacidade;
    public enum EstadoAlojamento{ ATIVO, EM_OBRAS, FECHADO };
    private EstadoAlojamento estado;

    public Alojamento(String nome, String cidade, int capacidade){
        this.nome = nome;
        this.cidade = cidade;
        this.capacidade  = capacidade;
        this.active = true;
        this.estado = EstadoAlojamento.ATIVO; // estado inicial
    }

    // Construtor ao ler a base de dados
    public Alojamento(int id,String nome, String cidade,boolean active, int capacidade, EstadoAlojamento estado){
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.active = active;
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

    public boolean isActive(){return active; }
    public void setActive(boolean active){this.active = active; }

    public int getCapacidade(){return capacidade; }
    public void setCapacidade(int Capacidade){this.capacidade = Capacidade; }

    public EstadoAlojamento getEstado(){return estado;}
    public void setEstado(EstadoAlojamento estado){this.estado = estado;}

    @Override
    public String toString(){
        return "Alojamento " + nome +
                "\nId: " + id +
                "\nCidade: " + cidade +
                "\nCapacidade: " + capacidade +
                "\nEstado: " + estado +
                "\nAtivo: " + active;
    }

}
