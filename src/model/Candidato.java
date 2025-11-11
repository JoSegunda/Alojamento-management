package model;

public class Candidato {
    private int id;
    private String nome;
    private String email;
    private String telefone;
    private String sexo;
    private String curso;

    // Construtor para novo candidato
    public Candidato(String nome, String email, String telefone, String sexo){
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    //Construtor completo
    public Candidato(int id, String nome, String email, String telefone, String sexo, String curso){
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.sexo = sexo;
        this.curso = curso;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getCurso() { return telefone; }
    public void setCurso(String curso) { this.curso = curso; }

    @Override
    public String toString(){
        return "ID do candidato\n"+id+"\nNome do candidato: "+nome+"\nemail: "+email;
    }

}
