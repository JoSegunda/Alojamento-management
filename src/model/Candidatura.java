package model;

import java.time.LocalDate;

public class Candidatura {
    private int id;
    private int alojamentoId; // Chave estrangeira, referência a tabela alojamento
    private int candidatoId; // Chave estrangeira, referência a tabela candidato
    private LocalDate dataCandidatura;
    public enum EstadoCandidatura { SUBMETIDA, EM_ANALISE, ACEITE, REJEITADA, PENDENTE }
    private EstadoCandidatura estado;

    //Construtor essencial
    public Candidatura(int alojamentoId, int candidatoId){
        this.alojamentoId = alojamentoId;
        this.candidatoId= candidatoId;
        this.dataCandidatura = LocalDate.now();
        this.estado = EstadoCandidatura.SUBMETIDA;
    }
    //Construtor Completo
    public Candidatura(int id, int alojamentoId, int candidatoId, LocalDate dataCandidatura,
                       EstadoCandidatura estado){
        this.id = id;
        this.alojamentoId = alojamentoId;
        this.candidatoId = candidatoId;
        this.dataCandidatura = dataCandidatura;
        this.estado = estado;
    }

    public boolean isAtiva() {
        return this.estado == EstadoCandidatura.SUBMETIDA ||
                this.estado == EstadoCandidatura.EM_ANALISE;
    }

    //Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAlojamentoId() { return alojamentoId; }
    public void setAlojamentoId(int alojamentoId) { this.alojamentoId = alojamentoId; }

    public int getCandidatoId() { return candidatoId; }
    public void setCandidatoId(int candidatoId) { this.candidatoId = candidatoId; }

    public LocalDate getDataCandidatura() { return dataCandidatura; }
    public void setDataCandidatura(LocalDate dataCandidatura) { this.dataCandidatura = dataCandidatura; }

    public EstadoCandidatura getEstado() { return estado; }
    public void setEstado(EstadoCandidatura estado) { this.estado = estado; }

    // Métodos de negócio para transições de estado
    public void colocarEmAnalise() {
        this.estado = EstadoCandidatura.EM_ANALISE;
    }

    public void aceitar() {
        this.estado = EstadoCandidatura.ACEITE;
    }

    public void rejeitar() {
        this.estado = EstadoCandidatura.REJEITADA;
    }


    @Override
    public String toString(){
        return "Id da candidatura: " + id +
                "\nId do alojamento: " + alojamentoId +
                "\nId do Candidato: " + candidatoId +
                "\nData: " + dataCandidatura +
                "\nEstado da candidatura: " + estado;
    }
}
