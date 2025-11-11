package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Candidato {
    private int id;
    private String nome;
    private String email;
    private String telefone;
    private Sexo sexo;
    private  EstadoCandidato estado;

    private LocalDate dataRegisto;
    private String curso;

    public enum Sexo { MASCULINO, FEMININO, OUTRO }
    public enum EstadoCandidato { ATIVO, SUSPENSO, REMOVIDO }



    // Construtor para novo candidato
    public Candidato(String nome, String email, String telefone, Sexo sexo, String curso){
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.sexo = sexo;
        this.curso = curso;
        this.dataRegisto = LocalDate.now();
        this.estado = EstadoCandidato.ATIVO; //Estado inicial
    }

    //Construtor completo
    public Candidato(int id, String nome, String email, String telefone, Sexo sexo, String curso,
                     LocalDate dataRegisto, EstadoCandidato estado){
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.sexo = sexo;
        this.curso = curso;
        this.dataRegisto = LocalDate.now();
        this.estado = estado;
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

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public EstadoCandidato getEstado() { return estado; }
    public void setEstado(EstadoCandidato estado) { this.estado = estado; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public LocalDate getDataRegisto() { return dataRegisto; }
    public void setDataRegisto(LocalDate dataRegisto) { this.dataRegisto = dataRegisto; }

    // Méthod para suspender candidato
    public void suspender() {
        this.estado = EstadoCandidato.SUSPENSO;
    }
    //Méthod para ativar candidato
    public void ativar() {
        this.estado = EstadoCandidato.ATIVO;
    }

    @Override
    public String toString(){
        return "ID do candidato: " + id +
                "\nNome do candidato: " + nome +
                "\nEmail: " + email +
                "\nTelefone: " + telefone +
                "\nCurso: " + curso +
                "\nEstado: " + estado;
    }

}
