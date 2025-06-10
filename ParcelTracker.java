import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;


public class ParcelTracker {

    //Kargo infoları
    public class Record {
        //Durumu
        String status;
        //Giriş zamanı
        int arrivalTick;
        //Sistemden çıkış(dağıtım) zamanı
        int dispatchTick;
        //Kargonun iade sayısı
        int returnCount;
        //Kargonun gidiceği şehit
        String destinationCity;
        //Kargonun öncelik seviyesi
        int priority;
        //Kargonun boyutu
        String size;

        // BONUS//////////////////////////////////////////////////////////
        LinkedList<String> statusHistory = new LinkedList<>(); // BONUS: Geçmiş durumlar
        // /////////////////////////////////////////////////////////
    }

    //Hash tablosunda saklanan her biröğeyi temsil eden iç sınıf
    public class Entry {
        //ParcelID
        String key;
        //Record object(Kargonun detaylarını tutar)
        Record value;
        //Aynı indekste çakışma olursa Linked List'te bir sonraki giriş
        Entry next;

        //Entry object
        public Entry(String key, Record value) {
            this.key = key;
            this.value = value;
        }
    }

    //Hash tablosu dizisi
    public Entry[] table;
    //Şu anda tabloda bulunan kargo sayısı
    public int size;

    //Belirli kapasite ile hash tablosu oluşturur
    public ParcelTracker(int capacity) {
        //Entry dizisi
        table = new Entry[capacity];
        size = 0;//Başta boş
    }

    //Key (parcelID) için hash değeri üretir
    public int hash(String key) {
        return Math.abs(key.hashCode()) % table.length;//Negatif değerleri önelemek için abs
    }

    //Yeni bir gargo kaydı ekler
    public void insert(Parcel parcel) {
        //Eğer zaten varsa ekleme
        if (exists(parcel.parcelID)) return;

        // BONUS///////////////////////////////
        //yeniden boyutlandırma kontrolü
        if(size >= table.length*0.75) resize();
        // ////////////////////////////////////

        //Yeni bir kayıt oluştur ve bilgileri doldur
        Record record = new Record();
        record.arrivalTick = parcel.arrivalTick;
        record.status = parcel.status;
        record.destinationCity = parcel.destinationCity;
        record.priority = parcel.priority;
        record.size = parcel.size;
        record.returnCount = 0;//İlk başta iade sıfırdır

        //Hash tablosuna kaydı ekle
        put(parcel.parcelID, record);
    }

    //Hash tablosuna yeni anahtar değer çifti ekleme
    public void put(String key, Record value) {
        //Hash indexsini bul
        int index = hash(key);
        //Yeni giriş oluşturur
        Entry e = new Entry(key, value);
        //Bu indeksteki eski kayıtları incele
        e.next = table[index];
        //yeni kaydı listenin başına ekle
        table[index] = e;
        //toplam boyutu bir artır
        size++;
    }

    //Belirli bir kargonun durumunu günceller
    public void updateStatus(String parcelID, String newStatus) {
        //Kargo kaydını bul
        Entry e = getEntry(parcelID);
        //Kayıt varsa durumunu güncelle
        if (e != null) {
            e.value.status = newStatus;
            e.value.statusHistory.add(newStatus);//Bonus
        }
    }

    //Belirli bir kargonun iade sayısı
    public void incrementReturnCount(String parcelID) {
        //Kargo kaydını bul
        Entry e = getEntry(parcelID);
        //Kayıt varsa durumunu güncelle
        if (e != null) e.value.returnCount++;
    }

    //Belirli bir kargonun iade sayısını döndürür
    public int getReturnCount(String parcelID) {
        Entry e = getEntry(parcelID);
        return (e != null) ? e.value.returnCount : 0;
    }

    //Belirtilen ID'ye sahip bir kargo tabloda var mı
    public boolean exists(String parcelID) {
        return getEntry(parcelID) != null;
    }

    //Belirli bir kargo ID'sine karşılık gelen Entry nesnesini döndürür
    public Entry getEntry(String parcelID) {
        //Hash indeksini hesapla
        int index = hash(parcelID);
        //Bu indeksteki ilk entryyi al
        Entry e = table[index];

        //Aynı indekste zincirlenmiş tüm girişleri tara
        while (e != null) {
            if (e.key.equals(parcelID)) return e;
            e = e.next;
        }

        return null;
    }

    //Kargonun dağıtıma çıkış zamanını ayarla
    public void setDispatchTick(String parcelID, int tick) {
        //Kaydı bul
        Entry e = getEntry(parcelID);
        if (e != null) e.value.dispatchTick = tick;//Kayıt varsa dispatchTick değerini güncelle
    }

    //Dağıtıma çıkan kargolar için ortalama bekleme süresini hesapla(dispatch - arrival)
    public double getAverageWaitTime() {
        //Toplam süre ve kaç kargonun hesaplandığı
        int total = 0, count = 0;

        //Tablodaki her "bucket"(dizi hücresi) üzerinden geç(tara)
        for (Entry bucket : table) {
            //İlk entry'i al
            Entry e = bucket;

            while (e != null) {
                //Sadece dağıtıma çıkmış olan kargolar dikkate alınır
                if (e.value.status.equals("Dispatched")) {
                    //Kargonun bekleme süresi: dispatch - arrival
                    total += (e.value.dispatchTick - e.value.arrivalTick);
                    //Hesaplanan kargo sayısını arttırır
                    count++;
                }
                e = e.next;
            }
        }

        return count == 0 ? 0 : (double) total / count;
    }

    // BONUS/////////////////////////////////////////////////////////////////////////////////////
    //Hash tablosunu daha büyük boyutta yenidenoluşturur
    public void resize(){

        //Mevcut tabloyu yedekle
        Entry[] oldTable = table;
        //Yeni tabloyu 2 kat büyük oluştur
        table = new Entry[oldTable.length*2];
        //boyutu sıfırla çünkü yeniden ekliyeceğiz
        size = 0;

        //Eski tablodaki yüm entry'leri yeni tabloya ekle
        for(Entry bucket : oldTable){
            Entry e = bucket;
            while(e != null){
                put(e.key, e.value);//put() metodu ile zaten hash hesaplayıp ekliyoruz
                e = e.next;
            }
        }
        //Bilgilendirme
        System.out.println("ParcelTracker resized to capacity: "+table.length);
    }

    //Tablodaki veriyi dosyaya aktarma(Debug için)
    public void exportToFile(String filename){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
            //Bsşlık satırı
            writer.write("ParcelID | Status | ReturnCount | StatusHistory\n");

            //Tablonun tüm Entry'leri taranır
            for(Entry bucket : table){
                Entry e = bucket;

                while(e != null){
                    Record r = e.value; //Kargo bilgidi alınır

                    //Her kargo satır olarak dosyaya yazılır
                    writer.write(String.format("%s | %s | %s\n", e.key, r.status, r.returnCount, r.statusHistory.toString()));

                    e = e.next;
                }
            }

        } catch (IOException ex) {
            System.err.println("Error writing to file: " + ex.getMessage());
        }
    }
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////
    
}