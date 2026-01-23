package it.unisannio.buildgenerator.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CPU.class, name = "CPU"),
        @JsonSubTypes.Type(value = GPU.class, name = "GPU"),
        @JsonSubTypes.Type(value = RAM.class, name = "RAM"),
        @JsonSubTypes.Type(value = PSU.class, name = "PSU"),
        @JsonSubTypes.Type(value = MotherBoard.class, name = "MOTHERBOARD"),
        @JsonSubTypes.Type(value = Case.class, name = "CASE"),
        @JsonSubTypes.Type(value = Cooler.class, name = "COOLER"),
        @JsonSubTypes.Type(value = SSD.class, name = "SSD")
})
public abstract class Part {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    protected String model;
    protected String brand;
    protected float price;

    public Long getId(){
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setPrice(float price) {
        this.price = price;
    }

    public abstract String getModel();
    public abstract String getBrand();
    public abstract float getPrice();
    public abstract float getPerformance();
    public abstract int compare(Part other);
    public abstract int comparePrice(Part other);
    public abstract String displayInfo();

}