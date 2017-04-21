package br.edu.ffb.pedro.aulafacilprofessor.events;

public class MessageEvent {
    public static final String UPDATE_STUDENTS_LIST = "Atualizar a lista de estudantes";
    public final String message;
    public MessageEvent(String message) {
        this.message = message;
    }
}
