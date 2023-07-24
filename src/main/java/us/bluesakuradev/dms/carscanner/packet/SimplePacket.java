package us.bluesakuradev.dms.carscanner.packet;

public class SimplePacket {
    String data;
    String timeStamp;
    String label;
    String units;

    public SimplePacket(String label, String timeStamp, String data){
        this.data = data.replaceAll("\"", "");
        this.timeStamp = timeStamp.replaceAll("\"", "");
        this.label = label.replaceAll("\"", "");
        this.units = "U";
    }

    public SimplePacket(String label, String timeStamp, String data, String units){
        this(label, timeStamp, data);
        this.units = units.replaceAll("\"", "");
    }

    public String getData(){
        return data;
    }

    public String getTimeStamp(){
        return timeStamp;
    }

    public String getLabel(){
        return label;
    }

    public String getUnits(){
        return units;
    }

    public String toString(){
        return timeStamp + "," + label + "," + data;
    }
}
