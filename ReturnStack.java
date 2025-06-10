
public class ReturnStack {

    public class Node {
        //Bu node da tutulan kargo
        Parcel parcel;
        Node next;

        public Node(Parcel parcel){
            this.parcel = parcel;
        }
    }

    public Node top;
    public int size;

    //CONSTRUCTOR
    public ReturnStack(){
        top = null;
        size = 0;
    }

    //Kargo ekleme
    public void push(Parcel parcel){
        Node newNode = new Node(parcel);
        //Yeni Node un next i eski top u göstersin
        newNode.next = top;
        top = newNode;
        size++;
    }

    //Kargo çıkartma
    public Parcel pop(){
        if(isEmpty()) return null;
        Parcel p = top.parcel;
        top = top.next;
        size--;
        return p;
    }

    //Tepedeki
    public Parcel peek(){
        return isEmpty() ? null : top.parcel;
    }

    public boolean isEmpty(){
        return top == null;
    }

    public int size(){
        return size;
    }
}
