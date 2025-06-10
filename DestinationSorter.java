import java.util.LinkedList;

public class DestinationSorter {

    public static class Node{
        String cityName;
        //O şehire ait kargoların listesi
        LinkedList<Parcel> parcels;
        //Ağaçtaki sol ve sağ nodes
        Node left,right;

       // /////////////////////////////////////////////////////////
        //Tüm kargoların öncelik puanlarının toplamı
        int totalPriorityScore;
        //AVL için yükseklik bilgisi
        int height;
       // ////////////////////////////////////////////////////////

        public Node(String cityName){
            this.cityName = cityName;
            this.parcels = new LinkedList<>();

            // ////////////////////////////////////////////////////////
            this.totalPriorityScore = 0;
            this.height = 1;//Yeni düğümün başlangıç yüksekliği 1
            // ////////////////////////////////////////////////////////
        }
    }

    //Kök
    public Node root;

    // ////////////////////////////////////////////////////////
    public int nodeCount = 0;
    // ////////////////////////////////////////////////////////

    //Kargo eklendiğinde şehrin adına göre ağaca ekleme yapılır
    public  void insertParcel(Parcel parcel){
        root = insertRecursive(root,parcel);
    }

    public Node insertRecursive(Node node,Parcel parcel){
        if(node == null){
            Node newNode = new Node(parcel.destinationCity);
            newNode.parcels.add(parcel);
            nodeCount++;
            return newNode;
        }

        //int result = string1.compareTo(string2);
        //Bu metot şu kurallara göre çalışır:
        //
        //string1.compareTo(string2) sonucu	Anlamı
        // < 0	string1, alfabetik olarak önce gelir
        //  0	string1 ve string2 aynıdır
        // > 0	string1, alfabetik olarak sonra gelir

        //Alfabetik olrak şehir isimleri sıralama
        int cmp = parcel.destinationCity.compareTo(node.cityName);

        // kargonun şehir adı, mevcut düğümün adından önce gelir (sola git)
        if(cmp < 0){
            node.left = insertRecursive(node.left,parcel);
        }
        // sonra gelir (sağa git)
        else if(cmp > 0){
            node.right = insertRecursive(node.right,parcel);
        }
        //aynı şehir (mevcut düğüme ekle)
        else{
            node.parcels.add(parcel);
            node.totalPriorityScore += parcel.priority;
            return node;
        }

        // ////////////////////////////////////////////////////////
        //AVL ağacını dengede tutmak için yükseklik ve balans kontrolü
        node.height = 1 + Math.max(getHeight(node.left),getHeight(node.right));
        int balance = getBalance(node);

        //Dengesizlik durumlarında dönüşler (rotation)

        // Sol-sol durumu
        if (balance > 1 && parcel.destinationCity.compareTo(node.left.cityName) < 0)
            return rotateRight(node);

        // Sağ-sağ durumu
        if (balance < -1 && parcel.destinationCity.compareTo(node.right.cityName) > 0)
            return rotateLeft(node);

        // Sol-sağ durumu
        if (balance > 1 && parcel.destinationCity.compareTo(node.left.cityName) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Sağ-sol durumu
        if (balance < -1 && parcel.destinationCity.compareTo(node.right.cityName) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }
        // ////////////////////////////////////////////////////////

        return node;

    }

    // ////////////////////////////////////////////////////////
    //Bir node un yüksekliğini döner
    public int getHeight(Node node){
        return (node == null) ? 0 : node.height;
    }
    // ////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////
    // AVL balans faktörünü hesaplar: sol yükseklik - sağ yükseklik
    public int getBalance(Node node){
        return (node == null) ? 0 : getHeight(node.left) - getHeight(node.right);
    }

    // Sağ dönüş (rotate right) - sol-sol dengesizlikte kullanılır
    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left = T2;

        // Yükseklik güncelle
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;

        return x; // Yeni kök
    }

    // Sol dönüş (rotate left) - sağ-sağ dengesizlikte kullanılır
    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        y.left = x;
        x.right = T2;

        // Yükseklik güncelle
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;

        return y; // Yeni kök
    }

    // ////////////////////////////////////////////////////////

    //Belirli bir şehirdeki tüm kargoları alma
    public LinkedList<Parcel> getCityParcels(String city){
        Node node = search(root, city);
        return node == null ? null : node.parcels;
    }

    //Belirtilen parcelID ye göre kargoyu siler
    public void removeParcel(String city, String parceID){
        Node node = search(root,city);
        if(node != null){
            node.parcels.removeIf(p -> p.parcelID.equals(parceID));
        }
    }

    //Bir şehirde kaç kargo var
    public int countCityParcels(String city){
        Node node = search(root, city);
        return node == null ? 0 : node.parcels.size();
    }


    //Ağaçta şehri arar. Alfabetik sıraya göre sağa ya da sola ilerleyerek arama yapar.
    public Node search(Node node, String city){
        if(node == null) return null;

        int cmp = city.compareTo(node.cityName);
        if(cmp == 0) return node;
        return (cmp < 0) ? search(node.left,city) : search(node.right,city);
    }

    //ağaçtaki kök düğümden (root) başlayarak in-order traversal (orta sırada dolaşım) yapmayı başlatır.
    public void inOrderTraversal(){
        traverse(root);
    }

    //ağacı alfabetik sıraya göre dolaşır ve her şehir ile kargo sayısını yazdırır:
    public void traverse(Node node){
        if(node != null){
            traverse(node.left);
            System.out.println("City:"+node.cityName+"Parcel count:"+node.parcels.size());
        }
    }

    // ////////////////////////////////////////////////////////
    // AVL ağacının yüksekliğini döner
    public int getHeight() {
        return getHeight(root);
    }

    // Toplam kaç düğüm (şehir) olduğunu döner
    public int getNodeCount() {
        return nodeCount;
    }

    // En fazla kargoya sahip şehrin ismini döner
    public String getMostLoadedCity() {
        return getMaxCity(root, "", 0);
    }

    // Rekürsif olarak en yoğun şehir bulunur
    private String getMaxCity(Node node, String maxCity, int maxLoad) {
        if (node == null) return maxCity;

        if (node.parcels.size() > maxLoad) {
            maxCity = node.cityName;
            maxLoad = node.parcels.size();
        }

        maxCity = getMaxCity(node.left, maxCity, maxLoad);
        return getMaxCity(node.right, maxCity, maxLoad);
    }
    // ////////////////////////////////////////////////////////
}
