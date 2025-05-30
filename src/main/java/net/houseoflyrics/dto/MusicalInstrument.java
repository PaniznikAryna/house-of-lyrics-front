package net.houseoflyrics.dto;

public class MusicalInstrument {
    private int id;
    private String instrument;

    public MusicalInstrument(int id, String instrument) {
        this.id = id;
        this.instrument = instrument;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }
}
