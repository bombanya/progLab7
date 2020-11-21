package please.help.organizationBuilding;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Organization implements Comparable<Organization>, Serializable {

    private static final long serialVersionUID = 20200916L;
    private Long id;
    private LocalDateTime creationDate;

    private final String name;
    private final Coordinates coordinates;
    private final double annualTurnover;
    private final OrganizationType type;
    private final Address officialAddress;
    private String owner;

    public Organization(String name, Coordinates coordinates,
                        double annualTurnover, OrganizationType type, Address officialAddress ){
        this.name = name;
        this.coordinates = coordinates;
        this.annualTurnover = annualTurnover;
        this.type = type;
        this.officialAddress = officialAddress;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates(){
        return coordinates;
    }

    public double getAnnualTurnover() {
        return annualTurnover;
    }

    public OrganizationType getType() {
        return type;
    }

    public Address getOfficialAddress() {
        return officialAddress;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Long getId() {
        return id;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public String[] getAllFields(){
        return new String[]{
                getName(), String.valueOf(getCoordinates().getX()),
                String.valueOf(getCoordinates().getY()), String.valueOf(getAnnualTurnover()),
                getType().toString(), getOfficialAddress().getStreet(), getOfficialAddress().getZipCode()
        };
    }

    public int[] getLengthsOfFields(){
        int[] lengths = new int[10];
        lengths[0] = id.toString().length();
        lengths[1] = name.length();
        lengths[2] = Integer.toString(coordinates.getX()).length();
        lengths[3] = Integer.toString(coordinates.getY()).length();
        lengths[4] = creationDate.toString().length();
        lengths[5] = Double.toString(annualTurnover).length();
        lengths[6] = type.toString().length();
        lengths[7] = officialAddress.getStreet().length();
        lengths[8] = officialAddress.getZipCode().length();
        lengths[9] = owner.length();
        return lengths;
    }

    @Override
    public int compareTo(Organization other) {
        return Double.compare(annualTurnover, other.getAnnualTurnover());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        return ((Organization) other).getId().equals(this.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate);
    }

    @Override
    public String toString() {
        return "Organization{ " + "id = " + id + "; creationDate = " + creationDate
                + "; name = " + name + "; coordinates = " + coordinates
                + "; annualTurnover = " + annualTurnover + "; type = " + type
                + "; officialAddress = " + officialAddress + "; owner = " + owner + " }";
    }
}
