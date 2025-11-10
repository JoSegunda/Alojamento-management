package model;

import java.time.LocalDateTime;

public class Candidatura {
    private int id;
    private int alojamentoId; // Chave estrangeira, referência a tabela alojamento
    private int candidatoId; // Chave estrangeira, referência a tabela candidato
    private LocalDateTime dataCandidatura;
    private String estado; //Ex: submetida, rejeitada, aceite,

    //Construtor essencial
    public Candidatura(int alojamentoId, int candidatoId){
        this.alojamentoId = alojamentoId;
        this.candidatoId= candidatoId;
        this.dataCandidatura = LocalDateTime.now();
        this.estado = "Submetida";
    }
    //Construtor Completo
    public Candidatura(int id, int alojamentoId, int candidatoId, LocalDateTime dataCandidatura, String estado){
        this.id = id;
        this.alojamentoId = alojamentoId;
        this.candidatoId = candidatoId;
        this.dataCandidatura = dataCandidatura;
        this.estado = estado;
    }

    //Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAlojamentoId() { return alojamentoId; }
    public void setAlojamentoId(int alojamentoId) { this.alojamentoId = alojamentoId; }

    public int getCandidatoId() { return candidatoId; }
    public void setCandidatoId(int candidatoId) { this.candidatoId = candidatoId; }

    public LocalDateTime getDataCandidatura() { return dataCandidatura; }
    public void setDataCandidatura(LocalDateTime dataCandidatura) { this.dataCandidatura = dataCandidatura; }

    public String getStatus() { return estado; }
    public void setStatus(String estado) { this.estado = estado; }

    @Override
    public String toString(){
        return "Id da candidatura: "+id+"\nId do alojamento: "+alojamentoId+ """
                \nId do Candidato:\s""" +candidatoId+"\nEstado da candidatura: "+estado;
    }
}
