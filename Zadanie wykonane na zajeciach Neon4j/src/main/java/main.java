import static org.neo4j.driver.internal.types.InternalTypeSystem.TYPE_SYSTEM;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.util.Pair;
public class main {

    public static void main(String[] args) {
        try (Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "neo4jpassword"));
             Session session = driver.session()) {
            Scanner scan = new Scanner(System.in);
            session.writeTransaction(main::deleteEverything);
            while (true) {
                int operation = showMainOperation(scan);
                scan.nextLine();
                if (operation == 1) {
                    System.out.println("Podaj nazwe typ i wage zwierzecia które chcesz dodać");
                    String name =scan.nextLine();
                    String type=scan.nextLine();
                    double weight=scan.nextDouble();
                    scan.nextLine();
                    System.out.println("Podaj numer i sektor klatki");
                    int number =scan.nextInt();
                    scan.nextLine();
                    String sector=scan.nextLine();
                    session.writeTransaction(tx -> createAnimal(tx,name,type,weight));
                    session.writeTransaction(tx -> createCage(tx,number,sector));
                    session.writeTransaction(tx -> createRelationship(tx,name,number));
                } else if (operation == 2) {
                    System.out.println("Podaj id zwierzecia ktore chcesz modyfikowac");
                    int id = scan.nextInt();
                    scan.nextLine();
                    System.out.println("Podaj nowy wage");
                    double weight = scan.nextDouble();
                    scan.nextLine();
                    session.writeTransaction(tx -> modify(tx,id,weight));
                } else if (operation == 3) {
                    System.out.println("Podaj nazwe zwierzecia którego chcesz usunąć");
                    String name = scan.nextLine();
                    session.writeTransaction(tx -> delete(tx,name));
                } else if (operation == 4) {
                    System.out.println("Podaj id zwierzecia którego chcesz wyszukac");
                    int id = scan.nextInt();
                    scan.nextLine();
                    session.writeTransaction(tx -> getByID(tx,id));
                } else if (operation == 5) {
                    System.out.println("Podaj rodaj zwierząt które chcesz wyszukac");
                    String type = scan.nextLine();
                    session.writeTransaction(tx -> getByType(tx,type));
                } else if (operation == 6) {
                    session.writeTransaction(main::avg);
                } else if (operation == 7) {
                    session.writeTransaction(main::readAllNodes);
                } else if (operation == 0) {
                    System.out.println("Zamkniecie aplikacji");
                    return;
                } else {
                    System.out.println("Wprowadzon złą wartośc");
                    operation = showMainOperation(scan);
                    scan.nextLine();
                }

            }
        }catch(RuntimeException ex){
            System.out.println(ex);
        }
    }

    public static int showMainOperation(Scanner scan){
        System.out.println("Wybierz rodzaj operaccji:");
        System.out.println("0.zamknij");
        System.out.println("1.zapisywanie");
        System.out.println("2.aktualizacja");
        System.out.println("3.kasowanie");
        System.out.println("4.pobieranie po id");
        System.out.println("5.pobieranie złożone");
        System.out.println("6.przetwarzanie");
        System.out.println("7.wyswietlenie");
        return scan.nextInt();
    }

    public static Result createAnimal(Transaction transaction, String animalName,String animalType,double weight) {
        String command = "CREATE (:Zwierze {name:$animalName,type:$animalType,weight:$animalWeight})";
        System.out.println("Executing: " + command);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("animalName", animalName);
        parameters.put("animalType", animalType);
        parameters.put("animalWeight", weight);
        return transaction.run(command, parameters);
    }
    public static Result createCage(Transaction transaction, int cageNumber,String cageSector) {
        String command = "CREATE (:Klatka {number:$cageNumber,sector:$cageSector})";
        System.out.println("Executing: " + command);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cageNumber", cageNumber);
        parameters.put("cageSector", cageSector);
        return transaction.run(command, parameters);
    }
    public static Result createRelationship(Transaction transaction, String animalName, int cageNumber) {
        String command =
                "MATCH (z:Zwierze),(k:Klatka) " +
                        "WHERE z.name = $animalName AND k.number = $cageNumber "
                        + "CREATE (z)-[r:JEST_W]->(k)" +
                        "RETURN type(r)";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("animalName", animalName);
        parameters.put("cageNumber", cageNumber);
        System.out.println("Executing: " + command);
        return transaction.run(command, parameters);
    }
    public static Result readAllRealtionships(Transaction transaction) {
        String command =
                "MATCH ()-[r]->()" +
                        "RETURN r;";
        System.out.println("Executing: " + command);
        Result result = transaction.run(command);
        while (result.hasNext()) {
            Record record = result.next();
            List<Pair<String, Value>> fields = record.fields();
            for (Pair<String, Value> field : fields)
                printField(field);
        }
        return result;
    }
    public static Result readAllNodes(Transaction transaction) {
        String command =
                "MATCH (n)" +
                        "RETURN n";
        System.out.println("Executing: " + command);
        Result result = transaction.run(command);
        while (result.hasNext()) {
            Record record = result.next();
            List<Pair<String, Value>> fields = record.fields();
            for (Pair<String, Value> field : fields)
                printField(field);
        }
        return result;
    }

    public static void printField(Pair<String, Value> field) {
        System.out.println("field = " + field);
        Value value = field.value();
        if (TYPE_SYSTEM.NODE().isTypeOf(value))
            printNode(field.value().asNode());
        else if (TYPE_SYSTEM.RELATIONSHIP().isTypeOf(value))
            printRelationship(field.value().asRelationship());
        else
            throw new RuntimeException();
    }

    public static void printNode(Node node) {
        System.out.println("id = " + node.id());
        System.out.println("labels = " + " : " + node.labels());
        System.out.println("asMap = " + node.asMap());
    }

    public static void printRelationship(Relationship relationship) {
        System.out.println("id = " + relationship.id());
        System.out.println("type = " + relationship.type());
        System.out.println("startNodeId = " + relationship.startNodeId());
        System.out.println("endNodeId = " + relationship.endNodeId());
        System.out.println("asMap = " + relationship.asMap());
    }

    public static Result deleteEverything(Transaction transaction) {
        String command = "MATCH (n) DETACH DELETE n";
        System.out.println("Executing: " + command);
        return transaction.run(command);
    }

    public static Result delete(Transaction transaction,String name) {
        String command = "MATCH (n:Zwierze {name:$animalName}) DETACH DELETE n";
        System.out.println("Executing: " + command);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("animalName", name);
        return transaction.run(command,parameters);
    }

    public static Result getByType(Transaction transaction,String type) {
        String command = "MATCH (Zwierze {type: $animalType}) RETURN Zwierze";
        System.out.println("Executing: " + command);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("animalType", type);
        Result result = transaction.run(command,parameters);
        while (result.hasNext()) {
            Record record = result.next();
            List<Pair<String, Value>> fields = record.fields();
            for (Pair<String, Value> field : fields)
                printField(field);
        }
        return result;
    }

    public static Result getByID(Transaction transaction,int id) {
        String command = "MATCH (Zwierze) WHERE ID(Zwierze) = $id RETURN Zwierze";
        System.out.println("Executing: " + command);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        Result result = transaction.run(command,parameters);
        while (result.hasNext()) {
            Record record = result.next();
            List<Pair<String, Value>> fields = record.fields();
            for (Pair<String, Value> field : fields)
                printField(field);
        }
        return result;
    }

    public static Result modify(Transaction transaction,int id,double weight) {
        String command = "MATCH (Zwierze) WHERE ID(Zwierze) = $id SET Zwierze.weight=$weight";
        System.out.println("Executing: " + command);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        parameters.put("weight", weight);
        Result result = transaction.run(command,parameters);
        while (result.hasNext()) {
            Record record = result.next();
            List<Pair<String, Value>> fields = record.fields();
            for (Pair<String, Value> field : fields)
                printField(field);
        }
        return result;
    }

    public static Result avg(Transaction transaction) {
        String command = "MATCH (Zwierze) RETURN avg(Zwierze.weight)";
        System.out.println("Executing: " + command);
        Result result = transaction.run(command);
        System.out.println(result.next().fields().get(0));
        return result;
    }
}
