
public class ArrivalBuffer {

    //Paketeri Tutan Dizi
    public Parcel[] queue;
    //Kuyruğun başı(ilk öğe)
    public int front;
    //Kuyruğun sonuna eklenen son öğe
    public int rear;
    //Kuyruğun kaç öğe olduğunu takip eder
    public int count;
    //Kuyruğun maximum kapasitesi
    public int capacity;
    //Maximum görülen kuyruk boyutu
    public int maxSeen = 0;


    //CONSTRUCTOR:
    public ArrivalBuffer(int capacity) {
        this.capacity = capacity;
        this.queue = new Parcel[capacity];
        this.front = 0;
        this.rear = -1;
        this.count = 0;
    }

    //Dolu mu?
    public boolean isFull() {
        return count == capacity;
    }

    //Boş mu?
    public boolean isEmpty() {
        return count == 0;
    }

    //Kuyruktaki eleman sayısı
    public int size() {
        return count;
    }

    //Kuyrukta şimdiye kadar aynı anda maximum kaç eleman bulunduğunu öğrenmek için kullanılır
    public int getMaxSeen() {
        return maxSeen;
    }

    //Enqueue For Parcel
    public void enqueue(Parcel parcel) {
        if (isFull()) {
            System.out.println("Warning:Queue is full.Parcel discarded -> ID: " + parcel.parcelID);
            return;
        }
        //Circular yapı için.kapasiteyi aşarsa 0'a sarar
        rear = (rear + 1) % capacity;
        queue[rear] = parcel;
        count++;

        if(count > maxSeen) maxSeen = count;
    }

    //Dequeue
    public Parcel dequeue() {
        if (isEmpty()) return null;
        Parcel item = queue[front];
        front = (front + 1) % capacity;
        count--;
        return item;
    }

    //Kuyruğun başındaki nesne
    public Parcel peek(){
        if(isEmpty()) return null;
        return queue[front];
    }
}