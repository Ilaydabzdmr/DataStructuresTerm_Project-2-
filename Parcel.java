public class Parcel {

    //Paketle ilgili bilgiler:
    //ID
    public  String parcelID;
    //Gönderilceği Şehir
    public  String  destinationCity;
    //Öncelik Seviyesi (1 = low, 2 = medium, 3 = high)
    public  int  priority;
    //Paketin Boyutu  (”Small”, ”Medium”, ”Large”)
    public  String size;
    //Bu paket simülasyona hangi zaman adımında(tick)gelmiş
    public  int arrivalTick;
    //Paketin mevcut durumu  (”InQueue”, ”Sorted”, ”Dispatched”, ”Returned”)
    public  String status;

    //CONSTRUCTOR:
    public Parcel(String parcelID, String destinationCity, int priority, String size, int arrivalTick, String  status) {
        this.parcelID = parcelID;
        this.destinationCity = destinationCity;
        this.priority = priority;
        this.size = size;
        this.arrivalTick = arrivalTick;
        this.status = "InQueue";
    }


}
