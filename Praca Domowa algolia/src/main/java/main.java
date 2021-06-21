import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.algolia.search.models.indexing.Query;
import com.algolia.search.models.indexing.SearchResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class main {

    public static void save(SearchIndex<Animal> index,Random r,String name,String type,double weight){
        Animal animal = new Animal();
        animal.setobjectID(Integer.toString(Math.abs(r.nextInt(30))));
        animal.setName(name);
        animal.setType(type);
        animal.setWeight(weight);
        index.saveObject(animal);
    }

    public static void main(String[] args) throws IOException {
        SearchClient client =
                DefaultSearchClient.create("6IBTMLYWWT", "ba21c6cf1e055fe9ccca9920e98cddf3");
        Scanner scan = new Scanner(System.in);
        Random r = new Random();
        SearchIndex<Animal> index = client.initIndex("PSR", Animal.class);
        SearchIndex<UpdateAnimal> index2 = client.initIndex("PSR", UpdateAnimal.class);
        index.clearObjects();
        while (true) {
            int operation = showMainOperation(scan);
            scan.nextLine();
            if (operation == 1) {
                System.out.println("Podaj nazwe typ i wage zwierzecia które chcesz zapisac");
                String name =scan.nextLine();
                String type=scan.nextLine();
                double weight=scan.nextDouble();
                scan.nextLine();
                save(index,r,name,type,weight);
            } else if (operation == 2) {
                System.out.println("Podaj id zwierzecia ktore chcesz modyfikowac");
                int id = scan.nextInt();
                scan.nextLine();
                System.out.println("Podaj nowy wage");
                double weight = scan.nextDouble();
                scan.nextLine();
                UpdateAnimal animal1 = new UpdateAnimal();
                animal1.setObjectID(Integer.toString(id));
                animal1.setWeight(weight);
                index2.partialUpdateObject(animal1);
            } else if (operation == 3) {
                System.out.println("Podaj id zwierzecia które chcesz usunąć");
                String id = scan.nextLine();
                index.deleteObject(id);
            } else if (operation == 4) {
                System.out.println("Podaj id zwierzecia którego chcesz wyszukac");
                String id = scan.nextLine();
                System.out.println(index.getObject(id));
            } else if (operation == 5) {
                System.out.println("Podaj rodaj zwierząt które chcesz wyszukac");
                String type = scan.nextLine();
                 List<Animal> search =index.search(new Query(type)).getHits();
                for (Animal value : search) System.out.println(value);
            } else if (operation == 6) {
                double avg=0;
                List<Animal> search =index.search(new Query()).getHits();
                for (Animal value : search) avg+=value.getWeight();
                System.out.println("Srednia waga zwierzat wynosi: "+avg/search.size());
            } else if (operation == 7) {
                List<Animal> search =index.search(new Query()).getHits();
                for (Animal value : search) System.out.println(value);
            } else if (operation == 0) {
                System.out.println("Zamkniecie aplikacji");
                client.close();
                return;
            } else {
                System.out.println("Wprowadzon złą wartośc");
                operation = showMainOperation(scan);
                scan.nextLine();
            }

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


}
