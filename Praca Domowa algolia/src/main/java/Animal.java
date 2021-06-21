public class Animal {
    private String objectID;
    private String name;
   private String type;
   private double weight;

    @Override
    public String toString() {
        return "Animal{" +
                "objectID='" + objectID + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", weight=" + weight +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getobjectID() {
        return objectID;
    }

    public void setobjectID(String objectID) {
        this.objectID =objectID;
    }
}
